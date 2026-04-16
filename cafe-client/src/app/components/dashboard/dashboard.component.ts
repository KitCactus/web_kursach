import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  currentUser: any;
  todayRevenue = 0;
  todayOrders = 0;
  pendingOrders = 0;
  isLoading = true;

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser;
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    forkJoin({
      revenue: this.orderService.getTodayRevenue(),
      todayOrders: this.orderService.getTodayOrders(),
      pendingOrders: this.orderService.getOrdersByStatus('PENDING')
    }).subscribe({
      next: (data) => {
        this.todayRevenue = data.revenue || 0;
        this.todayOrders = data.todayOrders.length;
        this.pendingOrders = data.pendingOrders.length;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Ошибка загрузки данных дашборда:', err);
        this.isLoading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateToOrders(): void {
    this.router.navigate(['/orders']);
  }

  navigateToMenu(): void {
    this.router.navigate(['/menu']);  // Исправлено: раньше был баг /staff/menu
  }

  navigateToStaff(): void {
    if (this.authService.isAdmin) {
      this.router.navigate(['/admin/staff']);
    }
  }

  navigateToReports(): void {
    if (this.authService.isAdmin) {
      this.router.navigate(['/admin/reports']);
    }
  }

  navigateToSchedule(): void {
    if (this.authService.isAdmin) {
      this.router.navigate(['/admin/schedule']);
    }
  }

  navigateToProfile(): void {
    this.router.navigate(['/profile']);
  }
}
