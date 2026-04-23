# AssetTrack Backend

Backend REST en Java (Spring Boot) para gestionar usuarios y portafolios, y consultar mercado/gráficas.

## Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 17 | Lenguaje principal |
| **Spring Boot** | 4.0.2 | Framework base |
| **Spring Security** | - | Autenticación y autorización |
| **JWT (jjwt)** | 0.12.6 | Tokens de sesión stateless |
| **Spring Data JPA / Hibernate** | - | ORM y acceso a datos |
| **PostgreSQL** | 42.7.2 | Base de datos relacional |
| **Spring Cloud Azure** | 7.0.0 | Conector JDBC PostgreSQL sin contraseña (passwordless) |
| **Spring WebFlux / WebClient** | - | Comunicación reactiva con el servicio Python |
| **springdoc-openapi** | 2.7.0 | Documentación Swagger UI / OpenAPI 3 |
| **Lombok** | - | Reducción de boilerplate (getters, builders, etc.) |
| **Bean Validation (Jakarta)** | - | Validación de DTOs en los endpoints |
| **Spring DevTools** | - | Recarga automática en desarrollo |

## Base URL

```
http://localhost:8080/api/v1
```

## Documentación interactiva (Swagger UI)

```
http://localhost:8080/swagger-ui.html
```

## Endpoints

### Usuarios `/users`
| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/users/login` | No | Autenticación, devuelve JWT |
| `POST` | `/users/register` | No | Registro de nuevo usuario |
| `GET` | `/users/me` | Sí | Perfil del usuario autenticado |
| `PUT` | `/users/me` | Sí | Modificar perfil |
| `DELETE` | `/users/me` | Sí | Eliminar cuenta |

### Portfolio `/portfolio`
| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/portfolio/dashboard` | Sí | Estado general del portfolio |
| `GET` | `/portfolio/stocks/{ticker}` | Sí | Detalles completos de una acción |
| `POST` | `/portfolio/positions` | Sí | Añadir posición |
| `PUT` | `/portfolio/positions` | Sí | Modificar posición |
| `DELETE` | `/portfolio/positions` | Sí | Eliminar posición |
| `PATCH` | `/portfolio/holdings/{holdingId}` | Sí | Actualización manual de un holding |
| `GET` | `/portfolio/graph` | Sí | Datos históricos o intraday del portfolio |

### Mercado `/market`
| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/market/search?query=` | Sí | Búsqueda de acciones |
| `GET` | `/market/watchlist` | Sí | Lista de seguimiento del usuario |
| `POST` | `/market/watchlist` | Sí | Añadir a lista de seguimiento |
| `DELETE` | `/market/watchlist/{watchlistItemId}` | Sí | Eliminar de lista de seguimiento |

### Gráficas `/chart`
| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `GET` | `/chart/{ticker}?period=1mo` | Sí | Datos de gráfica de una acción |

## Seguridad

Todos los endpoints (excepto `/login` y `/register`) requieren un token JWT en la cabecera:
```
Authorization: Bearer <token>
```
El `userId` **nunca** es enviado por el cliente — siempre se extrae del token en el servidor.

## Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto del servidor |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/investdb` | URL de conexión a PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Contraseña de la base de datos |
| `AZURE_MANAGED_IDENTITY_ENABLED` | `false` | Habilitar identidad gestionada de Azure |
| `SPRING_DATASOURCE_AZURE_PASSWORDLESS_ENABLED` | `false` | Conexión sin contraseña (solo entornos Azure) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Estrategia DDL de Hibernate |
| `SPRING_JPA_SHOW_SQL` | `false` | Mostrar SQL generado en consola |
| `SPRING_SQL_INIT_MODE` | `never` | Modo de inicialización SQL (`never` / `always`) |
| `JWT_SECRET` | *(valor de desarrollo)* | Clave secreta para firmar JWT — **cambiar en producción** |
| `JWT_EXPIRATION` | `86400000` | Expiración del token en ms (24 h) |
| `DB_POOL_MAX_SIZE` | `10` | Tamaño máximo del pool HikariCP |
| `DB_POOL_MIN_IDLE` | `2` | Conexiones mínimas inactivas |
| `LOG_LEVEL_ROOT` | `INFO` | Nivel de log raíz |
| `SPRINGDOC_SWAGGER_UI_PATH` | `/swagger-ui.html` | Ruta de la UI de Swagger |

## Requisitos

- Java 17+
- PostgreSQL corriendo en `localhost:5432` con base de datos `investdb`
- Servicio Python corriendo en `http://localhost:5000`

## Ejecutar (PowerShell)

```powershell
.\mvnw spring-boot:run
```

## Docker

### Levantar backend + PostgreSQL con Docker Compose

```powershell
docker compose up --build -d
```

### Ver logs del backend

```powershell
docker compose logs -f backend
```

### Parar servicios

```powershell
docker compose down
```

Swagger quedará disponible en:

```
http://localhost:8080/swagger-ui/
```

## Buenas prácticas de configuración

- No hardcodear credenciales: `application.properties` usa variables de entorno.
- En producción define un `JWT_SECRET` fuerte (largo, aleatorio y privado).
- Ajusta `SPRING_JPA_HIBERNATE_DDL_AUTO` (idealmente `validate` en producción).
- Mantén `SPRING_JPA_SHOW_SQL=false` en producción.
- Habilita Azure passwordless solo en entornos Azure:
	- `AZURE_MANAGED_IDENTITY_ENABLED=true`
	- `SPRING_DATASOURCE_AZURE_PASSWORDLESS_ENABLED=true`

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `JWT_SECRET` | clave base64 por defecto | Clave de firma del token JWT |
| `JWT_EXPIRATION` | `86400000` (24h) | Expiración del token en ms |

## Más detalle

`DOCUMENTACION_FUNCIONES_BACKEND.txt`
