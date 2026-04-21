import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
  if (req.url.includes('/auth/login')) {
    console.log('[AUTH] Skipping auth interceptor for login endpoint:', req.url);
    return next(req);
  }

  const isApiRequest = req.url.includes('/api/');
  console.log('[AUTH] Request URL:', req.url, '| Is API request:', isApiRequest);

  if (isApiRequest) {
    try {
      const token = sessionStorage.getItem('authToken');
      console.log('[AUTH] Token found:', !!token);
      if (!token) {
        console.log('[AUTH] No token, passing request without authorization');
        return next(req);
      }

      const isMultipart = req.body instanceof FormData;
      let headers = req.headers.set('Authorization', `Basic ${token}`);
      if (!isMultipart) {
        headers = headers.set('Content-Type', 'application/json');
      }

      console.log('[AUTH] Adding Authorization header with token');
      return next(req.clone({ headers }));
    } catch (error) {
      console.error('[AUTH] Error in interceptor:', error);
      return next(req);
    }
  }
  console.log('[AUTH] Not an API request, skipping interceptor');
  return next(req);
};
