import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { MenuComponent } from './components/menu/menu.component';
import { OrdersComponent } from './components/orders/orders.component';
import { StaffManagementComponent } from './components/staff-management/staff-management.component';
import { ScheduleComponent } from './components/schedule/schedule.component';
import { ReportsComponent } from './components/reports/reports.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'menu', component: MenuComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [AuthGuard] },
  // Только для админа:
  { path: 'admin/staff', component: StaffManagementComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/schedule', component: ScheduleComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: 'admin/reports', component: ReportsComponent, canActivate: [AuthGuard, AdminGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];