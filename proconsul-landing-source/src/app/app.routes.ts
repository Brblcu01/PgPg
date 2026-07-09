import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/landing/landing.component').then(
        (m) => m.LandingComponent
      )
  },
  {
    path: 'home',
    loadComponent: () =>
      import('./layout/app-shell/app-shell.component').then(
        (m) => m.AppShellComponent
      ),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          )
      },
      {
        path: 'prenotazioni',
        loadComponent: () =>
          import('./pages/prenotazioni/prenotazioni.component').then(
            (m) => m.PrenotazioniComponent
          )
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
