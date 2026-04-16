import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MenuItem, CreateMenuItemRequest, UpdateMenuItemRequest } from '../interfaces';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private readonly API_URL = `${environment.apiUrl}/menu`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(this.API_URL, { headers: this.getAuthHeaders() });
  }

  getPublicMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.API_URL}/bot`);
  }

  getMenuItemById(id: number): Observable<MenuItem> {
    return this.http.get<MenuItem>(`${this.API_URL}/${id}`, { headers: this.getAuthHeaders() });
  }

  createMenuItem(item: CreateMenuItemRequest, userId: number): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.API_URL}?userId=${userId}`, item, { headers: this.getAuthHeaders() });
  }

  updateMenuItem(id: number, item: UpdateMenuItemRequest): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${this.API_URL}/${id}`, item, { headers: this.getAuthHeaders() });
  }

  toggleAvailability(id: number, available: boolean): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/availability?available=${available}`, {}, { headers: this.getAuthHeaders() });
  }

  toggleVisibility(id: number, hidden: boolean): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/visibility?hidden=${hidden}`, {}, { headers: this.getAuthHeaders() });
  }

  deleteMenuItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`, { headers: this.getAuthHeaders() });
  }

  uploadPhoto(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    const headers = new HttpHeaders({
      'Authorization': `Basic ${localStorage.getItem('authToken')}`
    });
    return this.http.post(`${this.API_URL}/upload`, formData, {
      headers,
      responseType: 'text'
    });
  }
}