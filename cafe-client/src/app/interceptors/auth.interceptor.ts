import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  const isApiRequest = req.url.includes('/api/');

  if (isApiRequest) {
    try {
      const token = sessionStorage.getItem('authToken');
      if (!token) return next(req);

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
