import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReportService, DailyReport } from '../../services/report.service';
import { AuthService } from '../../services';

interface ChartBar { label: string; value: number; height: number; color: string; }
interface PieSlice { name: string; value: number; percent: number; color: string; dashOffset: number; }

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  report: DailyReport | null = null;
  selectedDate = new Date().toISOString().split('T')[0];
  isLoading = false;
  error: string | null = null;

  hourBars: ChartBar[] = [];
  categorySlices: PieSlice[] = [];
  popularList: { name: string; count: number }[] = [];
  maxPopular = 1;

  readonly COLORS = ['#B8A9D4','#D4A030','#5A9E5A','#D86A6A','#7EC8D8','#E8A87C','#A8D89E','#C8A8D4'];
  readonly CIRCUMFERENCE = 2 * Math.PI * 40;

  constructor(
    private reportService: ReportService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin) { this.router.navigate(['/dashboard']); return; }
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.error = null;
    this.reportService.getDailyReport(this.selectedDate).subscribe({
      next: (r) => {
        this.report = r;
        this.buildHourBars(r.ordersByHour);
        this.buildCategoryPie(r.salesByCategory);
        this.buildPopularList(r.popularItems);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Не удалось загрузить отчёт';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private buildHourBars(ordersByHour: { [h: number]: number }): void {
    const entries = Object.entries(ordersByHour).map(([h, v]) => ({ h: +h, v }));
    const max = Math.max(...entries.map(e => e.v), 1);
    this.hourBars = entries
      .sort((a, b) => a.h - b.h)
      .map((e, i) => ({
        label: `${e.h}:00`,
        value: e.v,
        height: Math.round((e.v / max) * 80),
        color: this.COLORS[i % this.COLORS.length]
      }));
  }

  private buildCategoryPie(salesByCategory: { [c: string]: number }): void {
    const entries = Object.entries(salesByCategory);
    const total = entries.reduce((s, [, v]) => s + v, 0) || 1;
    let offset = 0;
    this.categorySlices = entries.map(([name, value], i) => {
      const percent = value / total;
      const dash = percent * this.CIRCUMFERENCE;
      const slice: PieSlice = {
        name,
        value,
        percent: Math.round(percent * 100),
        color: this.COLORS[i % this.COLORS.length],
        dashOffset: -offset * this.CIRCUMFERENCE
      };
      offset += percent;
      return slice;
    });
  }

  private buildPopularList(popularItems: { [name: string]: number }): void {
    this.popularList = Object.entries(popularItems)
      .map(([name, count]) => ({ name, count }))
      .slice(0, 8);
    this.maxPopular = Math.max(...this.popularList.map(p => p.count), 1);
  }

  barWidth(): number {
    return this.hourBars.length > 0 ? Math.floor(280 / this.hourBars.length) - 2 : 20;
  }

  goBack(): void { this.router.navigate(['/dashboard']); }
}
