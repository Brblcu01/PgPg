import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthApiService } from '../../core/auth-api.service';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './auth-callback.component.html',
  styleUrl: './auth-callback.component.css'
})
export class AuthCallbackComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthApiService);

  protected errorMessage: string | null = null;

  ngOnInit(): void {
    const encryptedCode = this.route.snapshot.queryParamMap.get('state');

    if (!encryptedCode) {
      this.errorMessage = 'Token di login mancante nella callback.';
      return;
    }

    this.authApi.loadUserInfo(encryptedCode).subscribe({
      next: userInfo => {
        this.authApi.saveSession(userInfo);
        void this.router.navigateByUrl('/prenotazioni');
      },
      error: error => this.errorMessage = this.createErrorMessage(error)
    });
  }

  protected goToLogin(): void {
    void this.router.navigateByUrl('/login');
  }

  private createErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) {
      return 'Errore durante il completamento del login.';
    }

    if (error.status === 0) {
      return 'Backend non raggiungibile su localhost:8080.';
    }

    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    return 'Non è stato possibile completare il login Microsoft.';
  }
}
