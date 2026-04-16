import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../interfaces';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  isLoading = false;
  isSaving = false;

  editData = { firstName: '', lastName: '', email: '', phone: '' };
  editErrors: { firstName?: string; email?: string } = {};

  showPasswordForm = false;
  passwordData = { newPassword: '', confirmPassword: '' };
  passwordErrors: { newPassword?: string; confirmPassword?: string } = {};

  successMessage = '';
  errorMessage = '';

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.userService.getOwnProfile().subscribe({
      next: (user) => {
        this.user = user;
        this.editData = {
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          email: user.email || '',
          phone: user.phone || ''
        };
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Не удалось загрузить профиль';
      }
    });
  }

  saveProfile(): void {
    this.editErrors = {};
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.editData.firstName?.trim()) {
      this.editErrors.firstName = 'Введите имя';
      return;
    }

    const fullName = `${this.editData.firstName} ${this.editData.lastName}`.trim();
    this.isSaving = true;

    this.userService.updateOwnProfile({
      fullName,
      email: this.editData.email,
      phone: this.editData.phone
    }).subscribe({
      next: (updated) => {
        this.user = updated;
        this.isSaving = false;
        this.successMessage = 'Профиль успешно обновлён';
        // Обновляем имя в текущей сессии
        if (this.authService.currentUser) {
          const current = { ...this.authService.currentUser, fullName };
          localStorage.setItem('currentUser', JSON.stringify(current));
        }
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Ошибка при сохранении профиля';
      }
    });
  }

  savePassword(): void {
    this.passwordErrors = {};
    this.successMessage = '';
    this.errorMessage = '';

    if (!this.passwordData.newPassword) {
      this.passwordErrors.newPassword = 'Введите новый пароль';
      return;
    }
    if (this.passwordData.newPassword.length < 6) {
      this.passwordErrors.newPassword = 'Пароль должен быть не менее 6 символов';
      return;
    }
    if (this.passwordData.newPassword !== this.passwordData.confirmPassword) {
      this.passwordErrors.confirmPassword = 'Пароли не совпадают';
      return;
    }

    this.isSaving = true;
    this.userService.changeOwnPassword(this.passwordData.newPassword).subscribe({
      next: () => {
        this.isSaving = false;
        this.successMessage = 'Пароль успешно изменён. Войдите заново.';
        this.passwordData = { newPassword: '', confirmPassword: '' };
        this.showPasswordForm = false;
        // После смены пароля нужно перелогиниться
        setTimeout(() => {
          this.authService.logout();
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: () => {
        this.isSaving = false;
        this.errorMessage = 'Ошибка при смене пароля';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
