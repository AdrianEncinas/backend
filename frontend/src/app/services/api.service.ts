import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PortfolioDashboard,
  PortfolioPointDTO,
  StockFullDTO,
  StockPositionDTO,
  ChartDTO,
  TickerSearchDTO,
  WatchlistDTO,
  WatchlistAddRequest,
} from '../models/interfaces';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly BASE = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) {}

  // ─── Portfolio ──────────────────────────────────────────────────

  getDashboard(): Observable<PortfolioDashboard> {
    return this.http.get<PortfolioDashboard>(`${this.BASE}/portfolio/dashboard`);
  }

  getPortfolioGraph(mode = 'historic', period = '1mo'): Observable<PortfolioPointDTO[]> {
    const params = new HttpParams().set('mode', mode).set('period', period);
    return this.http.get<PortfolioPointDTO[]>(`${this.BASE}/portfolio/graph`, { params });
  }

  getStockDetails(ticker: string): Observable<StockFullDTO> {
    return this.http.get<StockFullDTO>(`${this.BASE}/portfolio/stocks/${ticker}`);
  }

  addPosition(position: StockPositionDTO): Observable<string> {
    return this.http.post(`${this.BASE}/portfolio/positions`, position, { responseType: 'text' });
  }

  modifyPosition(position: StockPositionDTO): Observable<string> {
    return this.http.put(`${this.BASE}/portfolio/positions`, position, { responseType: 'text' });
  }

  deletePosition(ticker: string): Observable<string> {
    return this.http.delete(`${this.BASE}/portfolio/positions/${ticker}`, { responseType: 'text' });
  }

  syncHolding(holdingId: number, totalShares: number, avgPrice: number): Observable<void> {
    return this.http.patch<void>(`${this.BASE}/portfolio/holdings/${holdingId}`, {
      totalShares,
      avgPrice,
    });
  }

  // ─── Chart ──────────────────────────────────────────────────────

  getStockChart(ticker: string, period = '1mo'): Observable<ChartDTO> {
    const params = new HttpParams().set('period', period);
    return this.http.get<ChartDTO>(`${this.BASE}/chart/${ticker}`, { params });
  }

  // ─── Market ──────────────────────────────────────────────────────

  searchTickers(query: string): Observable<TickerSearchDTO[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<TickerSearchDTO[]>(`${this.BASE}/market/search`, { params });
  }

  getWatchlist(): Observable<WatchlistDTO[]> {
    return this.http.get<WatchlistDTO[]>(`${this.BASE}/market/watchlist`);
  }

  addToWatchlist(request: WatchlistAddRequest): Observable<WatchlistDTO> {
    return this.http.post<WatchlistDTO>(`${this.BASE}/market/watchlist`, request);
  }

  deleteFromWatchlist(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/market/watchlist/${id}`);
  }
}
