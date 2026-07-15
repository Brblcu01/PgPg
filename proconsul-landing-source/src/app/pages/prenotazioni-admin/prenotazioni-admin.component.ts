import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, timeout } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

import { AuthApiService, UserInfo } from '../../core/auth-api.service';
import {
  MessageResponse,
  PrenotazioneDTO,
  PrenotazioniApiService
} from '../prenotazioni/prenotazioni-api.service';

interface FeedbackMessage {
  type: 'success' | 'error';
  text: string;
}

@Component({
  selector: 'app-prenotazioni-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, DialogModule],
  templateUrl: './prenotazioni-admin.component.html',
  styleUrl: './prenotazioni-admin.component.css'
})
export class PrenotazioniAdminComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthApiService);
  private readonly prenotazioniApi = inject(PrenotazioniApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  currentUser: UserInfo | null = this.authApi.getCurrentUserInfo();
  prenotazioni: PrenotazioneDTO[] = [];
  selectedBooking: PrenotazioneDTO | null = null;
  showDeleteDialog = false;
  isLoading = false;
  isSaving = false;
  feedbackMessage: FeedbackMessage | null = null;
  private feedbackTimeoutId?: number;

  filtro = {
    data: this.toIsoDate(new Date()),
    dataDa: '',
    dataA: ''
  };

  get isAdminUser(): boolean {
    const profileCode = this.currentUser?.profileCode?.toUpperCase();
    const roleCode = this.currentUser?.roleCode?.toUpperCase();

    return profileCode === 'ADMIN' || roleCode === 'HR';
  }

  get prenotazioniFiltrate(): PrenotazioneDTO[] {
    return this.prenotazioni;
  }

  get totalPrenotazioni(): number {
    return this.prenotazioni.length;
  }

  ngOnInit(): void {
    if (!this.isAdminUser) {
      void this.router.navigateByUrl('/prenotazioni');
      return;
    }

    this.caricaPrenotazioni();
  }

  goToLogin(): void {
    this.authApi.clearSession();
    void this.router.navigateByUrl('/login');
  }

  caricaPrenotazioni(showSuccess = false): void {
    if (!this.isAdminUser) {
      return;
    }

    this.setLoading(true);
    this.feedbackMessage = null;

    const usaIntervallo = Boolean(this.filtro.dataDa || this.filtro.dataA);
    const data = usaIntervallo ? undefined : this.filtro.data;

    this.prenotazioniApi.trovaPrenotate(data, this.filtro.dataDa || undefined, this.filtro.dataA || undefined)
      .pipe(finalize(() => this.setLoading(false)))
      .subscribe({
        next: prenotazioni => {
          this.prenotazioni = prenotazioni;
          this.changeDetector.detectChanges();

          if (showSuccess) {
            this.setFeedbackMessage({
              type: 'success',
              text: 'Lista prenotazioni aggiornata correttamente.'
            });
          }
        },
        error: error => {
          this.prenotazioni = [];
          this.showError(this.createErrorMessage(error));
        }
      });
  }

  openDeleteDialog(prenotazione: PrenotazioneDTO): void {
    this.selectedBooking = prenotazione;
    this.showDeleteDialog = true;
  }

  closeDeleteDialog(): void {
    this.showDeleteDialog = false;
    this.selectedBooking = null;
  }

  eliminaPrenotazioneAdmin(): void {
    if (!this.selectedBooking || this.isSaving) {
      return;
    }

    this.isSaving = true;
    this.feedbackMessage = null;

    this.prenotazioniApi.eliminaPrenotazione(this.selectedBooking.idPrenotazione)
      .pipe(
        timeout(15000),
        finalize(() => this.isSaving = false)
      )
      .subscribe({
        next: response => {
          this.closeDeleteDialog();
          this.showSuccessFromResponse(response, 'Prenotazione annullata correttamente. Email inviata al dipendente.');
          this.caricaPrenotazioniSilenzioso();
        },
        error: error => this.handleActionError(error, 'Annullamento inviato. Aggiorno la lista prenotazioni.')
      });
  }

  getBookingWorkspaceName(prenotazione: PrenotazioneDTO): string {
    const workspaceName = prenotazione.nomeWorkspace
      || prenotazione.nomeRisorsa
      || prenotazione.codiceWorkspace
      || prenotazione.codiceRisorsa
      || 'Workspace';

    return prenotazione.postazioneName
      ? `${workspaceName} - ${prenotazione.postazioneName}`
      : workspaceName;
  }

  getEmployeeLabel(prenotazione: PrenotazioneDTO): string {
    return prenotazione.utenteName
      || (prenotazione.idUtente ? `Utente #${prenotazione.idUtente}` : 'Utente non disponibile');
  }

  formatItalianDate(value?: string): string {
    if (!value) {
      return '-';
    }

    const date = new Date(`${value.slice(0, 10)}T00:00:00`);
    const formatted = new Intl.DateTimeFormat('it-IT', {
      weekday: 'short',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);

    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  }

  private caricaPrenotazioniSilenzioso(): void {
    const usaIntervallo = Boolean(this.filtro.dataDa || this.filtro.dataA);
    const data = usaIntervallo ? undefined : this.filtro.data;

    this.prenotazioniApi.trovaPrenotate(data, this.filtro.dataDa || undefined, this.filtro.dataA || undefined)
      .subscribe({
        next: prenotazioni => {
          this.prenotazioni = prenotazioni;
          this.changeDetector.detectChanges();
        },
        error: error => this.showError(this.createErrorMessage(error))
      });
  }

  private showSuccessFromResponse(response: MessageResponse | null | undefined, fallbackMessage: string): void {
    this.setFeedbackMessage({
      type: 'success',
      text: response?.message || fallbackMessage
    });
  }

  private handleActionError(error: unknown, timeoutMessage: string): void {
    if (error instanceof Error && error.name === 'TimeoutError') {
      this.closeDeleteDialog();
      this.setFeedbackMessage({
        type: 'success',
        text: timeoutMessage
      });
      this.caricaPrenotazioniSilenzioso();
      return;
    }

    this.showError(this.createErrorMessage(error));
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

  private setLoading(value: boolean): void {
    this.isLoading = value;
    this.changeDetector.detectChanges();
  }

  private createErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'Operazione non riuscita. Riprova tra poco.';
    }

    if (error.status === 0) {
      return 'Backend non raggiungibile su localhost:8080.';
    }

    if (error.status === 401) {
      return 'Sessione non valida: effettua il login e riprova.';
    }

    if (error.status === 403) {
      return 'Profilo non abilitato alla gestione delle prenotazioni.';
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (error.error?.message) {
      return error.error.message;
    }

    return 'Operazione non riuscita. Riprova tra poco.';
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
