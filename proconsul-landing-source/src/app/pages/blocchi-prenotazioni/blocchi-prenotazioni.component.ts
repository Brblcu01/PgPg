import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, timeout } from 'rxjs/operators';
import { ButtonModule } from 'primeng/button';

import { AuthApiService, UserInfo } from '../../core/auth-api.service';
import {
  BloccoPrenotazioniDTO,
  MessageResponse,
  PrenotazioniApiService
} from '../prenotazioni/prenotazioni-api.service';

interface FeedbackMessage {
  type: 'success' | 'error';
  text: string;
}

@Component({
  selector: 'app-blocchi-prenotazioni',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule],
  templateUrl: './blocchi-prenotazioni.component.html',
  styleUrl: './blocchi-prenotazioni.component.css'
})
export class BlocchiPrenotazioniComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthApiService);
  private readonly prenotazioniApi = inject(PrenotazioniApiService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  currentUser: UserInfo | null = this.authApi.getCurrentUserInfo();
  blocchi: BloccoPrenotazioniDTO[] = [];
  isLoading = false;
  isSaving = false;
  feedbackMessage: FeedbackMessage | null = null;
  private feedbackTimeoutId?: number;

  filtro = {
    dataDa: this.toIsoDate(new Date()),
    dataA: this.toIsoDate(new Date())
  };

  nuovoBlocco = {
    dataInizio: this.toIsoDate(new Date()),
    dataFine: this.toIsoDate(new Date()),
    motivo: ''
  };

  get isAdminUser(): boolean {
    const profileCode = this.currentUser?.profileCode?.toUpperCase();
    const roleCode = this.currentUser?.roleCode?.toUpperCase();

    return profileCode === 'ADMIN' || roleCode === 'HR';
  }

  get activeBlocksCount(): number {
    return this.blocchi.length;
  }

  ngOnInit(): void {
    if (!this.isAdminUser) {
      void this.router.navigateByUrl('/prenotazioni');
      return;
    }

    this.caricaBlocchi();
  }

  goToLogin(): void {
    this.authApi.clearSession();
    void this.router.navigateByUrl('/login');
  }

  caricaBlocchi(showSuccess = false): void {
    if (!this.isAdminUser) {
      return;
    }

    this.setLoading(true);
    this.feedbackMessage = null;

    this.prenotazioniApi.trovaBlocchiPrenotazioni(undefined, this.filtro.dataDa, this.filtro.dataA)
      .pipe(finalize(() => this.setLoading(false)))
      .subscribe({
        next: blocchi => {
          this.blocchi = blocchi;
          this.changeDetector.detectChanges();
          if (showSuccess) {
            this.setFeedbackMessage({
              type: 'success',
              text: 'Lista blocchi aggiornata correttamente.'
            });
          }
        },
        error: error => {
          this.blocchi = [];
          this.showError(this.createErrorMessage(error));
        }
      });
  }

  creaBlocco(): void {
    if (this.isSaving) {
      return;
    }

    const motivo = this.nuovoBlocco.motivo.trim();

    if (!this.nuovoBlocco.dataInizio || !this.nuovoBlocco.dataFine) {
      this.showError('Indica data inizio e data fine del blocco.');
      return;
    }

    if (this.nuovoBlocco.dataFine < this.nuovoBlocco.dataInizio) {
      this.showError('La data fine non puo essere precedente alla data inizio.');
      return;
    }

    if (!motivo) {
      this.showError('Inserisci il motivo del blocco.');
      return;
    }

    this.isSaving = true;
    this.feedbackMessage = null;

    this.prenotazioniApi.creaBloccoPrenotazioni({
      dataInizio: this.nuovoBlocco.dataInizio,
      dataFine: this.nuovoBlocco.dataFine,
      motivo
    })
      .pipe(
        timeout(15000),
        finalize(() => this.isSaving = false)
      )
      .subscribe({
        next: response => {
          this.showSuccessFromResponse(response, 'Blocco prenotazioni creato correttamente.');
          this.resetForm();
          this.caricaBlocchiSilenzioso();
        },
        error: error => this.handleActionError(error, 'Blocco prenotazioni inviato. Aggiorno la lista.')
      });
  }

  eliminaBlocco(idBlocco: number): void {
    if (this.isSaving) {
      return;
    }

    this.isSaving = true;
    this.feedbackMessage = null;

    this.prenotazioniApi.eliminaBloccoPrenotazioni(idBlocco)
      .pipe(
        timeout(15000),
        finalize(() => this.isSaving = false)
      )
      .subscribe({
        next: response => {
          this.showSuccessFromResponse(response, 'Blocco prenotazioni eliminato correttamente.');
          this.caricaBlocchiSilenzioso();
        },
        error: error => this.handleActionError(error, 'Eliminazione blocco inviata. Aggiorno la lista.')
      });
  }

  formatItalianDate(value: string): string {
    const date = new Date(`${value}T00:00:00`);
    const formatted = new Intl.DateTimeFormat('it-IT', {
      weekday: 'short',
      day: 'numeric',
      month: 'long',
      year: 'numeric'
    }).format(date);

    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  }

  private caricaBlocchiSilenzioso(): void {
    this.prenotazioniApi.trovaBlocchiPrenotazioni(undefined, this.filtro.dataDa, this.filtro.dataA)
      .subscribe({
        next: blocchi => {
          this.blocchi = blocchi;
          this.changeDetector.detectChanges();
        },
        error: error => this.showError(this.createErrorMessage(error))
      });
  }

  private setLoading(value: boolean): void {
    this.isLoading = value;
    this.changeDetector.detectChanges();
  }

  private resetForm(): void {
    this.nuovoBlocco = {
      dataInizio: this.nuovoBlocco.dataInizio,
      dataFine: this.nuovoBlocco.dataFine,
      motivo: ''
    };
  }

  private showSuccessFromResponse(response: MessageResponse | null | undefined, fallbackMessage: string): void {
    this.setFeedbackMessage({
      type: 'success',
      text: response?.message || fallbackMessage
    });
  }

  private handleActionError(error: unknown, timeoutMessage: string): void {
    if (error instanceof Error && error.name === 'TimeoutError') {
      this.setFeedbackMessage({
        type: 'success',
        text: timeoutMessage
      });
      this.caricaBlocchiSilenzioso();
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
      return 'Operazione non riuscita. Riprova tra poco.';
    }

    if (error.status === 0) {
      return 'Backend non raggiungibile su localhost:8080.';
    }

    if (error.status === 401) {
      return 'Sessione non valida: effettua il login e riprova.';
    }

    if (error.status === 403) {
      return 'Profilo non abilitato alla gestione dei blocchi.';
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
