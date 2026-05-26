import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription, forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { WebSocketService } from '../../services/websocket.service';
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
  private wsSub: Subscription | null = null;
  private userSub: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private wsService: WebSocketService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.userSub = this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.cdr.markForCheck();
    });
    this.loadDashboardData();
    this.startClock();

    this.wsService.connect();
    this.wsSub = this.wsService.orders$.subscribe(event => {
      this.zone.run(() => {
        if (event.type === 'CREATED') {
          this.todayOrders++;
          if (event.order.status === 'PENDING') this.pendingOrders++;
          this.loadRevenue();
        } else if (event.type === 'STATUS_CHANGED') {
          const prev = event.order.status;
          // Пересчитываем ожидающие заказы при изменении статуса
          this.refreshPendingCount();
          if (prev === 'COMPLETED') this.loadRevenue();
        }
        this.cdr.markForCheck();
      });
    });
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
    const monthNames = ['января', 'февраля', 'марта', 'апреля', 'мая', 'июня',
                       'июля', 'августа', 'сентября', 'октября', 'ноября', 'декабря'];
    const weekdayNames = ['воскресенье', 'понедельник', 'вторник', 'среда', 'четверг', 'пятница', 'суббота'];
    const day = now.getDate();
    this.currentDate = `${day} ${monthNames[now.getMonth()]}, ${weekdayNames[now.getDay()]} ${now.getFullYear()}`;
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
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  private loadRevenue(): void {
    this.orderService.getTodayRevenue().subscribe(r => {
      this.todayRevenue = r || 0;
      this.cdr.markForCheck();
    });
  }

  private refreshPendingCount(): void {
    this.orderService.getOrdersByStatus('PENDING').subscribe(orders => {
      this.pendingOrders = orders.length;
      this.cdr.markForCheck();
    });
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  navigateToOrders(): void { this.router.navigate(['/orders']); }
  navigateToMenu(): void { this.router.navigate(['/menu']); }
  navigateToStaff(): void {
    if (this.authService.isAdmin) this.router.navigate(['/admin/staff']);
  }
  navigateToReports(): void {
    if (this.authService.isAdmin) this.router.navigate(['/admin/reports']);
  }

  ngOnDestroy(): void {
    if (this.timeInterval) clearInterval(this.timeInterval);
    this.wsSub?.unsubscribe();
    this.userSub?.unsubscribe();
    this.wsService.disconnect();
  }
}
