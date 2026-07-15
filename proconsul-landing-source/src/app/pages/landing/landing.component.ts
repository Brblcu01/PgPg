import { Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthApiService } from '../../core/auth-api.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent implements OnInit {
  @ViewChild('introVideo') private introVideo?: ElementRef<HTMLVideoElement>;

  private readonly router = inject(Router);
  private readonly authApi = inject(AuthApiService);

  protected readonly videoSrc = 'assets/hero-video.mp4';
  protected readonly backgroundSrc = 'assets/proconsul-background.png';
  protected videoStarted = false;
  protected introComplete = false;
  protected whiteTransition = false;
  protected loginLoading = false;
  protected loginError: string | null = null;
  protected loginFlowActive = false;
  private completingIntro = false;
  private pendingAuthorizationUrl: string | null = null;
  private pendingPrenotazioniNavigation = false;
  private videoFinished = false;

  ngOnInit(): void {
    if (this.authApi.shouldUseLocalhostForLogin()) {
      this.authApi.goToLocalhostLogin();
    }
  }

  protected startLoginVideo(): void {
    const video = this.introVideo?.nativeElement;

    if (!video || this.videoStarted) {
      return;
    }

    this.introComplete = false;
    this.videoStarted = true;
    this.whiteTransition = false;
    this.completingIntro = false;
    this.videoFinished = false;
    video.muted = true;
    video.currentTime = 0;
    video.play()
      .catch(() => {
        video.controls = true;
        this.videoStarted = false;
      });
  }

  protected loginWithMicrosoft(): void {
    this.authApi.clearSession();
    this.startLoginVideo();
    this.loginLoading = true;
    this.loginError = null;
    this.loginFlowActive = true;
    this.pendingAuthorizationUrl = null;
    this.pendingPrenotazioniNavigation = false;

    this.authApi.loginWithMicrosoft()
      .subscribe({
        next: response => {
          this.loginLoading = false;

          if (response.authorizationUrl) {
            this.pendingAuthorizationUrl = response.authorizationUrl;
            this.completeLoginAfterVideo();
            return;
          }

          if (response.accessToken) {
            this.authApi.saveSession(response);
            this.pendingPrenotazioniNavigation = true;
            this.completeLoginAfterVideo();
            return;
          }

          this.loginFlowActive = false;
          this.loginError = 'Risposta di login non valida.';
        },
        error: error => {
          this.loginLoading = false;
          this.loginFlowActive = false;
          this.videoStarted = false;
          this.pendingAuthorizationUrl = null;
          this.pendingPrenotazioniNavigation = false;
          this.loginError = this.createLoginError(error);
        }
      });
  }

  protected get loginTransform(): string {
    return 'translate3d(0, 0, 0) scale(1)';
  }

  protected onIntroTimeUpdate(video: HTMLVideoElement): void {
    if (!Number.isFinite(video.duration) || this.introComplete || this.completingIntro) {
      return;
    }

    const remaining = video.duration - video.currentTime;
    this.whiteTransition = remaining <= 0.25;

    if (video.ended || remaining <= 0.08) {
      this.onIntroEnded();
    }
  }

  protected onIntroEnded(): void {
    if (this.completingIntro) {
      return;
    }

    this.completingIntro = true;
    this.whiteTransition = true;
    window.setTimeout(() => {
      this.introComplete = true;
      this.videoStarted = false;
      this.videoFinished = true;
      this.completingIntro = false;
      this.introVideo?.nativeElement.pause();
      this.completeLoginAfterVideo();
    }, 120);
  }

  private completeLoginAfterVideo(): void {
    if (!this.videoFinished) {
      return;
    }

    if (this.pendingAuthorizationUrl) {
      window.location.href = this.pendingAuthorizationUrl;
      return;
    }

    if (this.pendingPrenotazioniNavigation) {
      void this.router.navigateByUrl('/prenotazioni');
    }
  }

  private createLoginError(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'Errore durante il login Microsoft.';
    }

    if (error.status === 0) {
      return 'Backend non raggiungibile su localhost:8080.';
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    return 'Login Microsoft non riuscito.';
  }
}
