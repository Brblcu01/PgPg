import { Routes } from '@angular/router';

import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/landing/landing.component').then(
        (m) => m.LandingComponent
      )
  },
  {
    path: 'callback',
    loadComponent: () =>
      import('./pages/auth-callback/auth-callback.component').then(
        (m) => m.AuthCallbackComponent
      )
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./layout/app-shell/app-shell.component').then(
        (m) => m.AppShellComponent
      ),
    children: [
      {
        path: '',
        redirectTo: 'prenotazioni',
        pathMatch: 'full'
      },
      {
        path: 'prenotazioni',
        loadComponent: () =>
          import('./pages/prenotazioni/prenotazioni.component').then(
            (m) => m.PrenotazioniComponent
          )
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./pages/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          )
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'prenotazioni'
  }
];
