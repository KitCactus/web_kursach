import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { PricePipe } from '../../pipes/price.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, PricePipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: any;
  todayRevenue = 0;
  todayOrders = 0;
  pendingOrders = 0;
  isLoading = true;
  currentTime = '';
  currentDate = '';
  private timeInterval: any;

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUser;
    this.loadDashboardData();
    this.startClock();
  }

  private startClock(): void {
    this.updateTime();
    this.timeInterval = setInterval(() => {
      this.updateTime();
      this.cdr.markForCheck();
    }, 1000);
  }

  private updateTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });

    const day = now.getDate();
    const monthNames = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
                       'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'];
    const weekdayNames = ['воскресенье', 'понедельник', 'вторник', 'среда', 'четверг', 'пятница', 'суббота'];

    const month = monthNames[now.getMonth()];
    const weekday = weekdayNames[now.getDay()];
    const year = now.getFullYear();

    this.currentDate = `${day} ${month}, ${weekday} ${year}`;
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

  ngOnDestroy(): void {
    if (this.timeInterval) {
      clearInterval(this.timeInterval);
    }
  }
}
