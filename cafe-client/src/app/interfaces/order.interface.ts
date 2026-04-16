export interface Order {
  id: number;
  orderNumber?: string;   // необязательно — бэкенд не возвращает
  clientId?: number;
  clientName?: string;    // имя клиента из Telegram
  orderDate?: string;     // дата заказа
  items: OrderItem[];
  totalAmount: number;
  status: 'PENDING' | 'IN_PROGRESS' | 'PAID' | 'COMPLETED' | 'CANCELLED';
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  completedAt?: string;
}

export interface OrderItem {
  id: number;
  menuItemId: number;
  menuItemName: string;
  quantity: number;
  price: number;
  category?: string;
  notes?: string;
}

export interface CreateOrderRequest {
  items: {
    menuItemId: number;
    quantity: number;
    notes?: string;
  }[];
  clientName?: string;
  notes?: string;
}

export interface OrderStatusUpdate {
  status: Order['status'];
}
