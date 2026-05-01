# AssetTrack

Aplicación web para gestionar y seguir una cartera de inversiones en acciones. Permite registrar posiciones, ver el valor del portfolio en tiempo real, consultar gráficos históricos e intraday, y mantener una watchlist de activos de interés.

## Tecnologías

**Backend**
- Java 17 + Spring Boot 3
- Spring Security con autenticación JWT
- PostgreSQL
- Maven

**Frontend**
- Angular 17 (módulos)
- TypeScript
- SCSS

## Funcionalidades principales

- Registro e inicio de sesión con JWT
- Dashboard con valor total del portfolio y gráfico histórico
- Añadir, editar y eliminar posiciones
- Búsqueda de tickers y consulta de datos de mercado
- Watchlist personalizada
- Gráficos intraday e históricos por acción

## Estructura

```
backend/   ? API REST (Spring Boot, puerto 8080)
frontend/  ? SPA Angular (puerto 4200)
```

## Arranque rápido

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

> Requiere PostgreSQL en ejecuci�n y credenciales configuradas en `backend/src/main/resources/application.properties`.
