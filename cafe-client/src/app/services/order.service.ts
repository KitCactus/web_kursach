import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, CreateOrderRequest, OrderStatusUpdate } from '../interfaces';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.API_URL, { headers: this.getAuthHeaders() });
  }

  getOrdersByStatus(status: Order['status']): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API_URL}/status/${status}`, { headers: this.getAuthHeaders() });
  }

  getTodayOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API_URL}/today`, { headers: this.getAuthHeaders() });
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.API_URL}/${id}`, { headers: this.getAuthHeaders() });
  }

  // Бэкенд возвращает просто число (BigDecimal), не объект
  getTodayRevenue(): Observable<number> {
    return this.http.get<number>(`${this.API_URL}/today/revenue`, { headers: this.getAuthHeaders() });
  }

  createOrder(order: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.API_URL, order, { headers: this.getAuthHeaders() });
  }

  updateOrderStatus(id: number, status: OrderStatusUpdate): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/status?status=${status.status}`, {}, { headers: this.getAuthHeaders() });
  }
}
