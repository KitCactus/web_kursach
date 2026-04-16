import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
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
    private router: Router
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
          this.isLoading = false;
          this.router.navigate(['/dashboard']);
        },
        error: () => {
          this.errorMessage = 'Неверный логин или пароль';
          this.isLoading = false;
        }
      });
  }
}
