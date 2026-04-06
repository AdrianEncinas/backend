# AssetTrack Backend

Backend REST en Java (Spring Boot) para gestionar usuarios y portafolios, y consultar mercado/graficas.

## Base URL
- `http://localhost:8080/api/v1`

## Endpoints principales
- Usuario: `POST /user/login`, `POST /user/create`, `GET /user/list`, `GET /user/get/{id}`
- Portfolio: `GET /portfolio/dashboard/{id}`, `POST /portfolio/add`, `PUT /portfolio/modify`, `DELETE /portfolio/delete`
- Mercado: `GET /market/search`, `GET/POST/DELETE /market/watchlist/...`
- Graficas: `GET /chart/{ticker}?period=1d|5d|1mo|3mo|6mo|1y`

## Requisitos
- PostgreSQL (ver `src/main/resources/application.properties`)

## Ejecutar (PowerShell)
- `.\mvnw spring-boot:run`

## Mas detalle
- `DOCUMENTACION_FUNCIONES_BACKEND.txt`
