import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface LoginResponse {
  authorizationUrl?: string;
  accessToken?: string;
  refreshToken?: string;
}

export interface UserInfo {
  idUser?: number;
  email?: string;
  name?: string;
  accessToken?: string;
  refreshToken?: string;
  authMethod?: string;
  roleCode?: string;
  profileCode?: string;
  idProfile?: number;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080';

  shouldUseLocalhostForLogin(): boolean {
    return window.location.hostname === '127.0.0.1';
  }

  goToLocalhostLogin(): void {
    window.location.href = `${window.location.protocol}//localhost:${window.location.port}/login`;
  }

  loginWithMicrosoft(): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, {
      authMethod: 'Azure'
    });
  }

  loadUserInfo(code: string): Observable<UserInfo> {
    return this.http.post<UserInfo>(`${this.baseUrl}/userInfo`, code, {
      headers: new HttpHeaders({ 'Content-Type': 'text/plain' })
    });
  }

  saveSession(userInfo: UserInfo): void {
    if (userInfo.accessToken) {
      localStorage.setItem('accessToken', userInfo.accessToken);
    }

    if (userInfo.refreshToken) {
      localStorage.setItem('refreshToken', userInfo.refreshToken);
    }

    localStorage.setItem('userInfo', JSON.stringify(userInfo));
  }

  getCurrentUserInfo(): UserInfo | null {
    const rawUserInfo = localStorage.getItem('userInfo');

    if (!rawUserInfo) {
      return null;
    }

    try {
      return JSON.parse(rawUserInfo) as UserInfo;
    } catch {
      return null;
    }
  }

  clearSession(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userInfo');
  }
}
