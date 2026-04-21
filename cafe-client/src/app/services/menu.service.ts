import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MenuItem, CreateMenuItemRequest, UpdateMenuItemRequest } from '../interfaces';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private readonly API_URL = `${environment.apiUrl}/menu`;

  constructor(private http: HttpClient) {}

  getAllMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(this.API_URL);
  }

  getPublicMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.API_URL}/bot`);
  }

  getMenuItemById(id: number): Observable<MenuItem> {
    return this.http.get<MenuItem>(`${this.API_URL}/${id}`);
  }

  createMenuItem(item: CreateMenuItemRequest, userId: number): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.API_URL}?userId=${userId}`, item);
  }

  updateMenuItem(id: number, item: UpdateMenuItemRequest): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${this.API_URL}/${id}`, item);
  }

  toggleAvailability(id: number, available: boolean): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/availability?available=${available}`, {});
  }

  toggleVisibility(id: number, hidden: boolean): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/visibility?hidden=${hidden}`, {});
  }

  deleteMenuItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  uploadPhoto(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.API_URL}/upload`, formData, {
      responseType: 'text'
    });
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/categories/all`);
  }

  getSubcategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/subcategories/all`);
  }

  getSubcategoriesByCategory(category: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/subcategories/by-category?category=${category}`);
  }
}