import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Order } from '../../interfaces';
import { OrderService } from '../../services';
import { PricePipe } from '../../pipes/price.pipe';

type OrderStatus = Order['status'];

@Component({
  selector: 'app-order-archive',
  standalone: true,
  imports: [CommonModule, FormsModule, PricePipe],
  templateUrl: './order-archive.component.html',
  styleUrl: './order-archive.component.css'
})
export class OrderArchiveComponent implements OnInit {
  orders: Order[] = [];
  filteredOrders: Order[] = [];
  searchTerm = '';
  selectedStatus = '';
  selectedDate = '';
  statuses: OrderStatus[] = ['PENDING', 'IN_PROGRESS', 'PAID', 'COMPLETED', 'CANCELLED'];
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private orderService: OrderService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.loadArchive();
  }

  loadArchive(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        this.orders = orders
          .filter(o => {
            const d = new Date(o.orderDate || o.createdAt || '');
            d.setHours(0, 0, 0, 0);
            return d < today;
          })
          .sort((a, b) => {
            const da = new Date(b.orderDate || b.createdAt || '').getTime();
            const db = new Date(a.orderDate || a.createdAt || '').getTime();
            return da - db;
          });

        this.filterOrders();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Не удалось загрузить архив. Проверьте подключение к серверу.';
        this.isLoading = false;
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

      let matchesDate = true;
      if (this.selectedDate) {
        const orderDate = new Date(order.orderDate || order.createdAt || '');
        const filterDate = new Date(this.selectedDate);
        matchesDate =
          orderDate.getFullYear() === filterDate.getFullYear() &&
          orderDate.getMonth() === filterDate.getMonth() &&
          orderDate.getDate() === filterDate.getDate();
      }

      return matchesSearch && matchesStatus && matchesDate;
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

  clearDateFilter(): void {
    this.selectedDate = '';
    this.filterOrders();
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }
}
