import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DailyReport, PopularItem, SalesByCategory } from '../../services/report.service';
import { ReportService, AuthService } from '../../services';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  dailyReport: DailyReport | null = null;
  popularItems: PopularItem[] = [];
  salesByCategory: SalesByCategory[] = [];
  selectedDate = new Date().toISOString().split('T')[0];
  isLoading = false;

  constructor(
    private reportService: ReportService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin) {
      this.router.navigate(['/dashboard']);
      return;
    }
    this.loadReports();
  }

  loadReports(): void {
    this.isLoading = true;
    
    // Load daily report
    this.reportService.getDailyReport(this.selectedDate)
      .subscribe({
        next: (report) => {
          this.dailyReport = report;
        },
        error: (error) => {
          console.error('Error loading daily report:', error);
        }
      });

    // Load popular items
    this.reportService.getPopularItems()
      .subscribe({
        next: (items) => {
          this.popularItems = items;
        },
        error: (error) => {
          console.error('Error loading popular items:', error);
        }
      });

    // Load sales by category
    this.reportService.getSalesByCategory()
      .subscribe({
        next: (sales) => {
          this.salesByCategory = sales;
        },
        error: (error) => {
          console.error('Error loading sales by category:', error);
        }
      });

    this.isLoading = false;
  }

  onDateChange(): void {
    this.loadReports();
  }

  getCategoryDisplayName(category: string): string {
    const categoryNames: { [key: string]: string } = {
      'COFFEE': 'Кофе',
      'TEA': 'Чай',
      'DESSERT': 'Десерты',
      'SANDWICH': 'Сэндвичи',
      'SNACK': 'Закуски',
      'OTHER': 'Другое'
    };
    return categoryNames[category] || category;
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }
}
