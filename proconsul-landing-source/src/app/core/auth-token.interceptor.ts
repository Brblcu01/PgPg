import { HttpInterceptorFn } from '@angular/common/http';

const TOKEN_KEYS = ['accessToken', 'token', 'jwt'];
const PUBLIC_AUTH_PATHS = ['/login', '/callback', '/userInfo'];

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const requestUrl = new URL(request.url, window.location.origin);
  const isPublicAuthRequest = PUBLIC_AUTH_PATHS.includes(requestUrl.pathname);

  if (isPublicAuthRequest) {
    return next(request);
  }

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
