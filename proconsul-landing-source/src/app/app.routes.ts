import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/landing/landing.component').then(
        (m) => m.LandingComponent
      )
  },
  {
    path: '',
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
