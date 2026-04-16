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
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser && savedUser !== 'undefined') {
      try {
        this.currentUserSubject.next(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('currentUser');
      }
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
        // Маппинг плоского ответа бэкенда в объект User
        const user: User = {
          id: response.id,
          username: response.username,
          fullName: response.fullName,
          role: response.role,
          password: password  // сохраняем для Basic Auth заголовков
        };
        return user;
      }),
      tap(user => {
        localStorage.setItem('authToken', btoa(`${username}:${password}`));
        this.setCurrentUser(user);
      })
    );
  }

  getCurrentUser(username: string): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me?username=${username}`);
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('authToken');
    this.currentUserSubject.next(null);
  }

  private setCurrentUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  createAuthHeaders(username: string, password?: string): HttpHeaders {
    const pwd = password || this.currentUser?.password || '';
    const encodedAuth = btoa(`${username}:${pwd}`);
    return new HttpHeaders({
      'Authorization': `Basic ${encodedAuth}`,
      'Content-Type': 'application/json'
    });
  }

  getAuthHeaders(): HttpHeaders {
    const currentUser = this.currentUser;
    if (!currentUser) {
      throw new Error('No authenticated user found');
    }
    return this.createAuthHeaders(currentUser.username, currentUser.password);
  }
}
