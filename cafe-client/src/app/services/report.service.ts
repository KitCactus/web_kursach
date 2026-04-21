import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DailyReport {
  date: string;
  totalOrders: number;
  totalRevenue: number;
  ordersByStatus: {
    PENDING: number;
    IN_PROGRESS: number;
    PAID: number;
    COMPLETED: number;
    CANCELLED: number;
  };
  topItems: Array<{
    id: number;
    name: string;
    quantity: number;
    revenue: number;
  }>;
}

export interface PopularItem {
  id: number;
  name: string;
  category: string;
  totalOrders: number;
  totalRevenue: number;
  averageRating?: number;
}

export interface SalesByCategory {
  category: string;
  totalOrders: number;
  totalRevenue: number;
  percentage: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly API_URL = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Basic ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getDailyReport(date: string): Observable<DailyReport> {
    return this.http.get<DailyReport>(`${this.API_URL}/daily?date=${date}`, { headers: this.getAuthHeaders() });
  }

  getPopularItems(): Observable<PopularItem[]> {
    return this.http.get<PopularItem[]>(`${this.API_URL}/popular-items`, { headers: this.getAuthHeaders() });
  }

  getSalesByCategory(): Observable<SalesByCategory[]> {
    return this.http.get<SalesByCategory[]>(`${this.API_URL}/sales-by-category`, { headers: this.getAuthHeaders() });
  }
}
