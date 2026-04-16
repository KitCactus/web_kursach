import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User } from '../interfaces';
import { environment } from '../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  // Маппинг: бэкенд возвращает fullName, разбиваем на firstName/lastName для UI
  private mapUser(u: any): User {
    const parts = (u.fullName || '').split(' ');
    return {
      ...u,
      fullName: u.fullName || '',
      firstName: parts[0] || u.username,
      lastName: parts.slice(1).join(' ')
    };
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<any[]>(this.API_URL).pipe(
      map(users => users.map(u => this.mapUser(u)))
    );
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<any>(`${this.API_URL}/${id}`).pipe(
      map(u => this.mapUser(u))
    );
  }

  createUser(user: any, role: 'USER' | 'ADMIN'): Observable<User> {
    // Объединяем firstName + lastName → fullName для бэкенда
    const payload = {
      ...user,
      fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username
    };
    return this.http.post<any>(`${this.API_URL}?role=${role}`, payload).pipe(
      map(u => this.mapUser(u))
    );
  }

  updateUser(id: number, user: Partial<User>): Observable<User> {
    const payload = {
      ...user,
      fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.fullName
    };
    return this.http.put<any>(`${this.API_URL}/${id}`, payload).pipe(
      map(u => this.mapUser(u))
    );
  }

  updateUserRole(id: number, role: 'ADMIN' | 'USER'): Observable<void> {
    if (!role || !['ADMIN', 'USER'].includes(role)) {
      throw new Error(`Invalid role: ${role}`);
    }
    return this.http.patch<void>(`${this.API_URL}/${id}/role?role=${role}`, {});
  }

  updateUserPassword(id: number, newPassword: string): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/password?newPassword=${newPassword}`, {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
