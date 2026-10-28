# SmartInventory — Backend

API REST para gestionar inventarios, proveedores y movimientos de productos. Incluye autenticación JWT y acceso mediante los roles `ADMIN` y `MANAGER`.

## Funcionalidades

- Registro, inicio de sesión y gestión de usuarios.
- Gestión de productos, categorías y proveedores.
- Registro de compras, ventas y devoluciones a proveedores.
- Consulta de transacciones con paginación, filtros y selección por mes y año.
- Actualización del estado de las transacciones y carga de imágenes de productos.

## Tecnologías

Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA · MySQL · Maven · JWT · Lombok · ModelMapper

## Instalación y configuración

Requiere **JDK 21** y **MySQL**. El repositorio incluye Maven Wrapper.

```bash
git clone https://github.com/thoonidev/SmartInventory-Backend.git
cd SmartInventory-Backend
```

Crea la base de datos en MySQL:

```sql
CREATE DATABASE inventory_db CHARACTER SET utf8mb4;
```

Ajusta estos valores en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
server.port=5050
secreteJwtString=reemplaza_por_una_clave_aleatoria_de_al_menos_32_bytes
```

Hibernate crea o actualiza las tablas al iniciar mediante `spring.jpa.hibernate.ddl-auto=update`. No publiques credenciales reales ni la clave JWT que utilices.

## Ejecución

En Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:5050/api`.

## Uso de la API

Registra una cuenta mediante `POST /api/auth/register` con `name`, `email`, `password` y `phoneNumber`. El rol predeterminado es `MANAGER`; el registro también acepta el campo `role` con los valores `ADMIN` o `MANAGER`.

Inicia sesión con `POST /api/auth/login`, enviando `email` y `password`. Usa el token recibido en las solicitudes protegidas:

```http
Authorization: Bearer <token>
```

| Ruta base | Uso |
| --- | --- |
| `/api/users` | Usuarios y perfil. |
| `/api/products` | Productos y búsqueda. |
| `/api/categories` | Categorías. |
| `/api/suppliers` | Proveedores. |
| `/api/transactions` | Compras, ventas, devoluciones y consultas. |

## Comandos

| Comando (Windows) | Descripción |
| --- | --- |
| `.\mvnw.cmd test` | Ejecuta las pruebas. |
| `.\mvnw.cmd clean package` | Compila, ejecuta las pruebas y genera el JAR en `target/`. |

En Linux o macOS, sustituye `.\mvnw.cmd` por `./mvnw`. Las pruebas que cargan el contexto de Spring requieren MySQL y una configuración válida.

## Configuración de imágenes

Antes de utilizar las cargas, revisa `saveImage2()` en `services/impl/ProductServiceImpl.java`: la ruta de almacenamiento actual es una barra invertida (`\\`) y el método devuelve una ruta relativa `products/`. Debes definir una carpeta de escritura y configurar cómo servir sus imágenes para tu entorno.
