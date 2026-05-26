import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Order } from '../../interfaces';
import { OrderService, WebSocketService } from '../../services';
import { PricePipe } from '../../pipes/price.pipe';

type OrderStatus = Order['status'];

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, PricePipe],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit, OnDestroy {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  searchTerm = '';
  selectedStatus = '';
  statuses: OrderStatus[] = ['PENDING', 'IN_PROGRESS', 'PAID', 'COMPLETED', 'CANCELLED'];
  isLoading = false;
  todayRevenue = 0;
  errorMessage: string | null = null;
  private wsSub: Subscription | null = null;

  constructor(
    private orderService: OrderService,
    private wsService: WebSocketService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.loadOrders();
    this.loadTodayRevenue();
    this.wsService.connect();
    this.wsSub = this.wsService.orders$.subscribe(event => {
      this.zone.run(() => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const orderDate = new Date(event.order.orderDate || event.order.createdAt || '');
        orderDate.setHours(0, 0, 0, 0);

        if (orderDate < today) return; // уходит в архив — не показываем

        if (event.type === 'CREATED') {
          this.orders = [event.order, ...this.orders];
          this.loadTodayRevenue();
        } else if (event.type === 'STATUS_CHANGED') {
          const idx = this.orders.findIndex(o => o.id === event.order.id);
          if (idx !== -1) {
            this.orders[idx] = event.order;
            this.orders = [...this.orders]; // триггер change detection
          }
        }

        this.filterOrders();
        this.cdr.detectChanges();
      });
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    this.wsService.disconnect();
  }

  loadOrders(): void {
    this.isLoading = true;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.orders = orders
          .filter(o => {
            const d = new Date(o.orderDate || o.createdAt || '');
            d.setHours(0, 0, 0, 0);
            return d >= today;
          })
          .sort((a, b) =>
            new Date(b.orderDate || b.createdAt || '').getTime() -
            new Date(a.orderDate || a.createdAt || '').getTime()
          );
        this.filterOrders();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Не удалось загрузить заказы. Проверьте подключение к серверу.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadTodayRevenue(): void {
    this.orderService.getTodayRevenue().subscribe({
      next: (revenue) => {
        this.todayRevenue = revenue || 0;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  filterOrders(): void {
    this.filteredOrders = this.orders.filter(order => {
      const orderNum = String(order.id);
      const clientName = order.clientName || '';
      const matchesSearch = orderNum.includes(this.searchTerm) ||
        clientName.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = !this.selectedStatus || order.status === this.selectedStatus;
      return matchesSearch && matchesStatus;
    });
  }

  updateOrderStatus(order: Order, newStatus: OrderStatus): void {
    const oldStatus = order.status;
    order.status = newStatus;
    this.orderService.updateOrderStatus(order.id, { status: newStatus }).subscribe({
      next: () => {},
      error: () => {
        order.status = oldStatus;
        this.errorMessage = 'Не удалось изменить статус заказа. Попробуйте снова.';
      }
    });
  }

  getStatusDisplayName(status: string): string {
    const names: { [key: string]: string } = {
      'PENDING': 'Ожидает',
      'IN_PROGRESS': 'В работе',
      'PAID': 'Готов',
      'COMPLETED': 'Завершён',
      'CANCELLED': 'Отменён'
    };
    return names[status] || status;
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': '#D4A030',
      'IN_PROGRESS': '#B8A9D4',
      'PAID': '#494ba1',
      'COMPLETED': '#5A9E5A',
      'CANCELLED': '#D86A6A'
    };
    return colors[status] || '#6B6B6B';
  }

  canUpdateTo(status: OrderStatus, newStatus: OrderStatus): boolean {
    const transitions: { [key: string]: OrderStatus[] } = {
      'PENDING': ['IN_PROGRESS', 'CANCELLED'],
      'IN_PROGRESS': ['PAID', 'CANCELLED'],
      'PAID': ['COMPLETED'],
      'COMPLETED': [],
      'CANCELLED': []
    };
    return transitions[status]?.includes(newStatus) || false;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  goToArchive(): void {
    this.router.navigate(['/orders/archive']);
  }
}
