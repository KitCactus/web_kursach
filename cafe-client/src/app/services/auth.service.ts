import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { User, AuthResponse } from '../interfaces';
import { environment } from '../../environments/environment';


export interface AuthRequest {
  username: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const savedUser = sessionStorage.getItem('currentUser');
    const savedToken = sessionStorage.getItem('authToken');
    if (savedUser && savedUser !== 'undefined' && savedToken) {
      try {
        this.currentUserSubject.next(JSON.parse(savedUser));
      } catch (e) {
        sessionStorage.removeItem('currentUser');
        sessionStorage.removeItem('authToken');
      }
    } else {
      sessionStorage.removeItem('currentUser');
      sessionStorage.removeItem('authToken');
    }
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  get isStaff(): boolean {
    return this.currentUser?.role === 'USER';
  }

  get isAuthenticated(): boolean {
    return this.currentUser !== null;
  }

  login(username: string, password: string): Observable<User> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, { username, password }).pipe(
      map(response => {
        const user: User = {
          id: response.id,
          username: response.username,
          fullName: response.fullName,
          role: response.role
        };
        return user;
      }),
      tap(user => {
        sessionStorage.setItem('authToken', btoa(`${username}:${password}`));
        this.setCurrentUser(user);
        // Помечаем пользователя активным
        this.http.patch(`${environment.apiUrl}/users/${user.id}/active?active=true`, {},
          { headers: { 'Authorization': `Basic ${btoa(`${username}:${password}`)}`, 'Content-Type': 'application/json' } }
        ).subscribe({ error: () => {} });
      })
    );
  }

  getCurrentUser(username: string): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me?username=${username}`);
  }

  logout(): void {
    const user = this.currentUser;
    const token = sessionStorage.getItem('authToken');
    if (user && token) {
      // Помечаем пользователя неактивным перед выходом
      this.http.patch(`${environment.apiUrl}/users/${user.id}/active?active=false`, {},
        { headers: { 'Authorization': `Basic ${token}`, 'Content-Type': 'application/json' } }
      ).subscribe({ error: () => {} });
    }
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('authToken');
    this.currentUserSubject.next(null);
  }

  private setCurrentUser(user: User): void {
    sessionStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  createAuthHeaders(username: string, password?: string): HttpHeaders {
    if (password) {
      const encodedAuth = btoa(`${username}:${password}`);
      return new HttpHeaders({
        'Authorization': `Basic ${encodedAuth}`,
        'Content-Type': 'application/json'
      });
    }
    const token = sessionStorage.getItem('authToken');
    if (!token) {
      throw new Error('No authentication token available');
    }
    return new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAuthHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('authToken');
    if (!token) {
      throw new Error('No authentication token found');
    }
    return new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });
  }
}
