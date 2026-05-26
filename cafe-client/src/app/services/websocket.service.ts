import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject, Observable } from 'rxjs';
import { Order } from '../interfaces';

export interface OrderEvent {
  type: 'CREATED' | 'STATUS_CHANGED';
  order: Order;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private client: Client;
  private orderEvents$ = new Subject<OrderEvent>();

  constructor() {
    const wsUrl = `ws://${window.location.host}/ws/websocket`;
    this.client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        this.client.subscribe('/topic/orders', (msg: IMessage) => {
          const event: OrderEvent = JSON.parse(msg.body);
          this.orderEvents$.next(event);
        });
      }
    });
  }

  connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
    }
  }

  get orders$(): Observable<OrderEvent> {
    return this.orderEvents$.asObservable();
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}
