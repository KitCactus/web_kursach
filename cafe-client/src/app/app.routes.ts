import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { MenuComponent } from './components/menu/menu.component';
import { OrdersComponent } from './components/orders/orders.component';
import { StaffManagementComponent } from './components/staff-management/staff-management.component';
import { ReportsComponent } from './components/reports/reports.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/role.guard';
import { loginRedirectGuard } from './guards/login-redirect.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [loginRedirectGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'menu', component: MenuComponent, canActivate: [authGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [authGuard] },
  { path: 'admin/staff', component: StaffManagementComponent, canActivate: [authGuard, adminGuard] },
  { path: 'admin/reports', component: ReportsComponent, canActivate: [authGuard, adminGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];