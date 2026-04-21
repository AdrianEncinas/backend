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
| **Lombok** | - | Reducción de boilerplate (getters, builders, etc.) |
| **Bean Validation (Jakarta)** | - | Validación de DTOs en los endpoints |

## Base URL

```
http://localhost:8080/api/v1
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

## Requisitos

- Java 17+
- PostgreSQL corriendo en `localhost:5432` con base de datos `investdb`
- Servicio Python corriendo en `http://localhost:5000`

## Ejecutar (PowerShell)

```powershell
.\mvnw spring-boot:run
```

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `JWT_SECRET` | clave base64 por defecto | Clave de firma del token JWT |
| `JWT_EXPIRATION` | `86400000` (24h) | Expiración del token en ms |

## Más detalle

`DOCUMENTACION_FUNCIONES_BACKEND.txt`
