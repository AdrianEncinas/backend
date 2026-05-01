# AssetTrack Frontend - Guía de API

## Cambios Realizados

He actualizado completamente el frontend de AssetTrack para que funcione correctamente con los endpoints del backend Java. Aquí están todos los cambios y mejoras:

### 1. **Backend - Configuración CORS** ✅
Se añadió configuración de CORS en el archivo `CorsConfig.java` para permitir solicitudes desde el frontend en los puertos 3000 y 5173.

### 2. **Archivos Principales Actualizados**

#### `src/lib/api.ts` - Cliente API consolidado
Contiene todas las funciones para comunicarse con el backend:

**Portfolio Endpoints:**
- `fetchPortfolioDashboard(userId)` - GET `/api/v1/portfolio/dashboard/{id}`
- `fetchPortfolioGraph(userId, mode, period)` - GET `/api/v1/portfolio/{id}/graph`
- `fetchStockDetails(ticker)` - GET `/api/v1/portfolio/stock/{ticker}`
- `addPosition(position, userId)` - POST `/api/v1/portfolio/add`
- `modifyPosition(position, userId)` - PUT `/api/v1/portfolio/modify`
- `deletePosition(position, userId)` - DELETE `/api/v1/portfolio/delete`
- `manualUpdateHolding(holdingId, shares, avgPrice)` - PUT `/api/v1/portfolio/holdings/{id}/manual-update`

**Chart Endpoints:**
- `fetchStockChart(ticker, period)` - GET `/api/v1/chart/{ticker}`

**Market Endpoints:**
- `searchTicker(query)` - GET `/api/v1/market/search`
- `getWatchlist(userId)` - GET `/api/v1/market/watchlist/{id}`
- `addToWatchlist(item)` - POST `/api/v1/market/watchlist/add`
- `deleteFromWatchlist(item)` - DELETE `/api/v1/market/watchlist/delete`

**User Endpoints:**
- `getUsers()` - GET `/api/v1/user/list`
- `getUser(userId)` - GET `/api/v1/user/get/{id}`
- `createUser(user)` - POST `/api/v1/user/create`
- `modifyUser(userId, user)` - PUT `/api/v1/user/modify/{id}`
- `deleteUser(userId)` - DELETE `/api/v1/user/delete/{id}`

#### `src/apiClient.ts` - Re-exportador de funciones
Proporciona una interfaz unificada importando todas las funciones de `src/lib/api.ts`.

#### `src/types.ts` - Tipos TypeScript
Contiene las interfaces para:
- `PortfolioPointDTO` - Punto del gráfico de portfolio
- `PortfolioDashboard` - Datos del dashboard
- `StockFullDTO` - Detalles completos de una acción
- `HoldingDTO` - Posición de una acción
- `ChartDTO` - Datos del gráfico
- `PortfolioHistory` - Histórico del portfolio

#### `src/pages/DashboardPage.tsx` - Página principal
Ahora carga:
1. Dashboard del portfolio desde `fetchPortfolioDashboard()`
2. Gráfico del portfolio desde `fetchPortfolioGraph()`

#### `src/components/dashboard/portfolio-header.tsx`
Actualizado para mostrar:
- Valor total del portfolio
- Capital invertido
- Ganancia/pérdida
- Variación del día

Acepta datos tanto de `PortfolioDashboard` como de `PortfolioPointDTO`.

#### `src/components/dashboard/portfolio-chart.tsx`
Gráfico de evolución del portfolio con períodos seleccionables (1D, 1W, 1M, 1Y, ALL).

#### `src/components/TickerSearch.tsx`
Componente para buscar acciones usando `searchTicker()` del backend.

### 3. **Nuevos Componentes Creados** ✨

#### `src/components/PositionManager.tsx`
Componente para gestionar posiciones de acciones:
- ✅ Añadir nuevas posiciones
- ✅ Modificar posiciones existentes
- ✅ Eliminar posiciones

#### `src/components/WatchlistManager.tsx`
Componente para gestionar la lista de vigilancia:
- ✅ Ver lista de acciones vigiladas
- ✅ Añadir nuevas acciones a vigilancia
- ✅ Eliminar acciones de la lista

### 4. **Cambios en el Backend**

#### `src/main/java/com/assetstrack/backend/config/CorsConfig.java` (NUEVO)
Configuración CORS para permitir solicitudes desde el frontend.

#### `src/main/java/com/assetstrack/backend/model/dto/PortfolioPointDTO.java`
Actualizado para incluir campos adicionales:
- `date`, `time`, `timestamp`
- `portfolioValue`
- `investedCapital`
- `dayIntraChange`, `totalChangePercentage`

## Flujo de Datos

```
Frontend (React)
    ↓
src/apiClient.ts (interfaz unificada)
    ↓
src/lib/api.ts (cliente HTTP con axios)
    ↓
http://localhost:8080/api/v1 (Backend Java/Spring)
    ↓
Controladores (Portfolio, Chart, Market, User)
    ↓
Servicios (PortfolioApiService, MarketService, etc.)
    ↓
Repositorios (Base de datos PostgreSQL)
```

## Instalación y Uso

### Requisitos
- Node.js 18+
- Backend Java en http://localhost:8080
- Base de datos PostgreSQL

### Instalación del Frontend
```bash
cd frontend
npm install
npm run dev  # Puerto 5173 por defecto
```

### Construcción
```bash
npm run build
```

## Estructura de Datos

### Objeto de Portfolio
```json
{
  "totalValue": 25000.50,
  "dayChange": 150.25,
  "dayChangePct": 0.61,
  "investedCapital": 20000,
  "currency": "EUR",
  "holdings": [
    {
      "id": 1,
      "ticker": "AAPL",
      "companyName": "Apple Inc.",
      "shares": 100,
      "avg_price": 150.50
    }
  ]
}
```

### Objeto de Posición
```json
{
  "ticker": "AAPL",
  "companyName": "Apple Inc.",
  "shares": 100,
  "avg_price": 150.50
}
```

### Objeto del Gráfico
```json
{
  "ticker": "AAPL",
  "period": "1mo",
  "history": [
    {
      "time": "2024-01-15",
      "price": 185.50,
      "volume": 1000000
    }
  ]
}
```

## Notas Importantes

1. **Usuario por defecto**: El frontend usa `userId = 1`. Ajusta esto en `lib/api.ts` si necesitas otro usuario.

2. **CORS configurado**: El backend permite solicitudes desde `http://localhost:3000` y `http://localhost:5173`.

3. **Tipado completo**: Todos los archivos están correctamente tipados con TypeScript.

4. **Manejo de errores**: Todos los endpoints incluyen try/catch y manejo de errores.

5. **Componentes reutilizables**: Los componentes están diseñados para ser reutilizables en diferentes partes de la aplicación.

## Próximos Pasos (Opcionales)

1. Implementar autenticación real (reemplazar userId hardcoded)
2. Mejorar el manejo de caché
3. Añadir validaciones de formulario más robustas
4. Implementar sincronización automática de datos
5. Mejorar mensajes de error y feedback al usuario
