import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../interfaces';
import { UserService, AuthService } from '../../services';

interface ScheduleItem {
  id: number;
  userId: number;
  userName: string;
  date: string;
  startTime: string;
  endTime: string;
  shiftType: 'MORNING' | 'EVENING' | 'NIGHT';
  hourlyRate: number;
  hoursWorked: number;
  dailyEarnings: number;
}

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './schedule.component.html',
  styleUrl: './schedule.component.css'
})
export class ScheduleComponent implements OnInit {
  users: User[] = [];
  scheduleItems: ScheduleItem[] = [];
  selectedWeek = new Date();
  isLoading = false;
  
  isCreateModalOpen = false;
  isEditModalOpen = false;
  selectedItem: ScheduleItem | null = null;
  
  newScheduleItem: Partial<ScheduleItem> = {
    userId: 0,
    date: '',
    startTime: '',
    endTime: '',
    shiftType: 'MORNING',
    hourlyRate: 0
  };

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin) {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.loadUsers();
    this.loadSchedule();
  }

  loadUsers(): void {
    this.userService.getAllUsers()
      .subscribe({
        next: (users) => {
          this.users = users.filter(user => user.isActive && user.role === 'USER');
        },
        error: (error) => {
          console.error('Error loading users:', error);
        }
      });
  }

  loadSchedule(): void {
    this.isLoading = true;
    // TODO: Implement API call to load schedule
    // For now, using mock data
    this.scheduleItems = [
      {
        id: 1,
        userId: 1,
        userName: 'Иван Петров',
        date: this.formatDate(new Date()),
        startTime: '09:00',
        endTime: '17:00',
        shiftType: 'MORNING',
        hourlyRate: 500,
        hoursWorked: 8,
        dailyEarnings: 4000
      }
    ];
    this.isLoading = false;
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
    this.newScheduleItem = {
      userId: 0,
      date: this.formatDate(new Date()),
      startTime: '',
      endTime: '',
      shiftType: 'MORNING',
      hourlyRate: 500
    };
  }

  closeCreateModal(): void {
    this.isCreateModalOpen = false;
  }

  openEditModal(item: ScheduleItem): void {
    this.selectedItem = { ...item };
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.selectedItem = null;
  }

  createScheduleItem(): void {
    if (!this.newScheduleItem.userId || !this.newScheduleItem.date || 
        !this.newScheduleItem.startTime || !this.newScheduleItem.endTime) {
      return;
    }

    const hours = this.calculateHours(
      this.newScheduleItem.startTime!,
      this.newScheduleItem.endTime!
    );
    
    const user = this.users.find(u => u.id === this.newScheduleItem.userId);
    
    const scheduleItem: ScheduleItem = {
      id: Date.now(),
      userId: this.newScheduleItem.userId!,
      userName: user ? `${user.firstName} ${user.lastName}` : '',
      date: this.newScheduleItem.date!,
      startTime: this.newScheduleItem.startTime!,
      endTime: this.newScheduleItem.endTime!,
      shiftType: this.newScheduleItem.shiftType!,
      hourlyRate: this.newScheduleItem.hourlyRate!,
      hoursWorked: hours,
      dailyEarnings: hours * this.newScheduleItem.hourlyRate!
    };

    // TODO: Save to API
    this.scheduleItems.push(scheduleItem);
    this.closeCreateModal();
  }

  updateScheduleItem(): void {
    if (!this.selectedItem) return;

    const hours = this.calculateHours(
      this.selectedItem.startTime,
      this.selectedItem.endTime
    );
    
    this.selectedItem.hoursWorked = hours;
    this.selectedItem.dailyEarnings = hours * this.selectedItem.hourlyRate;

    // TODO: Update in API
    const index = this.scheduleItems.findIndex(item => item.id === this.selectedItem!.id);
    if (index !== -1) {
      this.scheduleItems[index] = this.selectedItem;
    }
    
    this.closeEditModal();
  }

  deleteScheduleItem(item: ScheduleItem): void {
    if (confirm(`Вы уверены, что хотите удалить смену ${item.userName}?`)) {
      // TODO: Delete from API
      this.scheduleItems = this.scheduleItems.filter(i => i.id !== item.id);
    }
  }

  calculateHours(startTime: string, endTime: string): number {
    const start = new Date(`2000-01-01T${startTime}`);
    const end = new Date(`2000-01-01T${endTime}`);
    const diff = end.getTime() - start.getTime();
    return Math.max(0, diff / (1000 * 60 * 60));
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  getShiftTypeName(shiftType: string): string {
    const types: { [key: string]: string } = {
      'MORNING': 'Утренняя',
      'EVENING': 'Вечерняя',
      'NIGHT': 'Ночная'
    };
    return types[shiftType] || shiftType;
  }

  getWeeklyTotal(): number {
    return this.scheduleItems.reduce((total, item) => total + item.dailyEarnings, 0);
  }

  getMonthlyTotal(): number {
    return this.getWeeklyTotal() * 4; // Approximation
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
