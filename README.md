# AssetTrack

Aplicación web para gestionar y seguir una cartera de inversiones en acciones. Permite registrar posiciones, ver el valor del portfolio en tiempo real, consultar gráficos históricos e intraday, y mantener una watchlist de activos de interés.

## Tecnologías

**Backend**
- Java 17 + Spring Boot 3
- Spring Security con autenticación JWT
- Spring Data JPA / Hibernate
- Spring WebFlux (WebClient)
- PostgreSQL
- OpenAPI / Swagger (springdoc)
- Maven
- Docker / Docker Compose

**Frontend**
- Angular 17 (módulos)
- TypeScript
- SCSS

**Python (servicio de mercado)**
- FastAPI
- yfinance (datos de mercado)
- pandas y NumPy (procesado de series temporales)
- requests

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
python/    ? API de apoyo para mercado/históricos (FastAPI, puerto 5000)
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

# Python
cd python
pip install fastapi uvicorn yfinance pandas numpy requests
uvicorn test:app --host 0.0.0.0 --port 5000 --reload
```

> Requiere PostgreSQL en ejecuci�n y credenciales configuradas en `backend/src/main/resources/application.properties`.

## Docker general (toda la aplicación)

Con este comando se levanta todo: PostgreSQL + Backend + Python + Frontend.

```bash
docker compose up --build -d
```

Servicios disponibles:
- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Python API: `http://localhost:8000/docs`

Para detener todo:

```bash
docker compose down
```
