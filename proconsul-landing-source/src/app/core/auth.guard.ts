import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('accessToken')
    || localStorage.getItem('token')
    || localStorage.getItem('jwt')
    || sessionStorage.getItem('accessToken')
    || sessionStorage.getItem('token')
    || sessionStorage.getItem('jwt');

  if (token) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
