import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, timeout } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';

import {
  DisponibilitaPrenotazioneDTO,
  MessageResponse,
  PostoWorkspaceDTO,
  PrenotazioneDTO,
  PrenotazioniApiService
} from './prenotazioni-api.service';
import { AuthApiService, UserInfo } from '../../core/auth-api.service';

export type BookingZoneType = 'office' | 'meeting' | 'training' | 'support' | 'service';
export type BookingZoneStatus = 'free' | 'reserved' | 'selected' | 'disabled';

export interface BookingMetric {
  label: string;
  value: string;
}

export interface BookingZone {
  id: string;
  workspaceId?: number;
  label: string;
  type: BookingZoneType;
  status: BookingZoneStatus;
  cssClass: string;
  seats?: number;
  exclusive?: boolean;
  description?: string;
}

export interface AvailableSeat {
  idWorkspace?: number;
  idWorkspaceSeat?: number;
  seat: string;
  room: string;
  type: string;
  selected?: boolean;
}

export interface MapSeatMarker {
  idWorkspace: number;
  idWorkspaceSeat?: number;
  label: string;
  cssClass: string;
  occupied: boolean;
  seatName?: string;
  roomName?: string;
}

export interface FeedbackMessage {
  type: 'success' | 'error';
  text: string;
}

@Component({
  selector: 'app-prenotazioni',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    DialogModule,
    TagModule,
    TooltipModule
  ],
  templateUrl: './prenotazioni.component.html',
  styleUrl: './prenotazioni.component.css'
})
export class PrenotazioniComponent implements OnInit {
  private readonly feedbackStorageKey = 'prenotazioniFeedbackMessage';
  private readonly selectedDateStorageKey = 'prenotazioniSelectedDate';
  private readonly router = inject(Router);
  private readonly prenotazioniApi = inject(PrenotazioniApiService);
  private readonly authApi = inject(AuthApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  private readonly workspaceIds = [1, 3, 4];
  private readonly seatMarkerClasses: Record<number, string[]> = {
    1: ['seat-w1-1', 'seat-w1-2'],
    3: ['seat-w3-1', 'seat-w3-2', 'seat-w3-3', 'seat-w3-4', 'seat-w3-5', 'seat-w3-6'],
    4: ['seat-w4-1', 'seat-w4-2', 'seat-w4-3', 'seat-w4-4']
  };

  selectedDate = this.getStoredSelectedDate();
  selectedZone: BookingZone | null = null;
  selectedSeat: AvailableSeat | null = null;
  showZoneDialog = false;
  isLoading = false;
  isLoadingPosti = false;
  isSaving = false;
  mapZoom = 1;
  readonly minMapZoom = 1;
  readonly maxMapZoom = 1.6;
  feedbackMessage: FeedbackMessage | null = null;
  private feedbackTimeoutId?: number;
  currentUser: UserInfo | null = this.authApi.getCurrentUserInfo();

  metrics: BookingMetric[] = [
    { label: 'Posti totali', value: '12' },
    { label: 'Posti liberi', value: '7' }
  ];

  availableSeats: AvailableSeat[] = [];
  myBookings: PrenotazioneDTO[] = [];
  postiPerWorkspace: Record<number, PostoWorkspaceDTO[]> = {};

  zones: BookingZone[] = [
    {
      id: 'academy',
      label: 'Academy',
      type: 'training',
      status: 'free',
      cssClass: 'zone-academy',
      exclusive: true,
      description: 'Sala academy prenotabile dai profili abilitati'
    },
    {
      id: 'workspace-1',
      workspaceId: 1,
      label: 'Ufficio 1',
      type: 'office',
      status: 'free',
      cssClass: 'zone-workspace-1',
      seats: 2,
      description: 'Ufficio in basso a destra con due postazioni'
    },
    {
      id: 'workspace-3',
      workspaceId: 3,
      label: 'Ufficio 2',
      type: 'office',
      status: 'free',
      cssClass: 'zone-workspace-3',
      seats: 6,
      description: 'Sala riunioni alla sinistra della sala d attesa'
    },
    {
      id: 'workspace-4',
      workspaceId: 4,
      label: 'Open space',
      type: 'office',
      status: 'free',
      cssClass: 'zone-workspace-4',
      seats: 4,
      description: 'Open space sopra la sala d attesa'
    },
    {
      id: 'meeting',
      label: 'Sala Riunioni',
      type: 'meeting',
      status: 'reserved',
      cssClass: 'zone-meeting',
      exclusive: true,
      description: 'Ufficio in alto a destra riservato a CTO/AU'
    }
  ];

  get selectedDateLabel(): string {
    return this.formatItalianDate(this.selectedDate);
  }

  get firstBooking(): PrenotazioneDTO | null {
    return this.myBookings[0] ?? null;
  }

  get mapSeatMarkers(): MapSeatMarker[] {
    return this.workspaceIds.flatMap(idWorkspace => {
      const posti = this.postiPerWorkspace[idWorkspace] ?? [];
      const cssClasses = this.seatMarkerClasses[idWorkspace] ?? [];

      return posti.map((posto, index) => ({
        idWorkspace,
        idWorkspaceSeat: posto.idWorkspaceSeat,
        label: String(index + 1),
        cssClass: cssClasses[index] ?? '',
        occupied: posto.occupato,
        seatName: posto.nome || posto.codice,
        roomName: this.getWorkspaceLabel(idWorkspace)
      }));
    });
  }

  get currentUserName(): string {
    return this.currentUser?.name || this.currentUser?.email || 'Utente';
  }

  get currentUserProfileCode(): string {
    return this.currentUser?.profileCode || '';
  }

  get currentUserProfileLabel(): string {
    const profileCode = this.currentUser?.profileCode;
    const roleDescription = this.currentUser?.descrizioneRuolo
      || this.currentUser?.roleName
      || this.currentUser?.roleCode;

    return [profileCode, roleDescription]
      .filter(Boolean)
      .join(' - ');
  }

  get currentUserInitials(): string {
    return this.currentUserName
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map(part => part[0]?.toUpperCase())
      .join('') || 'U';
  }

  get canReserveSelectedZone(): boolean {
    if (!this.selectedZone || this.selectedZone.status === 'reserved' || this.isSaving) {
      return false;
    }

    return Boolean(this.selectedZone.exclusive || this.selectedSeat);
  }

  get mapTransform(): string {
    return `scale(${this.mapZoom})`;
  }

  ngOnInit(): void {
    this.restoreFeedbackMessage();
    this.refreshPrenotazioni(!this.feedbackMessage);
  }

  goToLogin(): void {
    this.authApi.clearSession();
    void this.router.navigateByUrl('/login');
  }

  openDatePicker(input: HTMLInputElement): void {
    input.focus();

    const dateInput = input as HTMLInputElement & { showPicker?: () => void };
    if (dateInput.showPicker) {
      dateInput.showPicker();
      return;
    }

    input.click();
  }

  onDateChange(value: string): void {
    if (!value || value === this.selectedDate) {
      return;
    }

    this.selectedDate = value;
    sessionStorage.setItem(this.selectedDateStorageKey, value);
    this.selectedSeat = null;
    this.refreshPrenotazioni();
  }

  increaseMapZoom(): void {
    this.mapZoom = Math.min(this.maxMapZoom, this.roundMapZoom(this.mapZoom + 0.15));
  }

  decreaseMapZoom(): void {
    this.mapZoom = Math.max(this.minMapZoom, this.roundMapZoom(this.mapZoom - 0.15));
  }

  refreshPrenotazioni(clearFeedback = true): void {
    this.loadPrenotazioni(clearFeedback);
    this.caricaPostiWorkspace(clearFeedback);
  }

  private roundMapZoom(value: number): number {
    return Number(value.toFixed(2));
  }

  loadPrenotazioni(clearFeedback = true): void {
    this.isLoading = true;
    if (clearFeedback) {
      this.feedbackMessage = null;
    }

    forkJoin({
      disponibilita: this.prenotazioniApi.trovaDisponibili(this.selectedDate).pipe(
        catchError(error => this.handleListError<DisponibilitaPrenotazioneDTO>(error, clearFeedback))
      ),
      prenotate: this.prenotazioniApi.trovaPrenotate(this.selectedDate).pipe(
        catchError(error => this.handleListError<PrenotazioneDTO>(error, clearFeedback))
      ),
      miePrenotazioni: this.prenotazioniApi.trovaMiePrenotazioni(this.selectedDate).pipe(
        catchError(error => this.handleListError<PrenotazioneDTO>(error, clearFeedback))
      )
    })
      .pipe(finalize(() => this.isLoading = false))
      .subscribe(result => {
        this.myBookings = result.miePrenotazioni;
        this.updateZones(result.disponibilita);
        this.updateZonesFromPosti(this.postiPerWorkspace);
        this.updateExclusiveZonesFromBookings(result.prenotate);
        this.updateMetrics(result.disponibilita, this.postiPerWorkspace);
      });
  }

  selectZone(zone: BookingZone): void {
    if (zone.status === 'disabled') {
      return;
    }

    this.selectedZone = zone;
    this.selectedSeat = null;
    this.showZoneDialog = true;
  }

  reserveSelectedZone(): void {
    if (!this.selectedZone) {
      return;
    }

    if (this.selectedZone.exclusive) {
      this.createBooking(this.selectedZone.workspaceId);
      return;
    }

    if (!this.selectedSeat) {
      this.showError('Seleziona un pallino verde della mappa prima di prenotare.');
      return;
    }

    this.prenotaPosto(this.selectedSeat);
  }

  freeSelectedZone(): void {
    const booking = this.myBookings.find(prenotazione => {
      const idWorkspace = this.getBookingWorkspaceId(prenotazione);
      return idWorkspace === this.selectedZone?.workspaceId;
    });

    if (!booking) {
      this.showZoneDialog = false;
      return;
    }

    this.annullaPrenotazione(booking.idPrenotazione);
  }

  prenotaPosto(seat: AvailableSeat): void {
    if (this.isSaving) {
      return;
    }

    this.selectedSeat = seat;
    this.availableSeats = this.availableSeats.map(item => ({
      ...item,
      selected: item.idWorkspaceSeat === seat.idWorkspaceSeat
    }));
    this.createBooking(seat.idWorkspace, seat.idWorkspaceSeat);
  }

  selectMapSeat(marker: MapSeatMarker): void {
    if (marker.occupied) {
      this.showError('Questo posto risulta gia prenotato per la data selezionata.');
      return;
    }

    if (marker.idWorkspaceSeat == null) {
      this.showError('Dettaglio posto non disponibile. Ricarica la pagina e riprova.');
      return;
    }

    const seat: AvailableSeat = {
      idWorkspace: marker.idWorkspace,
      idWorkspaceSeat: marker.idWorkspaceSeat,
      seat: marker.seatName || `Posto ${marker.label}`,
      room: marker.roomName || this.getWorkspaceLabel(marker.idWorkspace),
      type: this.getSeatType(marker.idWorkspace),
      selected: true
    };

    this.selectedSeat = seat;
    this.selectedZone = this.zones.find(zone => zone.workspaceId === marker.idWorkspace) ?? null;
    this.availableSeats = this.availableSeats.map(item => ({
      ...item,
      selected: item.idWorkspaceSeat === marker.idWorkspaceSeat
    }));
    this.showZoneDialog = true;
    this.feedbackMessage = null;
  }

  annullaPrenotazione(idPrenotazione: number): void {
    this.isSaving = true;
    this.feedbackMessage = null;

    this.prenotazioniApi.eliminaPrenotazione(idPrenotazione)
      .pipe(
        timeout(15000),
        finalize(() => this.isSaving = false)
      )
      .subscribe({
        next: response => {
          this.showZoneDialog = false;
          this.showSuccessFromResponse(response, 'Prenotazione annullata correttamente.');
          this.refreshPageAfterAction();
        },
        error: error => this.handleActionError(error, 'Annullamento inviato. Aggiorno lo stato delle prenotazioni.')
      });
  }

  getBookingWorkspaceName(prenotazione: PrenotazioneDTO): string {
    return prenotazione.nomeWorkspace
      || prenotazione.nomeRisorsa
      || prenotazione.codiceWorkspace
      || prenotazione.codiceRisorsa
      || 'Prenotazione workspace';
  }

  getZoneSeverity(status: BookingZoneStatus): 'success' | 'danger' | 'info' | 'secondary' | 'warn' {
    const severities: Record<BookingZoneStatus, 'success' | 'danger' | 'info' | 'secondary' | 'warn'> = {
      free: 'success',
      reserved: 'danger',
      selected: 'info',
      disabled: 'secondary'
    };

    return severities[status];
  }

  getZoneStatusLabel(status: BookingZoneStatus): string {
    const labels: Record<BookingZoneStatus, string> = {
      free: 'Libero',
      reserved: 'Prenotato',
      selected: 'Selezionato',
      disabled: 'Privato'
    };

    return labels[status];
  }

  trackBySeat(_: number, seat: AvailableSeat): number | string {
    return seat.idWorkspaceSeat ?? seat.seat;
  }

  trackByMarker(_: number, marker: MapSeatMarker): number | string {
    return marker.idWorkspaceSeat ?? `${marker.idWorkspace}-${marker.cssClass}`;
  }

  private createBooking(idWorkspace?: number, idWorkspaceSeat?: number): void {
    if (this.isSaving) {
      return;
    }

    if (!idWorkspace) {
      this.showError('Workspace non disponibile o profilo non abilitato.');
      return;
    }

    this.isSaving = true;
    this.feedbackMessage = null;
    this.showZoneDialog = false;
    this.changeDetector.detectChanges();

    this.prenotazioniApi.creaPrenotazione({
      idWorkspace,
      idWorkspaceSeat,
      dataPrenotazione: this.selectedDate
    })
      .pipe(
        timeout(15000),
        finalize(() => this.isSaving = false)
      )
      .subscribe({
        next: response => {
          this.markSeatAsBooked(idWorkspaceSeat);
          this.showSuccessFromResponse(response, 'Prenotazione creata correttamente.');
          this.refreshPageAfterAction();
        },
        error: error => this.handleActionError(error, 'Prenotazione inviata. Aggiorno lo stato delle prenotazioni.')
      });
  }

  private caricaPostiWorkspace(clearFeedback = true): void {
    this.isLoadingPosti = true;
    this.postiPerWorkspace = {};
    this.availableSeats = [];

    let chiamateCompletate = 0;
    let postiRicevuti = 0;
    const completaChiamata = () => {
      chiamateCompletate++;

      if (chiamateCompletate === this.workspaceIds.length) {
        this.isLoadingPosti = false;

        if (postiRicevuti === 0 && clearFeedback) {
          this.showError('La chiamata dei posti non ha restituito postazioni prenotabili.');
        }
      }
    };

    this.workspaceIds.forEach(idWorkspace => {
      this.prenotazioniApi.trovaPostiWorkspace(idWorkspace, this.selectedDate)
        .subscribe({
          next: posti => {
            this.postiPerWorkspace = {
              ...this.postiPerWorkspace,
              [idWorkspace]: posti
            };
            postiRicevuti += posti.length;
            this.aggiornaPostiWorkspaceSuMappa(idWorkspace, posti);
            this.changeDetector.detectChanges();
          },
          error: error => {
            if (clearFeedback) {
              this.showError(this.createErrorMessage(error));
            }
            completaChiamata();
          },
          complete: completaChiamata
        });
    });
  }

  private aggiornaPostiWorkspaceSuMappa(idWorkspace: number, posti: PostoWorkspaceDTO[]): void {
    this.availableSeats = this.createAvailableSeats(this.postiPerWorkspace);
    this.updateZonesFromPosti(this.postiPerWorkspace);
    this.updateMetrics([], this.postiPerWorkspace);
  }

  private markSeatAsBooked(idWorkspaceSeat?: number): void {
    if (idWorkspaceSeat == null) {
      return;
    }

    this.postiPerWorkspace = Object.fromEntries(
      Object.entries(this.postiPerWorkspace).map(([idWorkspace, posti]) => [
        idWorkspace,
        posti.map(posto => posto.idWorkspaceSeat === idWorkspaceSeat
          ? { ...posto, occupato: true }
          : posto
        )
      ])
    );
    this.availableSeats = this.availableSeats.filter(seat => seat.idWorkspaceSeat !== idWorkspaceSeat);
    this.selectedSeat = null;
  }

  private updateZones(disponibilita: DisponibilitaPrenotazioneDTO[]): void {
    this.zones = this.zones.map(zone => {
      const workspace = this.findDisponibilitaByZone(zone, disponibilita);

      if (!workspace) {
        return zone;
      }

      const postiDisponibili = workspace.postiDisponibili ?? 0;

      return {
        ...zone,
        workspaceId: this.getWorkspaceId(workspace) ?? zone.workspaceId,
        seats: workspace.capienza ?? zone.seats,
        exclusive: workspace.prenotazioneEsclusiva ?? zone.exclusive,
        status: postiDisponibili > 0 ? 'free' : 'reserved'
      };
    });
  }

  private updateZonesFromPosti(postiPerWorkspace: Record<number, PostoWorkspaceDTO[]>): void {
    this.zones = this.zones.map(zone => {
      if (!zone.workspaceId || !postiPerWorkspace[zone.workspaceId]?.length) {
        return zone;
      }

      const posti = postiPerWorkspace[zone.workspaceId];
      const postiLiberi = posti.filter(posto => !posto.occupato).length;

      return {
        ...zone,
        seats: posti.length,
        status: postiLiberi > 0 ? 'free' : 'reserved'
      };
    });
  }

  private updateExclusiveZonesFromBookings(prenotate: PrenotazioneDTO[]): void {
    this.zones = this.zones.map(zone => {
      if (!zone.exclusive) {
        return zone;
      }

      const isBooked = prenotate.some(prenotazione => {
        const idWorkspace = this.getBookingWorkspaceId(prenotazione);
        const bookingName = this.getBookingWorkspaceName(prenotazione).toLowerCase();

        return idWorkspace === zone.workspaceId || bookingName.includes(zone.id === 'meeting' ? 'riunioni' : zone.id);
      });

      return isBooked ? { ...zone, status: 'reserved' } : zone;
    });
  }

  private updateMetrics(
    disponibilita: DisponibilitaPrenotazioneDTO[],
    postiPerWorkspace: Record<number, PostoWorkspaceDTO[]>
  ): void {
    const posti = Object.values(postiPerWorkspace).flat();
    const postiTotali = posti.length || disponibilita.reduce((total, item) => total + (item.capienza ?? 0), 0);
    const postiLiberi = posti.length
      ? posti.filter(posto => !posto.occupato).length
      : disponibilita.reduce((total, item) => total + (item.postiDisponibili ?? 0), 0);

    this.metrics = [
      { label: 'Posti totali', value: String(postiTotali || 12) },
      { label: 'Posti liberi', value: String(postiLiberi || 0) }
    ];
  }

  private createAvailableSeats(postiPerWorkspace: Record<number, PostoWorkspaceDTO[]>): AvailableSeat[] {
    return Object.values(postiPerWorkspace)
      .flat()
      .filter(posto => !posto.occupato)
      .map((posto, index) => ({
        idWorkspace: posto.idWorkspace,
        idWorkspaceSeat: posto.idWorkspaceSeat,
        seat: posto.nome || posto.codice || `Posto ${index + 1}`,
        room: this.getWorkspaceLabel(posto.idWorkspace),
        type: this.getSeatType(posto.idWorkspace),
        selected: index === 0
      }));
  }

  private findDisponibilitaByZone(
    zone: BookingZone,
    disponibilita: DisponibilitaPrenotazioneDTO[]
  ): DisponibilitaPrenotazioneDTO | undefined {
    if (zone.workspaceId) {
      return disponibilita.find(item => this.getWorkspaceId(item) === zone.workspaceId);
    }

    const zoneKey = zone.id === 'meeting' ? 'riunioni' : zone.id;

    return disponibilita.find(item => {
      const text = `${item.nome ?? ''} ${item.codice ?? ''}`.toLowerCase();
      return text.includes(zoneKey);
    });
  }

  private getWorkspaceId(item: DisponibilitaPrenotazioneDTO): number | undefined {
    return item.idWorkspace ?? item.idRisorsaPrenotabile;
  }

  private getBookingWorkspaceId(prenotazione: PrenotazioneDTO): number | undefined {
    return prenotazione.idWorkspace ?? prenotazione.idRisorsaPrenotabile;
  }

  private getWorkspaceLabel(idWorkspace?: number): string {
    return this.zones.find(zone => zone.workspaceId === idWorkspace)?.label ?? 'Workspace';
  }

  private getSeatType(idWorkspace?: number): string {
    return idWorkspace === 3 || idWorkspace === 4
      ? 'Scrivania condivisa'
      : 'Scrivania singola';
  }

  private handleListError<T>(error: unknown, showFeedback = true) {
    if (showFeedback) {
      this.showError(this.createErrorMessage(error));
    }

    return of([] as T[]);
  }

  private showSuccessFromResponse(response: MessageResponse | null | undefined, fallbackMessage: string): void {
    this.setFeedbackMessage({
      type: 'success',
      text: response?.message || fallbackMessage
    });
  }

  private handleActionError(error: unknown, timeoutMessage: string): void {
    if (this.isTimeoutError(error)) {
      this.setFeedbackMessage({
        type: 'success',
        text: timeoutMessage
      });
      this.showZoneDialog = false;
      this.refreshPageAfterAction();
      return;
    }

    this.showZoneDialog = false;
    this.showError(this.createErrorMessage(error));
  }

  private refreshPageAfterAction(): void {
    if (this.feedbackMessage) {
      sessionStorage.setItem(this.feedbackStorageKey, JSON.stringify(this.feedbackMessage));
    }

    sessionStorage.setItem(this.selectedDateStorageKey, this.selectedDate);
    window.setTimeout(() => window.location.reload(), 350);
  }

  private restoreFeedbackMessage(): void {
    const rawMessage = sessionStorage.getItem(this.feedbackStorageKey);

    if (!rawMessage) {
      return;
    }

    sessionStorage.removeItem(this.feedbackStorageKey);

    try {
      this.setFeedbackMessage(JSON.parse(rawMessage) as FeedbackMessage);
    } catch {
      this.feedbackMessage = null;
    }
  }

  private isTimeoutError(error: unknown): boolean {
    return error instanceof Error && error.name === 'TimeoutError';
  }

  private showError(message: string): void {
    this.setFeedbackMessage({
      type: 'error',
      text: message
    });
  }

  private setFeedbackMessage(message: FeedbackMessage): void {
    const isSameMessage = this.feedbackMessage?.type === message.type
      && this.feedbackMessage?.text === message.text;

    if (isSameMessage && this.feedbackTimeoutId) {
      return;
    }

    this.feedbackMessage = message;
    this.scheduleFeedbackClear();
    this.changeDetector.detectChanges();
  }

  private scheduleFeedbackClear(): void {
    if (this.feedbackTimeoutId) {
      window.clearTimeout(this.feedbackTimeoutId);
    }

    this.feedbackTimeoutId = window.setTimeout(() => {
      this.feedbackMessage = null;
      this.feedbackTimeoutId = undefined;
      this.changeDetector.detectChanges();
    }, 3000);
  }

  private createErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'Errore durante il caricamento delle prenotazioni.';
    }

    if (error.status === 0) {
      return 'Backend non raggiungibile su localhost:8080.';
    }

    if (error.status === 401) {
      return 'Sessione non valida: effettua il login e riprova.';
    }

    if (error.status === 403) {
      return 'Profilo non abilitato per questa operazione.';
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (error.error?.message) {
      return error.error.message;
    }

    return 'Operazione non riuscita. Riprova tra poco.';
  }

  protected formatItalianDate(value: string): string {
    const date = new Date(`${value}T00:00:00`);
    const formatted = new Intl.DateTimeFormat('it-IT', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);

    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private getStoredSelectedDate(): string {
    const storedDate = sessionStorage.getItem(this.selectedDateStorageKey);

    return storedDate && /^\d{4}-\d{2}-\d{2}$/.test(storedDate)
      ? storedDate
      : this.toIsoDate(new Date());
  }
}
