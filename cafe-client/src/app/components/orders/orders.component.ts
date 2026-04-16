import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Order, OrderStatusUpdate } from '../../interfaces';
import { OrderService } from '../../services';

type OrderStatus = Order['status'];

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  searchTerm = '';
  selectedStatus = '';
  statuses: OrderStatus[] = ['PENDING', 'IN_PROGRESS', 'PAID', 'COMPLETED', 'CANCELLED'];
  isLoading = false;
  todayRevenue = 0;
  errorMessage: string | null = null;

  constructor(
    private orderService: OrderService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrders();
    this.loadTodayRevenue();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.orderService.getAllOrders()
      .subscribe({
        next: (orders) => {
          this.orders = orders.sort((a, b) => {
            const dateA = new Date(b.orderDate || b.createdAt || '').getTime();
            const dateB = new Date(a.orderDate || a.createdAt || '').getTime();
            return dateA - dateB;
          });
          this.filteredOrders = this.orders;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading orders:', error);
          this.errorMessage = 'Не удалось загрузить заказы. Проверьте подключение к серверу.';
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  loadTodayRevenue(): void {
    this.orderService.getTodayRevenue()
      .subscribe({
        next: (revenue) => {
          this.todayRevenue = revenue || 0;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading revenue:', error);
          this.cdr.detectChanges();
        }
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

    const statusUpdate: OrderStatusUpdate = { status: newStatus };
    this.orderService.updateOrderStatus(order.id, statusUpdate)
      .subscribe({
        next: () => {},
        error: (error) => {
          order.status = oldStatus;
          console.error('Error updating order status:', error);
          this.errorMessage = 'Не удалось изменить статус заказа. Попробуйте снова.';
        }
      });
  }

  getStatusDisplayName(status: string): string {
    const statusNames: { [key: string]: string } = {
      'PENDING': 'Ожидает',
      'IN_PROGRESS': 'В работе',
      'PAID': 'Оплачен',
      'COMPLETED': 'Завершён',
      'CANCELLED': 'Отменён'
    };
    return statusNames[status] || status;
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'PENDING': '#D4A030',
      'IN_PROGRESS': '#B8A9D4',
      'PAID': '#5A9E5A',
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
}
