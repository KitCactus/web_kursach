import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DailyReport {
  date: string;
  totalRevenue: number;
  ordersCount: number;
  averageCheck: number;
  popularItems: { [name: string]: number };
  salesByCategory: { [category: string]: number };
  ordersByHour: { [hour: number]: number };
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly API_URL = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = sessionStorage.getItem('authToken');
    return new HttpHeaders({ 'Authorization': `Basic ${token}` });
  }

  getDailyReport(date: string): Observable<DailyReport> {
    return this.http.get<DailyReport>(`${this.API_URL}/daily?date=${date}`, { headers: this.getAuthHeaders() });
  }

  getPopularItems(): Observable<{ [name: string]: number }> {
    return this.http.get<{ [name: string]: number }>(`${this.API_URL}/popular-items`, { headers: this.getAuthHeaders() });
  }

  getSalesByCategory(): Observable<{ [category: string]: number }> {
    return this.http.get<{ [category: string]: number }>(`${this.API_URL}/sales-by-category`, { headers: this.getAuthHeaders() });
  }
}
