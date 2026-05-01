import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, AfterViewInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { ChartDTO, StockFullDTO } from '../../models/interfaces';

Chart.register(...registerables);

@Component({
  selector: 'app-stock-details',
  standalone: false,
  templateUrl: './stock-details.html',
  styleUrl: './stock-details.scss',
})
export class StockDetails implements OnChanges, AfterViewInit, OnDestroy {
  @Input() stock: StockFullDTO | null = null;
  @Input() chartData: ChartDTO | null = null;
  @Input() chartLoading = false;
  @Input() selectedChartPeriod = '1mo';
  @Input() chartPeriods: Array<{ label: string; value: string }> = [];
  @Input() changePct: number | null = null;
  @Input() potentialMode: 'ratio' | 'raw' = 'ratio';

  @Output() chartPeriodChange = new EventEmitter<string>();

  @ViewChild('stockChart') chartCanvas?: ElementRef<HTMLCanvasElement>;
  private chart: Chart | null = null;

  ngAfterViewInit(): void {
    this.tryRenderChart();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chartData'] || changes['stock']) {
      this.tryRenderChart();
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  selectChartPeriod(period: string): void {
    this.chartPeriodChange.emit(period);
  }

  private tryRenderChart(): void {
    if (!this.chartCanvas || !this.chartData) return;

    const labels = this.chartData.history.map((h) => h.time);
    const prices = this.chartData.history.map((h) => Number(h.price));
    const isPos = prices.length > 1 ? prices[prices.length - 1] >= prices[0] : true;
    const color = isPos ? '#10B981' : '#EF4444';

    const ctx = this.chartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    this.chart?.destroy();
    const gradient = ctx.createLinearGradient(0, 0, 0, 220);
    gradient.addColorStop(0, isPos ? 'rgba(16,185,129,0.3)' : 'rgba(239,68,68,0.3)');
    gradient.addColorStop(1, 'rgba(0,0,0,0)');

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          data: prices,
          borderColor: color,
          borderWidth: 2,
          backgroundColor: gradient,
          fill: true,
          tension: 0.3,
          pointRadius: 0,
          pointHoverRadius: 4,
          pointHoverBackgroundColor: color,
        }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { intersect: false, mode: 'index' },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1E293B',
            borderColor: '#334155',
            borderWidth: 1,
            titleColor: '#94A3B8',
            bodyColor: '#F1F5F9',
            callbacks: {
              label: (c) => ` $${Number(c.raw).toFixed(2)}`,
            },
          },
        },
        scales: {
          x: {
            grid: { color: 'rgba(51,65,85,0.4)', drawTicks: false },
            border: { display: false },
            ticks: { color: '#64748B', font: { size: 11 }, maxTicksLimit: 6, maxRotation: 0 },
          },
          y: {
            grid: { color: 'rgba(51,65,85,0.4)', drawTicks: false },
            border: { display: false },
            ticks: { color: '#64748B', font: { size: 11 }, callback: (v) => `$${Number(v).toFixed(0)}` },
          },
        },
      },
    });
  }

  pct(v: number | null | undefined): string {
    if (v == null) return 'N/D';
    return `${(v * 100).toFixed(2)}%`;
  }

  potentialPct(v: number | null | undefined): string {
    if (v == null) return 'N/D';
    if (this.potentialMode === 'raw') return `${Number(v).toFixed(2)}%`;
    return this.pct(v);
  }

  fmt(v: number | null | undefined, decimals = 2): string {
    if (v == null) return 'N/D';
    return Number(v).toFixed(decimals);
  }

  fmtBig(v: number | null | undefined): string {
    if (v == null) return 'N/D';
    if (Math.abs(v) >= 1e9) return `$${(v / 1e9).toFixed(1)}B`;
    if (Math.abs(v) >= 1e6) return `$${(v / 1e6).toFixed(1)}M`;
    return `$${v.toLocaleString()}`;
  }

  getRecoClass(r: string | null | undefined): string {
    if (!r) return '';
    const lower = r.toLowerCase();
    if (lower.includes('buy') || lower.includes('strong')) return 'positive';
    if (lower.includes('sell')) return 'negative';
    return '';
  }
}
