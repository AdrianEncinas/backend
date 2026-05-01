from fastapi import FastAPI, HTTPException, Body, Query
from fastapi.responses import JSONResponse
import yfinance as yf
import pandas as pd
import numpy as np
from datetime import datetime
from typing import List, Dict, Optional
from urllib.parse import quote
import requests

app = FastAPI(title="Investment Tracker API - Pro")

currency_cache = {}

def sanitize(obj):
    """Replace NaN/Inf float values with None for JSON serialization."""
    import math
    if isinstance(obj, float):
        return None if (math.isnan(obj) or math.isinf(obj)) else obj
    if isinstance(obj, dict):
        return {k: sanitize(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [sanitize(v) for v in obj]
    return obj

def get_exchange_rate(from_curr: str, to_curr: str):
    if not from_curr or not to_curr or from_curr == to_curr:
        return 1.0
    pair = f"{from_curr}{to_curr}=X"
    if pair in currency_cache:
        return currency_cache[pair]
    try:
        ticker = yf.Ticker(pair)
        rate = ticker.fast_info.last_price
        if rate is None or rate == 0:
            hist = ticker.history(period="1d")
            rate = hist['Close'].iloc[-1]
        currency_cache[pair] = rate
        return rate
    except:
        fallbacks = {"USDEUR": 0.92, "EURUSD": 1.08, "USDUSD": 1.0, "EUREUR": 1.0}
        return fallbacks.get(from_curr + to_curr, 1.0)


@app.get("/api/v1/market/search")
def search_ticker(query: str):
    safe_query = quote(query)
    url = f"https://query2.finance.yahoo.com/v1/finance/search?q={safe_query}"
    headers = {'User-Agent': 'Mozilla/5.0'}
    try:
        response = requests.get(url, headers=headers)
        data = response.json()
        results = [
            {
                "symbol": r.get("symbol"), 
                "name": r.get("longname") or r.get("shortname") or r.get("symbol"), 
                "exch": r.get("exchDisp")
            } 
            for r in data.get("quotes", []) if r.get("symbol")
        ]
        return JSONResponse(content=results)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

import yfinance as yf
from typing import List, Dict
from fastapi import Body

@app.post("/api/v1/portfolio/status")
def get_portfolio_status(portfolio: List[Dict] = Body(...)):
    total_value_base = 0
    total_cost_base = 0
    total_prev_value_base = 0
    assets_detail = []
    user_base_currency = portfolio[0].get('currency', 'EUR') if portfolio else 'EUR'

    for item in portfolio:
        ticker_symbol = item.get('ticker', '').upper()
        try:
            t = yf.Ticker(ticker_symbol)
            
            logo_url = None
            try:
                logo_url = t.info.get('logo_url')
            except:
                pass

            if not logo_url:
                logo_url = f"https://www.google.com/s2/favicons?domain={ticker_symbol}.com&sz=128"

            hist = t.history(period="2d")
            if hist.empty: continue
            
            asset_currency = t.fast_info.currency
            current_price_native = hist['Close'].iloc[-1]
            prev_close_native = hist['Close'].iloc[-2] if len(hist) > 1 else current_price_native
            rate = get_exchange_rate(asset_currency, user_base_currency)
            shares = float(item.get('totalShares') or item.get('shares', 0))
            avg_price_user_currency = float(item.get('avgPrice') or item.get('avg_price', 0))
            current_val_base = (current_price_native * shares) * rate
            cost_basis_base = avg_price_user_currency * shares 
            prev_val_base = (prev_close_native * shares) * rate

            assets_detail.append(sanitize({
                "ticker": ticker_symbol,
                "company_name": item.get('companyName', ticker_symbol),
                "logo_url": logo_url,
                "shares": shares,
                "value_user": round(current_val_base, 2),
                "gain_all_time_user": round(current_val_base - cost_basis_base, 2),
                "gain_pct": round(((current_val_base - cost_basis_base) / cost_basis_base * 100), 2) if cost_basis_base != 0 else 0,
                "current_price_user": round(current_price_native * rate, 2),
                "currency_native": asset_currency
            }))
            
            total_value_base += current_val_base
            total_cost_base += cost_basis_base
            total_prev_value_base += prev_val_base

        except Exception as e:
            print(f"Error en {ticker_symbol}: {e}")
            continue

    return {
        "summary": {
            "total_value": round(total_value_base, 2),
            "total_gain_loss": round(total_value_base - total_cost_base, 2),
            "currency": user_base_currency
        },
        "assets": assets_detail
    }

    total_gain_loss = total_value_base - total_cost_base
    return {
        "summary": {
            "total_value": round(total_value_base, 2),
            "total_gain_loss": round(total_gain_loss, 2),
            "today_gain_loss": round(total_value_base - total_prev_value_base, 2),
            "total_gain_pct": round((total_gain_loss / total_cost_base * 100), 2) if total_cost_base != 0 else 0,
            "currency": user_base_currency
        },
        "assets": assets_detail
    }

@app.get("/api/v1/market/history")
def get_market_history(tickers: str = Query(...), period: str = "1mo", start_date: Optional[str] = Query(None)):
    try:
        ticker_list = [t.strip().upper() for t in tickers.split(",")]
        interval = "5m" if period == "1d" else "1d"
        
        if start_date:
            data = yf.download(ticker_list, start=start_date, interval="1d", progress=False)
        else:
            data = yf.download(ticker_list, period=period, interval=interval, progress=False)

        if data.empty: return JSONResponse(content={t: {} for t in ticker_list}, status_code=200)

        df_close = data['Close']
        if len(ticker_list) == 1:
            df = df_close.to_frame(name=ticker_list[0]) if isinstance(df_close, pd.Series) else df_close
        else:
            df = df_close
        df = df.ffill().bfill().replace({float('nan'): None})
        df.index = df.index.strftime('%H:%M') if period == "1d" else df.index.strftime('%Y-%m-%d')
        return df.to_dict()
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))



@app.get("/api/v1/portfolio/history")
def get_portfolio_history(tickers: str = Query(...), period: str = "1mo"): 
    try:
        ticker_list = [t.strip().upper() for t in tickers.split(",")]
        
        intervals = {
            "1d": "2m",
            "1wk": "15m",
            "1mo": "90m",
            "1y": "1d",
            "5y": "1wk"
        }
        
        actual_period = "5y" if period == "max" else period
        interval = intervals.get(actual_period, "1d")
        
        data = yf.download(ticker_list, period=actual_period, interval=interval, progress=False)
        
        if data.empty:
            return {"rate": 0.92, "prices": {}, "isPositive": True}

        try:
            fx = yf.Ticker("EURUSD=X")
            current_rate = float(fx.fast_info.last_price)
        except Exception:
            current_rate = 0.92

        df_close = data['Close']

        if len(ticker_list) == 1:
            if isinstance(df_close, pd.Series):
                df = df_close.to_frame(name=ticker_list[0])
            else:
                df = df_close
        else:
            df = df_close

        df = df.ffill().bfill()
        
        if len(ticker_list) > 1:
            df['portfolio_index'] = df.mean(axis=1)
        else:
            df['portfolio_index'] = df[ticker_list[0]]

        start_val = float(df['portfolio_index'].iloc[0])
        end_val = float(df['portfolio_index'].iloc[-1])
        
        if actual_period == "1d":
            time_format = '%H:%M'
        elif actual_period == "1mo":
            time_format = '%d %b %H:%M'
        else:
            time_format = '%Y-%m-%d'

        result_prices = {}
        for col in df.columns:
            result_prices[col] = [
                {
                    "t": idx.strftime(time_format),
                    "v": sanitize(round(float(val), 2))
                } 
                for idx, val in df[col].items()
            ]
        
        return {
            "period": actual_period,
            "rate": current_rate,
            "isPositive": bool(end_val >= start_val),
            "totalChangePct": round(((end_val / start_val) - 1) * 100, 2),
            "prices": result_prices
        }
        
    except Exception as e:
        print(f"Error crítico en historial de portfolio: {str(e)}")
        return {"rate": 0.92, "prices": {}, "isPositive": True, "error": str(e)}

@app.get("/api/v1/stock/{ticker}/full")
def get_stock_data_pro(ticker: str):
    try:
        stock = yf.Ticker(ticker.upper())
        info = stock.info
        
        hist = stock.history(period="2d")
        
        current_price = info.get("currentPrice") or info.get("regularMarketPrice")
        
        daily_change_pct = None
        if not hist.empty and len(hist) >= 2:
            prev_close = hist['Close'].iloc[-2]
            last_price = hist['Close'].iloc[-1] if current_price is None else current_price
            daily_change_pct = ((last_price / prev_close) - 1) * 100
        elif info.get("regularMarketChangePercent"):
            daily_change_pct = info.get("regularMarketChangePercent")

        target_mean = info.get("targetMeanPrice")
        upside_pct = None
        if current_price and target_mean:
            upside_pct = ((target_mean / current_price) - 1) * 100

        logo_url = info.get("logo_url") or f"https://www.google.com/s2/favicons?domain={ticker.lower()}.com&sz=128"

        return sanitize({
            "ticker": ticker.upper(),
            "longName": info.get("longName"),
            "logoUrl": logo_url,
            "businessSummary": info.get("longBusinessSummary"),
            "currentPrice": current_price,
            "dailyChangePct": round(daily_change_pct, 2) if daily_change_pct is not None else None,
            "fundamentals": {
                "peRatio": info.get("trailingPE"),
                "forwardPE": info.get("forwardPE"),
                "pegRatio": info.get("pegRatio"),
                "enterpriseToEbitda": info.get("enterpriseToEbitda")
            },
            "metrics": {
                "profitability": {
                    "ebitdaMargins": info.get("ebitdaMargins"),
                    "operatingMargins": info.get("operatingMargins"),
                    "grossMargins": info.get("grossMargins"),
                    "returnOnAssets": info.get("returnOnAssets"),
                    "returnOnEquity": info.get("returnOnEquity")
                },
                "growth": {
                    "revenueGrowth": info.get("revenueGrowth"),
                    "earningsGrowth": info.get("earningsGrowth"),
                    "freeCashflow": info.get("freeCashflow"),
                    "operatingCashflow": info.get("operatingCashflow")
                }
            },
            "solvency": {
                "debtToEquity": info.get("debtToEquity"),
                "currentRatio": info.get("currentRatio"),
                "quickRatio": info.get("quickRatio"),
                "totalCash": info.get("totalCash"),
                "totalDebt": info.get("totalDebt")
            },
            "analysts": {
                "recommendation": info.get("recommendationKey"),
                "numberOfAnalysts": info.get("numberOfAnalystOpinions"),
                "targetPrice": {
                    "low": info.get("targetLowPrice"),
                    "high": info.get("targetHighPrice"),
                    "mean": target_mean,
                    "median": info.get("targetMedianPrice"),
                    "current": current_price
                },
                "upsidePotentialPct": round(upside_pct, 4) if upside_pct is not None else None
            },
            "dividends": {
                "yield": info.get("dividendYield"),
                "payoutRatio": info.get("payoutRatio"),
                "lastDividend": info.get("lastDividendValue")
            }
        })
    except Exception as e:
        print(f"Error en Python para {ticker}: {str(e)}")
        raise HTTPException(status_code=404, detail=f"Error al obtener datos: {str(e)}")

@app.get("/api/v1/stock/{ticker}/chart")
def get_stock_chart(ticker: str, period: str = "1mo"):
    intervals = {
        "1d": "2m",
        "1wk": "15m",
        "1mo": "90m",
        "1y": "1d",
        "5y": "1wk"
    }

    if period == "max":
        period = "5y"

    interval = intervals.get(period, "1d")
    stock = yf.Ticker(ticker.upper())
    
    hist = stock.history(period=period, interval=interval)

    if hist.empty:
        raise HTTPException(status_code=404, detail="No hay datos para este periodo")

    start_price = float(hist['Close'].iloc[0])
    current_price = float(hist['Close'].iloc[-1])
    is_positive = bool(current_price >= start_price)
    
    chart_data = []
    for index, row in hist.iterrows():
        if period == "1d":
            time_label = index.strftime("%H:%M")
        elif period == "1mo":
            time_label = index.strftime("%d %b %H:%M")
        else:
            time_label = index.strftime("%Y-%m-%d")

        chart_data.append(sanitize({
            "time": time_label,
            "timestamp": int(index.timestamp()),
            "price": round(float(row['Close']), 2),
            "open": round(float(row['Open']), 2),
            "high": round(float(row['High']), 2),
            "low": round(float(row['Low']), 2),
            "volume": int(row['Volume'])
        }))

    return {
        "ticker": ticker.upper(),
        "period": period,
        "isPositive": is_positive,
        "startPrice": round(start_price, 2),
        "endPrice": round(current_price, 2),
        "totalChangePct": round(((current_price / start_price) - 1) * 100, 2),
        "history": chart_data
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)