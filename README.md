# 📖 Documentación de AssetTrack

## 📑 Índice de Archivos

### 🚀 Para Empezar
1. **QUICKSTART.md** - Lee esto primero (5 minutos)
   - Pasos para ejecutar la aplicación
   - Checklist de configuración
   - Solución de problemas comunes

### 📊 Documentación Principal
2. **RESUMEN_CAMBIOS.md** - Resumen ejecutivo (10 minutos)
   - Cambios realizados
   - Matriz de endpoints
   - Características nuevas

3. **API_GUIDE.md** - Guía técnica completa (15 minutos)
   - Descripción de todos los endpoints
   - Flujo de datos
   - Ejemplos de uso
   - Configuración detallada

4. **CHANGELOG.md** - Documentación detallada (20 minutos)
   - Cambios línea por línea
   - Estructura del proyecto
   - Integración backend-frontend
   - Testing de endpoints

### 💻 Archivos del Código

#### Frontend - Cliente API
- `frontend/src/lib/api.ts` - **PRINCIPAL**: 25+ funciones API
- `frontend/src/apiClient.ts` - Re-exportador unificado

#### Frontend - Tipos
- `frontend/src/types.ts` - Tipos principales
- `frontend/src/lib/types.ts` - Tipos adicionales

#### Frontend - Componentes
- `frontend/src/app/services/auth.service.ts` - Servicio de autenticación
- `frontend/src/app/components/` - Componentes de la aplicación
- `frontend/src/app/guards/` - Guards de rutas
- `frontend/src/app/interceptors/` - Interceptores HTTP

#### Backend - Configuración
- `backend/src/main/java/com/assetstrack/backend/config/SecurityConfig.java` - Seguridad JWT
- `backend/src/main/java/com/assetstrack/backend/config/CorsConfig.java` - Config CORS
- `backend/src/main/java/com/assetstrack/backend/config/JwtUtil.java` - Utilidades JWT
- `backend/src/main/java/com/assetstrack/backend/config/JwtAuthFilter.java` - Filtro JWT

#### Backend - Controladores
- `UserController.java` → `/api/v1/users`
- `PortfolioController.java` → `/api/v1/portfolio`
- `MarketController.java` → `/api/v1/market`
- `ChartController.java` → `/api/v1/chart`

#### Backend - DTOs relevantes
- `StockPositionDTO.java` - Acepta `ticker`/`symbol` y `companyName`/`name` (aliases JSON)
- `LoginRequest.java` - `username` + `password`
- `UserDTO.java` - `username` + `password` + `baseCurrency`

---

## 🎯 Flujos de Trabajo

### Flujo 1: Login
```
POST /api/v1/users/login  { username, password }
↓
JWT token en respuesta
↓
Almacenar token → enviar en Authorization: Bearer <token>
```

### Flujo 2: Ver Dashboard
```
GET /api/v1/portfolio/dashboard  [AUTH]
↓
GET /api/v1/portfolio/graph?mode=historic  [AUTH]
↓
Muestra valor del portfolio y gráfico histórico
```

### Flujo 3: Añadir Posición
```
POST /api/v1/portfolio/positions  [AUTH]
{ "ticker": "AMZN", "companyName": "Amazon", "shares": 10, "avg_price": 185.00 }
↓
201 Created
```

### Flujo 4: Eliminar Posición
```
DELETE /api/v1/portfolio/positions/AMZN  [AUTH]
↓
200 "The position has been deleted."
```

### Flujo 5: Gestionar Watchlist
```
GET  /api/v1/market/watchlist          [AUTH] → lista actual
POST /api/v1/market/watchlist          [AUTH] { ticker, companyName } → añadir
DELETE /api/v1/market/watchlist/{id}   [AUTH] → eliminar por ID del item
```

---

## 📋 Checklist de Verificación

### Antes de Ejecutar
- [ ] Node.js 18+ instalado
- [ ] Maven instalado
- [ ] PostgreSQL corriendo
- [ ] Puerto 8080 disponible
- [ ] Puerto 5173 disponible

### Instalación
- [ ] `npm install` en frontend
- [ ] `mvn clean install` en backend
- [ ] Base de datos creada
- [ ] Tablas creadas (hibernate)

### Verificación
- [ ] Backend responde en :8080/api/v1/portfolio/dashboard/1
- [ ] Frontend carga en :5173 (si el puerto está ocupado el servidor fallará para avisar en lugar de cambiar a otro puerto)
- [ ] Datos visibles en dashboard
- [ ] Búsqueda funciona
- [ ] Gráficos se cargan

### Errores Comunes
- [ ] Si error CORS: Verifica CorsConfig.java
- [ ] Si error BD: Verifica credentials en application.properties
- [ ] Si datos vacíos: Verifica usuario 1 en BD
- [ ] Si puerto ocupado: Cambia puerto en config

---

## 🔍 Dónde Buscar

**Ver / modificar endpoints**
→ `backend/src/main/java/com/assetstrack/backend/controller/`

**Seguridad y JWT**
→ `backend/.../config/SecurityConfig.java`, `JwtUtil.java`, `JwtAuthFilter.java`

**Manejo de errores global**
→ `backend/.../exception/GlobalExceptionHandler.java`

**Modificar CORS**
→ `backend/.../config/CorsConfig.java`

**Servicio de autenticación (frontend)**
→ `frontend/src/app/services/auth.service.ts`

**URL base del backend**
→ `frontend/src/app/services/auth.service.ts` propiedad `API`

---

## 📊 Endpoints del Backend

| Método | URL | Auth | Descripción |
|--------|-----|------|-------------|
| POST | `/api/v1/users/login` | No | Login, devuelve JWT |
| POST | `/api/v1/users/register` | No | Registro |
| GET | `/api/v1/users/me` | Sí | Perfil propio |
| PUT | `/api/v1/users/me` | Sí | Modificar perfil |
| DELETE | `/api/v1/users/me` | Sí | Eliminar cuenta |
| GET | `/api/v1/portfolio/dashboard` | Sí | Estado del portfolio |
| GET | `/api/v1/portfolio/stocks/{ticker}` | Sí | Detalles de acción |
| POST | `/api/v1/portfolio/positions` | Sí | Añadir posición |
| PUT | `/api/v1/portfolio/positions` | Sí | Modificar posición |
| DELETE | `/api/v1/portfolio/positions/{ticker}` | Sí | Eliminar posición |
| PATCH | `/api/v1/portfolio/holdings/{holdingId}` | Sí | Sync manual holding |
| GET | `/api/v1/portfolio/graph` | Sí | Histórico/intraday |
| GET | `/api/v1/market/search?query=` | Sí | Buscar tickers |
| GET | `/api/v1/market/watchlist` | Sí | Obtener watchlist |
| POST | `/api/v1/market/watchlist` | Sí | Añadir a watchlist |
| DELETE | `/api/v1/market/watchlist/{id}` | Sí | Eliminar de watchlist |
| GET | `/api/v1/chart/{ticker}?period=` | Sí | Gráfico de acción |

---

## ✅ Estado de Implementación

Portfolio Management
- [x] Ver dashboard
- [x] Ver gráficos históricos e intraday
- [x] Añadir posiciones (acepta `ticker`/`symbol`, `companyName`/`name`)
- [x] Modificar posiciones
- [x] Eliminar posiciones por ticker (`DELETE /positions/{ticker}`)
- [x] Sincronización manual de holding

Market & Search
- [x] Buscar acciones por query
- [x] Ver detalles completos de acción
- [x] Ver gráficos de acción
- [x] Gestionar watchlist (obtener, añadir, eliminar)

User Management
- [x] Login con JWT
- [x] Registro
- [x] Ver/modificar/eliminar perfil propio

Técnico
- [x] Autenticación JWT stateless
- [x] CORS configurado
- [x] Validación de inputs (`@Valid`)
- [x] Manejo global de errores (400/401/403/404/409/500)

---

## 🚀 Comandos Útiles

### Backend
```bash
cd backend
mvn clean install           # Compilar
mvn spring-boot:run        # Ejecutar
mvn test                   # Tests
```

### Frontend
```bash
cd frontend
npm install                # Instalar dependencias
npm run dev                # Desarrollo (puerto 5173)
npm run build              # Compilar para producción
npm run preview            # Vista previa producción
```

### Testing con curl
```bash
# Login
curl -X POST http://localhost:8080/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario","password":"1234"}'

# Dashboard (sustituir <token>)
curl http://localhost:8080/api/v1/portfolio/dashboard \
  -H "Authorization: Bearer <token>"

# Añadir posición
curl -X POST http://localhost:8080/api/v1/portfolio/positions \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"ticker":"AAPL","companyName":"Apple Inc.","shares":10,"avg_price":175.00}'

# Eliminar posición
curl -X DELETE http://localhost:8080/api/v1/portfolio/positions/AAPL \
  -H "Authorization: Bearer <token>"

# Buscar
curl "http://localhost:8080/api/v1/market/search?query=AAPL" \
  -H "Authorization: Bearer <token>"
```

---

## 📞 Soporte

### Error: "Connection refused"
**Solución:** Verifica que el backend está corriendo en puerto 8080

### Error de CORS
**Solución:** Revisa CorsConfig.java y asegúrate que tu puerto está en la lista

### Datos vacíos
**Solución:** Verifica que el usuario 1 existe en BD y tiene datos

### TypeScript errors
**Solución:** Ejecuta `npm install` para instalar tipos

---

## 🎓 Recursos de Aprendizaje

1. **TypeScript**: https://www.typescriptlang.org/docs/
2. **React**: https://react.dev
3. **Axios**: https://axios-http.com/docs/intro
4. **Spring Boot**: https://spring.io/projects/spring-boot
5. **PostgreSQL**: https://www.postgresql.org/docs/

---

## 🎉 ¡Listo!

La aplicación está completamente funcional y lista para usar.

Comienza con **QUICKSTART.md** y luego consulta **API_GUIDE.md** si necesitas detalles técnicos.

**¡Diviértete! 🚀**
