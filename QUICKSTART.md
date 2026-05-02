# 🚀 Guía Rápida de Inicio - AssetTrack

## Pasos para Ejecutar la Aplicación

### 1. Backend (Java/Spring) - Puerto 8080

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Verificación:** Abre http://localhost:8080/api/v1/portfolio/dashboard/1 en el navegador. Deberías ver JSON.

### 2. Frontend (React/Vite) - Puerto 3001

> **Nota:** el proyecto usa `strictPort` en Vite. Si otro servicio ya ocupa el puerto 3001 obtendrás un error en lugar de saltar a 3002.

```bash
cd frontend
npm install
npm run dev
```

**Salida esperada:**
```
  VITE v6.0.0  ready in 234 ms

  ➜  Local:       http://localhost:3001/
```

### 3. Abre la Aplicación

- Dirección: http://localhost:5173
- Deberías ver el Dashboard de Portfolio

## 📱 Navegación de la Aplicación

### Dashboard (/)
- Valor total del portfolio
- Gráfico de evolución temporal
- Capital invertido vs valor de mercado

### Búsqueda (/search)
- Buscar acciones por ticker
- Ver gráfico de la acción
- Ver análisis fundamental
- Opción de añadir a portfolio

### Watchlist (/watchlist)
- Gestionar lista de vigilancia
- Ver lista de acciones vigiladas
- Análisis rápido de cada acción

## ✅ Checklist de Configuración

- [ ] Backend corriendo en puerto 8080
- [ ] PostgreSQL corriendo en puerto 5432
- [ ] Frontend corriendo en puerto 5173
- [ ] CORS configurado (CorsConfig.java)
- [ ] Base de datos con datos de prueba

## 🔍 Problemas Comunes

### "Connection refused" en 8080
- Asegúrate de que el backend está corriendo
- Verifica que el puerto 8080 está disponible
- Intenta: `mvn spring-boot:run` desde la carpeta backend

### "Connection refused" en 5432 (PostgreSQL)
- Asegúrate de que PostgreSQL está corriendo
- Verifica las credenciales en `application.properties`:
  - URL: `jdbc:postgresql://localhost:5432/investdb`
  - Usuario: `postgres`
   - Contraseña: `root`

### Errores CORS
- Verifica que `CorsConfig.java` existe en backend
- Los puertos permitidos: 3000, 3001, 5173
- Si usas otro puerto, actualiza `CorsConfig.java` y reinicia el servidor backend

### Datos vacíos en Dashboard
- Verifica que la base de datos tiene datos
- Ejecuta `import.sql` para datos de prueba
- Comprueba que el userId = 1 existe

## 📊 Datos de Prueba

El backend carda automáticamente datos de prueba desde `resources/import.sql`.

Para el usuario 1, deberías tener:
- Algunas posiciones de acciones
- Histórico de portfolio
- Datos de mercado

## 🔧 Cambios Realizados

### Frontend
- ✅ API cliente consolidado (`src/lib/api.ts`)
- ✅ Tipos TypeScript actualizados
- ✅ Componentes nuevos (PositionManager, WatchlistManager)
- ✅ Páginas nuevas (SearchPage, FavoritesPage)
- ✅ Enrutamiento completo

### Backend
- ✅ Configuración CORS (CorsConfig.java)
- ✅ DTOs actualizados (PortfolioPointDTO)

## 💡 Tips

1. **Consola del Navegador**
   - Abre F12 para ver errores y logs
   - Verifica la pestaña "Network" para ver llamadas API

2. **Consola del Backend**
   - Busca "Tomcat started" para confirmar que está corriendo
   - Los logs muestran las solicitudes HTTP

3. **Base de Datos**
   - Conéctate con DBeaver o pgAdmin si necesitas verificar datos
   - Host: localhost:5432, Usuario: postgres, Contraseña: root

## 📞 Recursos

- **API_GUIDE.md** - Documentación completa de endpoints
- **CHANGELOG.md** - Lista detallada de cambios
- Código comentado en archivos principales

## 🎯 Próximos Pasos

1. Prueba cada página de la aplicación (Dashboard, Buscar, Watchlist)
2. Intenta buscar una acción (ej: AAPL)
3. Intenta añadir una posición
4. Verifica que los gráficos se cargan

¡Diviértete! 🚀
