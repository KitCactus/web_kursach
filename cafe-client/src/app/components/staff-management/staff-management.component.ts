import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../interfaces';
import { UserService, AuthService } from '../../services';

interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
}

@Component({
  selector: 'app-staff-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './staff-management.component.html',
  styleUrl: './staff-management.component.css'
})
export class StaffManagementComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  searchTerm = '';
  selectedRole = '';
  roles = ['ADMIN', 'USER'];
  isLoading = false;

  isCreateModalOpen = false;
  isEditModalOpen = false;
  selectedUser: User | null = null;
  createErrors: { username?: string; password?: string; firstName?: string; phone?: string; server?: string } = {};

  toasts: Toast[] = [];
  private toastCounter = 0;

  newUser = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    role: 'USER' as 'ADMIN' | 'USER',
    isActive: true
  };

  get currentUserId(): number | undefined {
    return this.authService.currentUser?.id;
  }

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin) {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.loadUsers();
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    const id = ++this.toastCounter;
    this.toasts.push({ id, message, type });
    this.cdr.detectChanges();
    setTimeout(() => {
      this.toasts = this.toasts.filter(t => t.id !== id);
      this.cdr.detectChanges();
    }, 3500);
  }

  dismissToast(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.filterUsers();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoading = false;
        this.showToast('Не удалось загрузить список сотрудников', 'error');
      }
    });
  }

  filterUsers(): void {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch =
        user.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        (user.fullName || '').toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesRole = !this.selectedRole || user.role === this.selectedRole;
      return matchesSearch && matchesRole;
    });
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
    this.createErrors = {};
    this.newUser = { username: '', password: '', firstName: '', lastName: '', email: '', phone: '', role: 'USER', isActive: true };
  }

  closeCreateModal(): void {
    this.isCreateModalOpen = false;
  }

  openEditModal(user: User): void {
    this.selectedUser = { ...user };
    this.isEditModalOpen = true;
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.selectedUser = null;
  }

  createUser(): void {
    this.createErrors = {};
    if (!this.newUser.username?.trim()) this.createErrors.username = 'Введите логин';
    if (!this.newUser.password) this.createErrors.password = 'Введите пароль';
    if (!this.newUser.firstName?.trim()) this.createErrors.firstName = 'Введите имя';
    if (!this.newUser.phone?.trim()) this.createErrors.phone = 'Введите номер телефона';
    else if (!/^\d+$/.test(this.newUser.phone.replace(/[\s\-()]/g, '')))
      this.createErrors.phone = 'Телефон должен содержать только цифры';

    if (this.createErrors.username || this.createErrors.password || this.createErrors.firstName || this.createErrors.phone) return;

    const userData = {
      ...this.newUser,
      fullName: [this.newUser.firstName, this.newUser.lastName].filter(Boolean).join(' ') || this.newUser.username
    };

    this.userService.createUser(userData, userData.role).subscribe({
      next: (created) => {
        this.users.push(created);
        this.filterUsers();
        this.closeCreateModal();
        this.showToast(`Сотрудник ${created.fullName || created.username} добавлен`, 'success');
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg: string = err?.error?.message || '';
        if (msg.toLowerCase().includes('username already exists') || msg.toLowerCase().includes('already')) {
          this.createErrors = { ...this.createErrors, username: 'Этот логин уже занят' };
        } else {
          this.createErrors = { ...this.createErrors, server: msg || 'Ошибка при создании сотрудника' };
        }
        this.cdr.detectChanges();
      }
    });
  }

  updateUser(): void {
    if (!this.selectedUser) return;

    const { password, ...userWithoutPassword } = this.selectedUser;
    const userData = { ...userWithoutPassword };
    const idx = this.users.findIndex(u => u.id === userData.id);
    const oldUser = idx >= 0 ? { ...this.users[idx] } : null;

    // Optimistic update
    if (idx >= 0) {
      this.users[idx] = userData;
      this.filterUsers();
    }
    this.closeEditModal();

    this.userService.updateUser(userData.id, userData).subscribe({
      next: (updated) => {
        if (idx >= 0) {
          this.users[idx] = updated;
          this.filterUsers();
        }
        if (updated.id === this.currentUserId) {
          this.authService.patchCurrentUser({ fullName: updated.fullName, username: updated.username });
        }
        this.showToast('Данные сотрудника сохранены', 'success');
        this.cdr.detectChanges();
      },
      error: (err) => {
        if (idx >= 0 && oldUser) {
          this.users[idx] = oldUser;
          this.filterUsers();
        }
        this.showToast(err?.error?.message || 'Не удалось сохранить изменения', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Удалить сотрудника "${user.fullName || user.username}"?`)) return;

    const oldUsers = [...this.users];
    this.users = this.users.filter(u => u.id !== user.id);
    this.filterUsers();

    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.showToast(`Сотрудник удалён`, 'success');
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.users = oldUsers;
        this.filterUsers();
        this.showToast(err?.error?.message || 'Не удалось удалить сотрудника', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  resetPassword(user: User): void {
    const newPassword = prompt('Введите новый пароль:');
    if (!newPassword) return;

    this.userService.updateUserPassword(user.id, newPassword).subscribe({
      next: () => this.showToast('Пароль успешно изменён', 'success'),
      error: () => this.showToast('Не удалось изменить пароль', 'error')
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
