// ─── Auth ─────────────────────────────────────────────────────────
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  baseCurrency: string;
}

export interface UserResponse {
  id: number;
  username: string;
  baseCurrency: string;
}

// ─── Portfolio Dashboard ──────────────────────────────────────────
export interface PortfolioSummary {
  total_value: number;
  total_gain_loss: number;
  total_gain_loss_pct: number;
  day_change: number;
  day_change_pct: number;
  currency: string;
}

export interface Asset {
  ticker: string;
  company_name: string;
  logo_url: string;
  shares: number;
  value_user: number;
  gain_all_time_user: number;
  gain_pct: number;
  current_price_user: number;
  currency_native: string;
}

export interface PortfolioDashboard {
  summary: PortfolioSummary;
  assets: Asset[];
}

// ─── Portfolio Graph ──────────────────────────────────────────────
export interface PortfolioPointDTO {
  label: string;
  date: string | null;
  time: string | null;
  timestamp: string | null;
  totalValue: number;
  portfolioValue: number;
  totalCost: number;
  investedCapital: number;
  dayIntraChange: number | null;
  totalChangePercentage: number | null;
}

// ─── Stock ────────────────────────────────────────────────────────
export interface StockPositionDTO {
  ticker: string;
  companyName: string;
  shares: number;
  avg_price: number;
}

export interface HoldingDTO {
  id: number;
  userid: number;
  currency: string;
  ticker: string;
  companyName: string;
  logo_url: string;
  shares: number;
  avg_price: number;
}

export interface StockFullDTO {
  ticker: string;
  longName: string;
  logoUrl: string;
  businessSummary: string;
  currentPrice: number;
  dailyChangePct: number;
  fundamentals: {
    peRatio: number;
    forwardPE: number;
    pegRatio: number;
    enterpriseToEbitda: number;
  };
  metrics: {
    profitability: {
      ebitdaMargins: number;
      operatingMargins: number;
      grossMargins: number;
      returnOnAssets: number;
      returnOnEquity: number;
    };
    growth: {
      revenueGrowth: number;
      earningsGrowth: number;
      freeCashflow: number;
      operatingCashflow: number;
    };
  };
  solvency: {
    debtToEquity: number;
    currentRatio: number;
    quickRatio: number;
    totalCash: number;
    totalDebt: number;
  };
  analysts: {
    recommendation: string;
    numberOfAnalysts: number;
    targetPrice: {
      low: number;
      high: number;
      mean: number;
      median: number;
      current: number;
    };
    upsidePotentialPct: number;
  };
  dividends: {
    yield: number;
    payoutRatio: number;
    lastDividend: number;
  };
}

// ─── Chart ────────────────────────────────────────────────────────
export interface ChartDTO {
  ticker: string;
  period: string;
  history: {
    time: string;
    price: number;
    volume: number;
  }[];
}

// ─── Market / Search ──────────────────────────────────────────────
export interface TickerSearchDTO {
  symbol: string;
  name: string;
  exch: string;
}

// ─── Watchlist ────────────────────────────────────────────────────
export interface WatchlistDTO {
  id: number;
  ticker: string;
  companyName: string;
}

export interface WatchlistAddRequest {
  ticker: string;
  companyName: string;
}
