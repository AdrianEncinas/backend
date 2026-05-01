import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { ApiService } from '../../services/api.service';
import { PortfolioDashboard, PortfolioPointDTO, StockPositionDTO, Asset, TickerSearchDTO, StockFullDTO, ChartDTO } from '../../models/interfaces';
import { Chart, registerables } from 'chart.js';
import { TimeoutError, timeout } from 'rxjs';
import { debounceTime, distinctUntilChanged, finalize, switchMap } from 'rxjs/operators';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('portfolioChart') chartCanvas!: ElementRef<HTMLCanvasElement>;
  private chart: Chart | null = null;
  private readonly requestTimeoutMs = 10000;

  dashboard: PortfolioDashboard | null = null;
  graphData: PortfolioPointDTO[] = [];
  loading = true;
  graphLoading = false;
  error = '';
  toast = '';
  toastType: 'success' | 'error' = 'success';

  readonly stockChartPeriods = [
    { label: '1D', value: '1d' },
    { label: '1S', value: '1wk' },
    { label: '1M', value: '1mo' },
    { label: '3M', value: '3mo' },
    { label: '1A', value: '1y' },
    { label: '5A', value: '5y' },
  ];

  selectedPeriod = '1d';
  graphMode = 'intraday';
  periods = [
    { label: '1D', value: '1d', mode: 'intraday' },
    { label: '1S', value: '1wk', mode: 'intraday' },
    { label: '1M', value: '1mo', mode: 'intraday' },
    { label: '1A', value: '1y',  mode: 'intraday' },
  ];

  // Add position modal
  showAddModal = false;
  addForm: StockPositionDTO = { ticker: '', companyName: '', shares: 0, avg_price: 0 };
  addLoading = false;
  addError = '';
  addSearchQuery = '';
  addSearchResults: TickerSearchDTO[] = [];
  addSearching = false;
  addSearchSelected = false;
  addSelectedStock: StockFullDTO | null = null;
  addStockLoading = false;
  addStockError = '';
  addChartData: ChartDTO | null = null;
  addChartLoading = false;
  addSelectedChartPeriod = '1mo';
  addChartPeriods = this.stockChartPeriods;
  private addSearchSubject = new Subject<string>();

  // Position detail modal
  showPositionDetailModal = false;
  detailTargetAsset: Asset | null = null;
  detailStock: StockFullDTO | null = null;
  detailLoading = false;
  detailError = '';
  detailChartData: ChartDTO | null = null;
  detailChartLoading = false;
  detailSelectedChartPeriod = '1mo';
  detailChartPeriods = this.stockChartPeriods;

  // Edit position modal
  showEditModal = false;
  editForm: StockPositionDTO = { ticker: '', companyName: '', shares: 0, avg_price: 0 };
  editLoading = false;

  // Delete confirm
  showDeleteConfirm = false;
  deleteTarget: Asset | null = null;
  deleteLoading = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadDashboard();
    this.addSearchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap((q) => {
        this.addSearching = true;
        return this.api.searchTickers(q).pipe(
          finalize(() => {
            this.addSearching = false;
            this.cdr.detectChanges();
          })
        );
      })
    ).subscribe({
      next: (res) => { this.addSearchResults = res; },
      error: () => { this.addSearchResults = []; },
    });
  }

  ngAfterViewInit(): void {
    // chart will be initialized after data loads
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
    this.addSearchSubject.complete();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = '';
    this.api.getDashboard().pipe(timeout(this.requestTimeoutMs)).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
        this.cdr.detectChanges();
        this.loadGraph(this.graphMode, this.selectedPeriod);
      },
      error: (err: unknown) => {
        this.error = this.getDashboardErrorMessage(err);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadGraph(mode: string, period: string): void {
    this.graphLoading = true;
    this.api.getPortfolioGraph(mode, period).pipe(
      timeout(this.requestTimeoutMs),
      finalize(() => {
        this.graphLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (data) => {
        this.graphData = data;
        this.renderChart(data);
      },
      error: () => {},
    });
  }

  selectPeriod(p: { label: string; value: string; mode: string }): void {
    this.selectedPeriod = p.value;
    this.graphMode = p.mode;
    this.loadGraph(p.mode, p.value);
  }

  renderChart(data: PortfolioPointDTO[]): void {
    if (!this.chartCanvas) return;
    this.chart?.destroy();

    const labels = data.map((p) => p.date ?? p.time ?? p.label ?? '');
    const values = data.map((p) => Number(p.totalValue ?? p.portfolioValue ?? 0));

    const isPositive = values.length > 1 ? values[values.length - 1] >= values[0] : true;
    const color = isPositive ? '#10B981' : '#EF4444';
    const colorAlpha = isPositive ? 'rgba(16,185,129,0.15)' : 'rgba(239,68,68,0.15)';

    const ctx = this.chartCanvas.nativeElement.getContext('2d')!;
    const gradient = ctx.createLinearGradient(0, 0, 0, 280);
    gradient.addColorStop(0, isPositive ? 'rgba(16,185,129,0.35)' : 'rgba(239,68,68,0.35)');
    gradient.addColorStop(1, 'rgba(0,0,0,0)');

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            data: values,
            borderColor: color,
            borderWidth: 2,
            backgroundColor: gradient,
            fill: true,
            tension: 0.4,
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: color,
          },
        ],
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
              label: (ctx) => ` $${Number(ctx.raw).toLocaleString('en-US', { minimumFractionDigits: 2 })}`,
            },
          },
        },
        scales: {
          x: {
            grid: { color: 'rgba(51,65,85,0.4)', drawTicks: false },
            border: { display: false },
            ticks: {
              color: '#64748B',
              font: { size: 11 },
              maxTicksLimit: 6,
              maxRotation: 0,
            },
          },
          y: {
            grid: { color: 'rgba(51,65,85,0.4)', drawTicks: false },
            border: { display: false },
            ticks: {
              color: '#64748B',
              font: { size: 11 },
              callback: (v) => `$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 0 })}`,
            },
          },
        },
      },
    });
  }

  // ─── Add Position ──────────────────────────────────────────────
  openAddModal(): void {
    this.addForm = { ticker: '', companyName: '', shares: 0, avg_price: 0 };
    this.addError = '';
    this.addSearchQuery = '';
    this.addSearchResults = [];
    this.addSearching = false;
    this.addSearchSelected = false;
    this.addSelectedStock = null;
    this.addStockLoading = false;
    this.addStockError = '';
    this.addChartData = null;
    this.addChartLoading = false;
    this.addSelectedChartPeriod = '1mo';
    this.showAddModal = true;
  }

  onAddSearch(): void {
    if (this.addSearchQuery.length >= 1) {
      this.addSearchSubject.next(this.addSearchQuery);
    } else {
      this.addSearchResults = [];
    }
  }

  selectAddStock(r: TickerSearchDTO): void {
    this.addForm.ticker = r.symbol;
    this.addForm.companyName = r.name;
    this.addSearchQuery = `${r.symbol} — ${r.name}`;
    this.addSearchResults = [];
    this.addSearchSelected = true;
    this.loadAddStockDetails(r.symbol);
  }

  clearAddSearch(): void {
    this.addSearchQuery = '';
    this.addSearchResults = [];
    this.addSearchSelected = false;
    this.addSelectedStock = null;
    this.addStockLoading = false;
    this.addStockError = '';
    this.addChartData = null;
    this.addChartLoading = false;
    this.addSelectedChartPeriod = '1mo';
    this.addForm.ticker = '';
    this.addForm.companyName = '';
    this.addForm.shares = 0;
    this.addForm.avg_price = 0;
    this.addError = '';
  }

  loadAddStockDetails(ticker: string): void {
    this.addStockLoading = true;
    this.addStockError = '';
    this.addSelectedStock = null;
    this.addChartData = null;
    this.api.getStockDetails(ticker).pipe(
      finalize(() => {
        this.addStockLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (stock) => {
        this.addSelectedStock = stock;
        this.loadAddChart(ticker, this.addSelectedChartPeriod);
      },
      error: () => {
        this.addStockError = `No se encontraron datos para "${ticker}".`;
      },
    });
  }

  loadAddChart(ticker: string, period: string): void {
    this.addChartLoading = true;
    this.api.getStockChart(ticker, period).pipe(
      finalize(() => {
        this.addChartLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (data) => {
        this.addChartData = data;
      },
      error: () => {},
    });
  }

  selectAddChartPeriod(period: string): void {
    this.addSelectedChartPeriod = period;
    if (this.addSelectedStock) {
      this.loadAddChart(this.addSelectedStock.ticker, period);
    }
  }

  get addChartChangePct(): number | null {
    const history = this.addChartData?.history;
    if (!history || history.length < 2) return null;
    const first = Number(history[0].price);
    const last = Number(history[history.length - 1].price);
    if (first === 0) return null;
    return (last - first) / first;
  }

  submitAdd(): void {
    if (!this.addForm.ticker || !this.addForm.companyName || !this.addForm.shares || !this.addForm.avg_price) {
      this.addError = 'Todos los campos son obligatorios.';
      return;
    }
    this.addLoading = true;
    this.addError = '';
    this.api.addPosition(this.addForm).pipe(
      finalize(() => {
        this.addLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.showAddModal = false;
        this.showToast('Posición añadida correctamente', 'success');
        setTimeout(() => this.loadDashboard());
      },
      error: (err) => {
        this.addError = err?.error || 'Error al añadir la posición.';
      },
    });
  }

  // ─── Position Detail ──────────────────────────────────────────
  openPositionDetail(asset: Asset): void {
    this.showPositionDetailModal = true;
    this.detailTargetAsset = asset;
    this.detailStock = null;
    this.detailError = '';
    this.detailChartData = null;
    this.detailChartLoading = false;
    this.detailSelectedChartPeriod = '1mo';
    this.loadPositionDetail(asset.ticker);
  }

  closePositionDetail(): void {
    this.showPositionDetailModal = false;
    this.detailTargetAsset = null;
    this.detailStock = null;
    this.detailError = '';
    this.detailChartData = null;
    this.detailChartLoading = false;
    this.detailSelectedChartPeriod = '1mo';
  }

  loadPositionDetail(ticker: string): void {
    this.detailLoading = true;
    this.detailError = '';
    this.api.getStockDetails(ticker).pipe(
      finalize(() => {
        this.detailLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (stock) => {
        this.detailStock = stock;
        this.loadPositionDetailChart(stock.ticker, this.detailSelectedChartPeriod);
      },
      error: () => {
        this.detailError = `No se encontraron datos para "${ticker}".`;
      },
    });
  }

  loadPositionDetailChart(ticker: string, period: string): void {
    this.detailChartLoading = true;
    this.api.getStockChart(ticker, period).pipe(
      finalize(() => {
        this.detailChartLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (data) => {
        this.detailChartData = data;
      },
      error: () => {},
    });
  }

  selectDetailChartPeriod(period: string): void {
    this.detailSelectedChartPeriod = period;
    if (this.detailStock) {
      this.loadPositionDetailChart(this.detailStock.ticker, period);
    }
  }

  get detailChartChangePct(): number | null {
    const history = this.detailChartData?.history;
    if (!history || history.length < 2) return null;
    const first = Number(history[0].price);
    const last = Number(history[history.length - 1].price);
    if (first === 0) return null;
    return (last - first) / first;
  }

  // ─── Edit Position ─────────────────────────────────────────────
  openEditModal(asset: Asset): void {
    this.editForm = {
      ticker: asset.ticker,
      companyName: asset.company_name,
      shares: asset.shares,
      avg_price: 0,
    };
    this.showEditModal = true;
  }

  submitEdit(): void {
    this.editLoading = true;
    this.api.modifyPosition(this.editForm).pipe(
      finalize(() => {
        this.editLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.showEditModal = false;
        this.showToast('Posición actualizada', 'success');
        setTimeout(() => this.loadDashboard());
      },
      error: (err) => {
        this.showToast(err?.error || 'Error al modificar.', 'error');
      },
    });
  }

  // ─── Delete Position ───────────────────────────────────────────
  confirmDelete(asset: Asset): void {
    this.deleteTarget = asset;
    this.showDeleteConfirm = true;
  }

  submitDelete(): void {
    if (!this.deleteTarget) return;
    this.deleteLoading = true;
    this.api.deletePosition(this.deleteTarget.ticker).pipe(
      finalize(() => {
        this.deleteLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.deleteTarget = null;
        this.showToast('Posición eliminada', 'success');
        setTimeout(() => this.loadDashboard());
      },
      error: () => {
        this.showToast('Error al eliminar.', 'error');
      },
    });
  }

  // ─── Helpers ───────────────────────────────────────────────────
  showToast(msg: string, type: 'success' | 'error'): void {
    this.toast = msg;
    this.toastType = type;
    setTimeout(() => (this.toast = ''), 3000);
  }

  formatCurrency(v: number, currency = 'USD'): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: currency || 'USD', minimumFractionDigits: 2 }).format(v ?? 0);
  }

  private getDashboardErrorMessage(err: unknown): string {
    if (err instanceof TimeoutError) {
      return 'El portfolio ha tardado demasiado en responder. Revisa el backend y vuelve a intentarlo.';
    }

    if (err instanceof HttpErrorResponse && err.status === 401) {
      return 'La sesion ha expirado. Vuelve a iniciar sesion.';
    }

    return 'Error cargando el portfolio. Asegurate de que el backend esta activo.';
  }

  get totalValue(): number {
    return this.dashboard?.summary?.total_value ?? 0;
  }

  get totalGainLoss(): number {
    return this.dashboard?.summary?.total_gain_loss ?? 0;
  }

  get currency(): string {
    return this.dashboard?.summary?.currency ?? 'USD';
  }

  get sortedAssets(): Asset[] {
    const assets = this.dashboard?.assets ?? [];
    return [...assets].sort((a, b) => Number(b.value_user ?? 0) - Number(a.value_user ?? 0));
  }
 
  get graphReturnAbsolute(): number | null {
    if (!this.graphData || this.graphData.length < 2) return null;
    const first = Number(this.graphData[0].totalValue ?? this.graphData[0].portfolioValue ?? 0);
    const last = Number(this.graphData[this.graphData.length - 1].totalValue ?? this.graphData[this.graphData.length - 1].portfolioValue ?? 0);
    if (!Number.isFinite(first) || !Number.isFinite(last)) return null;
    return last - first;
  }

  get graphReturnPct(): number | null {
    if (!this.graphData || this.graphData.length < 2) return null;
    const first = Number(this.graphData[0].totalValue ?? this.graphData[0].portfolioValue ?? 0);
    const last = Number(this.graphData[this.graphData.length - 1].totalValue ?? this.graphData[this.graphData.length - 1].portfolioValue ?? 0);
    if (!Number.isFinite(first) || !Number.isFinite(last) || first === 0) return null;
    return (last - first) / first;
  }
}
