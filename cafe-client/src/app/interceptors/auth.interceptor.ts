import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpHeaders } from '@angular/common/http';
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
      const token = localStorage.getItem('authToken');
      if (!token) return next(req);

      // Для multipart (загрузка файлов) ставим ТОЛЬКО Authorization —
      // браузер сам выставит Content-Type: multipart/form-data с boundary
      const isMultipart = req.body instanceof FormData;
      let headers = req.headers.set('Authorization', `Basic ${token}`);
      if (!isMultipart) {
        headers = headers.set('Content-Type', 'application/json');
      }

      return next(req.clone({ headers }));
    } catch (error) {
      return next(req);
    }
  }
  return next(req);
};
