import { Routes } from '@angular/router';

// Base application routes. Adjust component paths as needed for your project.
export const appRoutes: Routes = [
  // default route redirects to /home
  { path: '', pathMatch: 'full', redirectTo: 'home' },

  // home route (uses a standalone component or adjust to loadChildren/loadComponent per project)
  {
    path: 'home',
    loadComponent: () => import('./home/sidebar-multi-demo.component').then(m => m.SidebarMultiDemoComponent),
  },

];

export default appRoutes;
