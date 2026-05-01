import { afterEach, vi } from 'vitest';
import { NEVER, of, throwError } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';
import { Dashboard } from './dashboard';
import { ApiService } from '../../services/api.service';

describe('Dashboard', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  const createApiService = () => ({
    getDashboard: vi.fn<ApiService['getDashboard']>(),
    getPortfolioGraph: vi.fn<ApiService['getPortfolioGraph']>(),
    addPosition: vi.fn<ApiService['addPosition']>(),
    modifyPosition: vi.fn<ApiService['modifyPosition']>(),
    deletePosition: vi.fn<ApiService['deletePosition']>(),
  });

  const createCdr = () => ({
    detectChanges: vi.fn<ChangeDetectorRef['detectChanges']>(),
  });

  it('should create', () => {
    const api = createApiService();
    const cdr = createCdr();
    const component = new Dashboard(api as unknown as ApiService, cdr as unknown as ChangeDetectorRef);

    expect(component).toBeTruthy();
  });

  it('should stop loading and show an error when the dashboard request fails', () => {
    const api = createApiService();
    const cdr = createCdr();
    api.getDashboard.mockReturnValue(throwError(() => new Error('backend down')));
    const component = new Dashboard(api as unknown as ApiService, cdr as unknown as ChangeDetectorRef);

    component.loadDashboard();

    expect(component.loading).toBeFalsy();
    expect(component.error).toContain('Error cargando el portfolio');
  });

  it('should stop loading and show a timeout error when the dashboard request hangs', () => {
    vi.useFakeTimers();
    const api = createApiService();
    const cdr = createCdr();
    api.getDashboard.mockReturnValue(NEVER);
    const component = new Dashboard(api as unknown as ApiService, cdr as unknown as ChangeDetectorRef);

    component.loadDashboard();
    vi.advanceTimersByTime(10001);

    expect(component.loading).toBeFalsy();
    expect(component.error).toContain('ha tardado demasiado');
  });

  it('should load the graph after the dashboard response arrives', () => {
    const api = createApiService();
    const cdr = createCdr();
    api.getDashboard.mockReturnValue(of({
      summary: {
        total_value: 100,
        total_gain_loss: 10,
        total_gain_loss_pct: 10,
        day_change: 5,
        day_change_pct: 5,
        currency: 'USD',
      },
      assets: [],
    }));
    api.getPortfolioGraph.mockReturnValue(of([]));
    const component = new Dashboard(api as unknown as ApiService, cdr as unknown as ChangeDetectorRef);
    component.renderChart = vi.fn();

    component.loadDashboard();

    expect(component.loading).toBeFalsy();
    expect(component.dashboard?.summary.total_value).toBe(100);
    expect(api.getPortfolioGraph).toHaveBeenCalledWith('intraday', '1d');
  });
});
