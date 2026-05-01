# AssetTrack

Aplicaci�n web para gestionar y seguir una cartera de inversiones en acciones. Permite registrar posiciones, ver el valor del portfolio en tiempo real, consultar gr�ficos hist�ricos e intraday, y mantener una watchlist de activos de inter�s.

## Tecnolog�as

**Backend**
- Java 17 + Spring Boot 3
- Spring Security con autenticaci�n JWT
- PostgreSQL
- Maven

**Frontend**
- Angular 17 (m�dulos)
- TypeScript
- SCSS

## Funcionalidades principales

- Registro e inicio de sesi�n con JWT
- Dashboard con valor total del portfolio y gr�fico hist�rico
- A�adir, editar y eliminar posiciones
- B�squeda de tickers y consulta de datos de mercado
- Watchlist personalizada
- Gr�ficos intraday e hist�ricos por acci�n

## Estructura

```
backend/   ? API REST (Spring Boot, puerto 8080)
frontend/  ? SPA Angular (puerto 4200)
```

## Arranque r�pido

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
