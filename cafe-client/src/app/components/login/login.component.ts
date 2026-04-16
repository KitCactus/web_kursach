import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService, AuthRequest } from '../../services';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  credentials: AuthRequest = {
    username: '',
    password: ''
  };

  isLoading = false;
  errorMessage = '';
  fieldErrors: { username?: string; password?: string } = {};
  submitted = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  onSubmit(): void {
    this.submitted = true;
    this.fieldErrors = {};
    this.errorMessage = '';

    if (!this.credentials.username) {
      this.fieldErrors.username = 'Введите логин';
    }
    if (!this.credentials.password) {
      this.fieldErrors.password = 'Введите пароль';
    }
    if (this.fieldErrors.username || this.fieldErrors.password) {
      return;
    }

    this.isLoading = true;

    this.authService.login(this.credentials.username, this.credentials.password)
      .subscribe({
        next: () => {
          console.log('✓ Login успешен, редирект на /dashboard');
          this.isLoading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (err: HttpErrorResponse) => {
          console.log('✗ Login ошибка:', err.status, err.message, err);
          this.isLoading = false;
          if (err.status === 0) {
            this.errorMessage = 'Сервер недоступен. Проверьте подключение.';
          } else if (err.status === 401 || err.status === 403) {
            this.errorMessage = 'Неверный логин или пароль';
          } else {
            this.errorMessage = 'Ошибка при входе. Попробуйте снова.';
          }
          this.cdr.detectChanges();
          console.log('📢 Ошибка показана:', this.errorMessage);
        }
      });
  }
}
