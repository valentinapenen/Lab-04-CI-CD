# Lab 04 - CI/CD: PizzUM & BurgUM

## Contexto de esta entrega

Este repositorio corresponde al **Entregable 4 de Programación Avanzada (2026)**. Reutilizamos PizzUM & BurgUM, una aplicación web desarrollada para el obligatorio de **Proyecto TIC (2025)**, como base para trabajar en pruebas automáticas, integración continua y despliegue con Jenkins.

- **Repositorio de esta entrega:** [Lab-04-CI-CD](https://github.com/valentinapenen/Lab-04-CI-CD).
- **Proyecto original:** [PizzUM-BurgUM](https://github.com/valentinapenen/PizzUM-BurgUM).
- **Último commit de la base importada:** [`7adbf27`](https://github.com/valentinapenen/Lab-04-CI-CD/commit/7adbf275974925b30872be95ebcbb4112398ded9).

Conservamos el historial del proyecto original y su autoría. El commit que incorpora esta preparación inicial y este README marca el comienzo del trabajo para la nueva entrega. Los commits posteriores corresponden a las adaptaciones y al desarrollo de CI/CD en este repositorio.

## Qué hace la aplicación

PizzUM & BurgUM permite gestionar pedidos de un restaurante ficticio. Tiene dos perfiles:

- **Cliente:** registra su cuenta, administra sus datos, domicilios y tarjetas, arma pizzas y hamburguesas, utiliza el carrito y consulta favoritos, historial y seguimiento de pedidos.
- **Administrador:** administra productos e ingredientes, precios base de pizzas, pedidos, otros administradores y consultas de ventas por fecha.

En el modelo, un **Producto** es un ingrediente o complemento del catálogo; una **Creacion** es una pizza o hamburguesa armada por un cliente; un **Pedido** reúne las creaciones confirmadas, el domicilio, el total y el estado. Las tarjetas se utilizan como datos de la aplicación: el flujo de confirmación no realiza un cobro mediante una pasarela de pagos.

## Tecnologías

| Componente | Uso |
| --- | --- |
| Java 21 | Versión de compilación declarada en el proyecto. |
| Spring Boot 3.5.6 | Aplicación web y configuración. |
| Spring MVC y Thymeleaf | Controladores y páginas HTML renderizadas en el servidor. |
| Spring Security | Autenticación, roles y sesión de usuario. |
| Spring Data JPA / Hibernate | Entidades y acceso a datos. |
| PostgreSQL 15 | Base de datos de desarrollo, disponible mediante Docker Compose. |
| H2 | Base temporal utilizada para la comprobación aislada del contexto. |
| Lombok | Generación de getters, setters y constructores, entre otros. |
| Maven Wrapper 3.9.11 | Compilación y ejecución sin instalar Maven por separado. |

La aplicación se construye como un único proyecto Maven. Los módulos son funcionales y comparten la misma aplicación y base de datos.

## Cómo está organizado el código

```text
src/main/java/com/example/PizzUMBurgUM/
  PizzUmBurgUmApplication.java   Punto de entrada
  configuracion/                Seguridad e inicialización del administrador
  controllers/                  Rutas web y respuestas REST
    DTOS/                       Objetos de entrada y salida
  services/                     Reglas de negocio
  repositories/                 Acceso a datos mediante JPA
  entities/                     Modelo persistente
    enums/                      Tipos, categorías y estados
src/main/resources/
  application.properties        Conexión a la base y configuración inicial
  templates/                    Vistas de inicio, cliente y administrador
  static/                       CSS e imágenes
src/test/java/                  Pruebas automáticas
pom.xml                         Dependencias y construcción
docker-compose.yml             Servicio PostgreSQL
```

Una petición suele pasar del controlador al servicio y luego al repositorio. El controlador prepara los datos de la vista y Thymeleaf genera el HTML. Las reglas de negocio principales están en los servicios.

| Funcionalidad | Archivos para empezar a leer |
| --- | --- |
| Registro e inicio de sesión | [AuthController](src/main/java/com/example/PizzUMBurgUM/controllers/AuthController.java), [ClienteService](src/main/java/com/example/PizzUMBurgUM/services/ClienteService.java) y [ConfiguracionSeguridad](src/main/java/com/example/PizzUMBurgUM/configuracion/ConfiguracionSeguridad.java). |
| Autenticación y sesión | [CustomUserDetailsService](src/main/java/com/example/PizzUMBurgUM/services/CustomUserDetailsService.java), [PasswordConfig](src/main/java/com/example/PizzUMBurgUM/configuracion/PasswordConfig.java) y [CustomAuthenticationSuccessHandler](src/main/java/com/example/PizzUMBurgUM/configuracion/CustomAuthenticationSuccessHandler.java). |
| Productos e ingredientes | [ProductoController](src/main/java/com/example/PizzUMBurgUM/controllers/ProductoController.java) y [ProductoService](src/main/java/com/example/PizzUMBurgUM/services/ProductoService.java). |
| Creaciones, carrito y favoritos | [ClienteController](src/main/java/com/example/PizzUMBurgUM/controllers/ClienteController.java) y [CreacionService](src/main/java/com/example/PizzUMBurgUM/services/CreacionService.java). |
| Pedidos y seguimiento | [PedidoController](src/main/java/com/example/PizzUMBurgUM/controllers/PedidoController.java) y [PedidoService](src/main/java/com/example/PizzUMBurgUM/services/PedidoService.java). La confirmación desde el carrito está en `CreacionService.crearPedidoDesdeCarrito()`. |
| Domicilios y tarjetas del cliente | [ClienteDomicilioController](src/main/java/com/example/PizzUMBurgUM/controllers/ClienteDomicilioController.java), [TarjetaClienteController](src/main/java/com/example/PizzUMBurgUM/controllers/TarjetaClienteController.java) y sus servicios. |
| Administración y ventas | [AdministradorController](src/main/java/com/example/PizzUMBurgUM/controllers/AdministradorController.java), [AdminVentasController](src/main/java/com/example/PizzUMBurgUM/controllers/AdminVentasController.java) y [AdminPrecioPizzaController](src/main/java/com/example/PizzUMBurgUM/controllers/AdminPrecioPizzaController.java). |
| Administrador inicial | [AdminSeeder](src/main/java/com/example/PizzUMBurgUM/configuracion/AdminSeeder.java). |

## Ejecución local

### 1. Preparar el entorno

Se necesita **JDK 21**, **Git** y **Docker con Docker Compose**. En Windows se puede utilizar Docker Desktop con el motor iniciado. `JAVA_HOME` debe apuntar a la carpeta del JDK.

```powershell
git clone https://github.com/valentinapenen/Lab-04-CI-CD.git
cd Lab-04-CI-CD
.\mvnw.cmd -v
```

Si el repositorio ya está clonado, abrir una terminal en su carpeta. El wrapper descarga Maven en la primera ejecución y las dependencias que falten requieren conexión a Internet.

### 2. Iniciar PostgreSQL

```powershell
docker compose up -d postgres
docker compose ps
```

La configuración está en [docker-compose.yml](docker-compose.yml). Expone PostgreSQL en `localhost:5432`, utiliza la base `pizzeria_db` y guarda sus datos en un volumen persistente. Compose inicia la base; la aplicación Java se ejecuta por separado.

La conexión de la aplicación está en [application.properties](src/main/resources/application.properties). Las credenciales actuales son de desarrollo y están definidas en esos archivos; separarlas por ambiente es parte del trabajo pendiente. Hibernate utiliza `ddl-auto=update`, por lo que puede modificar el esquema al arrancar. Usar una base de desarrollo.

Si ya existe un contenedor llamado `pizzeria-db` del proyecto original o el puerto 5432 está ocupado, resolver ese conflicto antes de iniciar otra instancia. No es necesario borrar datos para ejecutar este proyecto.

### 3. Iniciar la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir [http://localhost:8080/](http://localhost:8080/). Para detener la aplicación, usar `Ctrl+C` en esa terminal. Para detener la base conservando sus datos:

```powershell
docker compose stop postgres
```

En Linux o macOS, utilizar `./mvnw` en lugar de `.\mvnw.cmd`.

### 4. Preparar datos para recorrer la aplicación

1. Iniciar sesión con el administrador inicial. Su correo y contraseña se consultan en las propiedades `app.seed-admin.correo` y `app.seed-admin.contrasena`. El inicializador está habilitado con `app.seed-admin=true`; si encuentra un administrador con ese correo, no crea otro ni actualiza su contraseña.
2. Desde **Productos**, crear ingredientes disponibles. Para una pizza, preparar al menos una masa, una salsa y un queso con categoría `PIZZA` o `AMBOS`. Para una hamburguesa, preparar pan, carne y queso con categoría `HAMBURGUESA` o `AMBOS`.
3. Revisar los precios base de pizzas en `/admin/pizzas/precios`. Los tamaños son `CHICA`, `MEDIANA` y `GRANDE`; el servicio crea los valores iniciales cuando se consultan si aún no existen.
4. Cerrar sesión y registrar una cuenta de cliente con domicilio y tarjeta de prueba. La pantalla de registro requiere esos datos; usar información ficticia.
5. Armar una creación, revisar el carrito y confirmar el pedido seleccionando domicilio y tarjeta.
6. Iniciar sesión como administrador para avanzar su estado. Volver al cliente para consultar seguimiento e historial.

El inicializador crea el administrador, pero no carga un catálogo completo de ingredientes. Una base nueva necesita esa preparación antes de recorrer el flujo de compra.

## Rutas principales

| Pantalla o acción | Ruta |
| --- | --- |
| Bienvenida | `/` |
| Iniciar sesión / crear cuenta | `/iniciar-sesion` / `/crear-cuenta` |
| Panel del cliente | `/cliente/home` |
| Armar pizza o hamburguesa | `/cliente/hacer-creacion` |
| Carrito | `/cliente/carrito` |
| Historial / favoritos | `/cliente/historial` / `/cliente/favoritos` |
| Domicilios / tarjetas | `/cliente/domicilios` / `/cliente/tarjetas` |
| Panel del administrador | `/administrador/inicio` |
| Productos / precios de pizza | `/admin/productos` / `/admin/pizzas/precios` |
| Pedidos / ventas | `/pedidos` / `/admin/ventas` |

Las rutas del cliente y de administración requieren sesión. El login lo procesa Spring Security mediante `POST /procesar-login`; después se guarda `usuarioLogueado` en sesión y `/post-login` redirige al panel correspondiente.

El proyecto también expone endpoints REST heredados: `/api/bps/funcionarios`, `/api/dgi/tickets?fecha=YYYY-MM-DD` y `/api/tarjetas/{numero}`. Consultan datos locales; no son conexiones con servicios reales de BPS, DGI ni un banco.

## Pruebas

Las pruebas disponibles son:

- [PizzUmBurgUmApplicationTests](src/test/java/com/example/PizzUMBurgUM/PizzUmBurgUmApplicationTests.java): comprueba que el contexto de Spring carga.
- [ProductoServiceTests](src/test/java/com/example/PizzUMBurgUM/services/ProductoServiceTests.java): ocho casos unitarios de creación y actualización de precios. Aceptan cero y valores positivos, y rechazan negativos sin guardar cambios. Utilizan repositorios simulados.

Para ejecutarlas con una base H2 temporal, sin utilizar PostgreSQL ni crear el administrador inicial, desde PowerShell:

```powershell
$testOptions = @(
    '-Dspring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1'
    '-Dspring.datasource.driver-class-name=org.h2.Driver'
    '-Dspring.datasource.username=sa'
    '-Dspring.datasource.password='
    '-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect'
    '-Dspring.jpa.hibernate.ddl-auto=create-drop'
    '-Dspring.jpa.show-sql=false'
    '-Dapp.seed-admin=false'
)
.\mvnw.cmd test @testOptions
```

Ejecutar solamente `test`, sin esas opciones, utiliza la configuración PostgreSQL del proyecto. Todavía no hay un perfil de pruebas dedicado. Los reportes se generan en `target/surefire-reports/`.

La preparación inicial se verificó compilando con JDK 24 y destino Java 21, y ejecutando el test con H2: **1 test, 0 fallos y 0 errores**. Esto no demuestra el funcionamiento completo con PostgreSQL. La comprobación HTTP quedó pendiente porque el entorno de revisión impidió que Java estableciera una conexión local. No hay un linter configurado.

## Estado inicial y próximos pasos

La preparación inicial retiró clases y recursos sin referencias, imports sobrantes, comentarios temporales y mensajes de depuración con datos personales o hashes de contraseñas. También ajustó los metadatos del proyecto.

Para el Entregable 4 queda implementar:

- Pruebas unitarias de los módulos y pruebas de integración.
- Un `Jenkinsfile` declarativo versionado con el código.
- Ejecución automática ante cambios en GitHub.
- Despliegue a un ambiente de producción simulado.
- UAT y aprobación manual del despliegue si el grupo tiene tres integrantes.
- Documentación del pipeline, errores encontrados y demostración de los casos exitoso y fallido.

Antes de modificar un flujo, tener presentes estos puntos de la base heredada:

- `TarjetaController`, `DomicilioController` y `CreacionController` conservan rutas antiguas que apuntan a seis vistas inexistentes. Las pantallas actuales usan los controladores indicados en el mapa de componentes. Su retiro está pendiente de revisión.
- `UsuarioService.login()` conserva una implementación antigua sin llamadas encontradas. La autenticación vigente pasa por Spring Security.
- `ClienteDomicilioController` mantiene redirecciones a `/usuario/login`, una ruta inexistente; el acceso actual es `/iniciar-sesion`.
- Falta separar credenciales, creación del administrador y configuración de bases por ambiente, y comprobar el recorrido completo con PostgreSQL.

Para trabajar en esta entrega, crear las ramas de cada tarea desde `main` y abrir los pull requests en **Lab-04-CI-CD**. El repositorio original se mantiene como referencia del trabajo anterior.
