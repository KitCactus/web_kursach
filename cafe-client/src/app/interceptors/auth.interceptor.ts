import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  const authService = inject(AuthService);

  // Не добавляем заголовки к эндпоинту логина — он должен работать без авторизации
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  if (req.url.startsWith('http://localhost:8080')) {
    try {
      const authHeaders = authService.getAuthHeaders();
      const authReq = req.clone({
        headers: authHeaders
      });
      return next(authReq);
    } catch (error) {
      return next(req);
    }
  }
  return next(req);
};
