import { HttpInterceptorFn } from '@angular/common/http';

const TOKEN_KEYS = ['accessToken', 'token', 'jwt'];

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const token = TOKEN_KEYS
    .map(key => localStorage.getItem(key) || sessionStorage.getItem(key))
    .find(Boolean);

  if (!token) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  }));
};
