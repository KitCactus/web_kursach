import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '../../interfaces';
import { UserService, AuthService } from '../../services';

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
  createErrors: { username?: string; password?: string; firstName?: string; phone?: string } = {};

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

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAllUsers()
      .subscribe({
        next: (users) => {
          this.users = users;
          this.filteredUsers = users;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error loading users:', error);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  filterUsers(): void {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch = user.username.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                         (user.fullName || '').toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesRole = !this.selectedRole || user.role === this.selectedRole;
      return matchesSearch && matchesRole;
    });
  }

  openCreateModal(): void {
    this.isCreateModalOpen = true;
    this.createErrors = {};
    this.newUser = {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      role: 'USER',
      isActive: true
    };
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
    else if (!/^\d+$/.test(this.newUser.phone.replace(/[\s\-()]/g, ''))) this.createErrors.phone = 'Телефон должен содержать только цифры';

    if (this.createErrors.username || this.createErrors.password || this.createErrors.firstName || this.createErrors.phone) return;

    const userData = { ...this.newUser };
    this.closeCreateModal();
    this.userService.createUser(userData, userData.role)
      .subscribe({
        next: (created) => {
          this.users.push(created);
          this.filterUsers();
          this.newUser = { username: '', password: '', firstName: '', lastName: '', email: '', phone: '', role: 'USER', isActive: true };
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('Error creating user:', error);
          this.cdr.detectChanges();
        }
      });
  }

  updateUser(): void {
    if (!this.selectedUser) return;

    const { password, ...userWithoutPassword } = this.selectedUser;
    const userData = { ...userWithoutPassword };
    const oldUser = this.users.find(u => u.id === userData.id);
    this.closeEditModal();

    // Сразу обновляем в списке
    const idx = this.users.findIndex(u => u.id === userData.id);
    if (idx >= 0) {
      this.users[idx] = userData;
      this.filterUsers();
    }

    this.userService.updateUser(userData.id, userData)
      .subscribe({
        next: () => { /* уже обновили выше */ },
        error: (error) => {
          // Откатываем
          if (idx >= 0 && oldUser) {
            this.users[idx] = oldUser;
            this.filterUsers();
          }
          console.error('Error updating user:', error);
        }
      });
  }

  deleteUser(user: User): void {
    if (confirm(`Вы уверены, что хотите удалить "${user.fullName || user.username}"?`)) {
      // Сразу удаляем из списка
      const oldUsers = this.users;
      this.users = this.users.filter(u => u.id !== user.id);
      this.filterUsers();
      // Отправляем запрос
      this.userService.deleteUser(user.id)
        .subscribe({
          next: () => { /* уже удалили выше */ },
          error: (error) => {
            // Откатываем
            this.users = oldUsers;
            this.filterUsers();
            console.error('Error deleting user:', error);
          }
        });
    }
  }

  resetPassword(user: User): void {
    const newPassword = prompt('Введите новый пароль:');
    if (newPassword) {
      this.userService.updateUserPassword(user.id, newPassword)
        .subscribe({
          next: () => {
            alert('Пароль успешно изменен');
          },
          error: (error) => {
            console.error('Error updating password:', error);
            alert('Ошибка при изменении пароля');
          }
        });
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
