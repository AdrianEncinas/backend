
-- Insert initial user data (only if not exists)
-- INSERT INTO app_users (email, username, password, base_currency) 
-- VALUES ('usuario@example.com', 'InversorPro', '1234', 'EUR')
-- ON CONFLICT (email) DO NOTHING;

-- Insert holdings
-- INSERT INTO holdings (user_id, ticker, company_name, total_shares, avg_price, native_currency) 
-- VALUES (1, 'AMD', 'Advanced Micro Devices, Inc.', 41.14, 123.29, 'EUR')
-- ON CONFLICT (user_id, ticker) DO NOTHING;

-- Insert transactions
-- INSERT INTO transactions (holding_id, shares, price, currency, type, date, created_at) 
-- VALUES (1, 41.14, 123.29, 'EUR', 'BUY', '2026-02-12', CURRENT_TIMESTAMP)
-- ON CONFLICT DO NOTHING;

-- Insert watchlist items
-- INSERT INTO watchlists (user_id, ticker, company_name) 
-- VALUES (1, 'TSLA', 'Tesla, Inc.')
-- ON CONFLICT (user_id, ticker) DO NOTHING;

-- INSERT INTO watchlists (user_id, ticker, company_name) 
-- VALUES (1, 'AAPL', 'Apple Inc.')
-- ON CONFLICT (user_id, ticker) DO NOTHING;

-- INSERT INTO watchlists (user_id, ticker, company_name) 
-- VALUES (1, 'MSFT', 'Microsoft Corporation')
-- ON CONFLICT (user_id, ticker) DO NOTHING;
