# Arquitectura real · Etapa 1

| Campo | Detalle |
|---|---|
| Tarea | T13 · Documentación técnica y manual de usuario de Etapa 1 |
| Versión | 1.0 · 03/11/2026 |
| Objeto | La arquitectura **construida**, leída del código del repositorio |
| Rama documentada | `feature/t12-pruebas-de-integracion-de-la-Etapa-1-y-correccion-de-defectos` (commit `cb51a68`) |
| Relación con el plan | El §8 del [plan de desarrollo](Plan_Desarrollo_por_Etapas.md) describe la estructura prevista. Donde difiere de esto, **manda este documento**; las diferencias están listadas en §9. |

---

## 1. Tecnología

| Elemento | Qué se usa |
|---|---|
| Lenguaje | Java, compilado con `maven.compiler.release` = **17** |
| Interfaz | Swing del JDK. Sin bibliotecas de UI; aspecto multiplataforma forzado en `Main` |
| Base de datos | SQL Server, por JDBC directo. Sin ORM |
| Persistencia | `mssql-jdbc` 13.2.0.jre11 |
| Configuración | `dotenv-java` 3.2.0 |
| Pruebas | `junit-jupiter` 5.11.4 |
| Construcción | Maven (`pom.xml`), `groupId` `com.mycompany`, `artifactId` `EscuelaVirtual`, versión `1.0-SNAPSHOT`, empaquetado `jar`. Sin wrapper y sin plugins de empaquetado |

El paquete raíz del código es **`com.mycompany.escuelavirtual`**.

## 2. Capas

```text
view  ──▶  controller  ──▶  service  ──▶  dao  ──▶  JDBC / SQL Server
  │             │              │           │
  └─────────────┴──────────────┴───────────┴──▶  model · model.enums · util · config
```

| Capa | Paquetes | Responsabilidad real |
|---|---|---|
| Presentación | `view`, `util.ui` | Swing. No contiene SQL ni reglas de negocio. Muestra el mensaje de la `NegocioException` que recibe |
| Coordinación | `controller` | Un controlador por módulo. **Fija el actor de la sesión en cada llamada**, de modo que la vista no pueda elegir en nombre de quién actúa |
| Negocio | `service` | Permisos, validación, transacciones y traducción de `SQLException` a `NegocioException` |
| Persistencia | `dao` | Solo lee y escribe filas. Lanza `SQLException`; no decide permisos |
| Transversal | `model`, `model.enums`, `util`, `util.db`, `config`, `session` | Entidades, enumerados, utilidades y configuración |

Dos reglas que el código respeta de forma consistente y que conviene no romper:

1. **El permiso se comprueba en el servicio, nunca en la vista.** Cada método público de servicio
   empieza con `Permisos.exigir(actor, …)` o `Permisos.exigirAccesoAEstudiante(...)`. Así ninguna
   ruta —ni la interfaz, ni una prueba, ni otro controlador— puede esquivar el control de acceso.
2. **La `SQLException` no sale de la capa de servicio.** `ServicioBase.consultar/ejecutar` la
   atrapa y la traduce; la vista solo ve `NegocioException` con un `CodigoError` estable.

## 3. Inventario de clases

### Arranque

| Clase | Papel |
|---|---|
| `Main` | Punto de entrada real. Comprueba `DatabaseConnection.isAvailable()`; si falla muestra el diálogo de error de conexión y no abre nada. Si responde, abre `LoginFrame` dentro de `invokeLater` |
| `EscuelaVirtual` | Segundo punto de entrada que solo delega en `Main`. Residuo de la plantilla de NetBeans |

### `config` y `util.db`

| Clase | Papel |
|---|---|
| `Config` | Lectura única de configuración. Precedencia: propiedad de sistema → variable de entorno → `.env` (o `.dotenv`). Valor vacío = no configurado. `require()` falla con `IllegalStateException` si falta la clave |
| `DatabaseConnection` | `getConnection()` con `DriverManager` sobre `DB_URL`/`DB_USER`/`DB_PASSWORD`. `isAvailable()` para la comprobación de arranque. **Sin pool de conexiones**: cada operación abre y cierra la suya |
| `Transaccion` | Ejecuta varias sentencias en una transacción JDBC. El servicio es el dueño: abre la conexión, la pasa a todos los DAO participantes y confirma solo al final. Cualquier excepción —incluida una `NegocioException` de validación— hace `rollback` de todo el bloque |

### `model` y `model.enums`

Entidades: `Usuario`, `Billetera`, `Producto`, `Logro`, `Recarga`, `Movimiento`.
Proyecciones de lectura (`record`): `DetalleLogro`, `DetalleMovimiento`, `DetalleRecarga`,
`CuadreBilletera`.
Enumerados: `Rol` (ESTUDIANTE, PADRE, DOCENTE, ADMIN), `MetodoPago` (EFECTIVO, TRANSFERENCIA,
YAPE, PLIN), `EstadoRecarga` (PENDIENTE, APROBADA, RECHAZADA), `TipoMoneda` (DINERO, MONEDAS).

### `dao`

`BaseDAO` (solo `connection()` y `close()`) más `UsuarioDAO`, `BilleteraDAO`, `ProductoDAO`,
`MovimientoDAO`, `RecargaDAO`, `LogroDAO`. Todos usan `PreparedStatement` con parámetros; no hay
SQL concatenado con datos de entrada.

### `service`

`ServicioBase` (plomería y traducción de errores) más nueve servicios: `AuthService`,
`UsuarioService`, `BilleteraService`, `RecargaService`, `CompraService`, `CanjeService`,
`LogroService`, `CatalogoService`, `ReporteService`.

`CanjeService` no duplica lógica: comprueba su propio permiso y delega en el método compartido de
`CompraService` con `TipoMoneda.MONEDAS`.

### `controller`

Nueve controladores, uno por módulo. Todos reciben el actor en el constructor y lo inyectan en
cada llamada al servicio. `LoginController` es el único que usa `SwingWorker`: autentica fuera
del hilo de interfaz y entrega el resultado ya en el EDT.

### `view` y `util.ui`

| Clase | Papel |
|---|---|
| `LoginFrame` + `PanelLogin` | Inicio de sesión. Valida formato antes de llamar al servicio; limpia el `char[]` de la clave tras usarlo |
| `MainFrame` | Cabecera, panel de inicio del rol ocupando toda la zona central, y barra de estado. **Sin navegación**: muestra una sola pantalla |
| `PanelInicioAdmin`, `PanelInicioPadre`, `PanelInicioEstudiante`, `PanelInicioDocente` | La funcionalidad real de la etapa. Es lo único que la ventana muestra |
| `PanelModuloPrototipo` | Pantalla prototipo de M02 a M09. **Ya no se instancia**: quedó sin usar al retirarse el árbol de navegación |
| `PanelTablaBase<T>` | Tabla reutilizable: búsqueda incremental, filtro por rango de fechas (oculto en todos los usos actuales), estado vacío y exportación a CSV con BOM UTF-8 y separador `;` de las filas visibles |
| `PanelFormularioBase` | Formulario con etiquetas, errores por campo y bloqueo durante una operación |
| `Estilos`, `Toast`, `DialogoConfirmacion` | Paleta y tipografías, avisos no modales, y diálogos de confirmación y error |

`MainFrame` resuelve el panel de inicio con un `switch` exhaustivo sobre `Rol`: añadir un rol
rompe la compilación en lugar de dejar un hueco en tiempo de ejecución.

La ventana no tiene árbol de módulos ni `CardLayout`: se retiraron el 03/11/2026 a pedido del
equipo, para que la pantalla del rol ocupe el ancho completo. Las consecuencias están en §7.

## 4. Esquema de base de datos, tal como lo usa el código

Los scripts SQL no están versionados (ver §10). Estas son las columnas que los DAO leen y
escriben, que es la fuente más fiable del esquema vigente.

| Tabla | Columnas usadas |
|---|---|
| `usuarios` | `id_usuario`, `nombres`, `apellidos`, `correo`, `password_hash`, `rol`, `grado`, `seccion`, `id_padre`, `estado`, `intentos_fallidos`, `bloqueado_hasta`, `ultimo_acceso` |
| `billeteras` | `id_billetera`, `id_estudiante`, `saldo_dinero`, `saldo_monedas` |
| `productos` | `id_producto`, `nombre`, `descripcion`, `precio_dinero`, `precio_monedas`, `stock`, `activo` |
| `movimientos` | `id_movimiento`, `id_billetera`, `id_producto`, `metodo_pago`, `cantidad`, `monto_total`, `fecha`, `id_usuario` |
| `recargas` | `id_recarga`, `id_billetera`, `id_padre`, `monto`, `metodo_pago`, `estado`, `fecha`, `id_admin_aprueba`, `fecha_aprobacion`, `motivo_rechazo` |
| `logros` | `id_logro`, `id_estudiante`, `id_docente`, `tipo`, `descripcion`, `monedas`, `fecha` |
| `vw_cuadre_billeteras` (vista) | `id_billetera`, `id_estudiante`, `saldo_dinero`, `dinero_calculado`, `saldo_monedas`, `monedas_calculadas` |

Notas de implementación:

- El rol, el método de pago y el estado de recarga se guardan como **texto**, con el nombre del
  enumerado de Java.
- Las fechas las pone el servidor con `SYSDATETIME()`, nunca el cliente. `UsuarioDAO.ahoraEnServidor()`
  consulta la hora del servidor para evaluar el bloqueo, de modo que el reloj del equipo del
  usuario no influya.
- El dinero es `BigDecimal`; `Validador` acota los montos a `DECIMAL(10,2)` (8 dígitos enteros y
  2 decimales). El correo se acota a 150 caracteres.
- La billetera se crea con saldos en cero la primera vez que se necesita, si no existía.

### Integridad de saldos

Las operaciones que mueven dinero o stock se ejecutan dentro de `Transaccion` y bloquean primero
la fila con `WITH (UPDLOCK, ROWLOCK)` antes de leer el saldo o el stock, para que dos operaciones
simultáneas no se pisen. La secuencia de una compra o canje es:

1. Bloquear el producto y comprobar que está activo y que hay stock.
2. Bloquear la billetera del estudiante (crearla si no existe).
3. Comprobar que el producto admite esa forma de pago y que el saldo alcanza.
4. Descontar el saldo, descontar el stock e insertar el movimiento con el `id_usuario` que lo
   ejecutó.

Todo en la misma transacción: una validación que falla en el paso 3 deshace lo anterior. La
aprobación de una recarga y el registro de un logro siguen el mismo patrón.

El descuadre se vigila con `vw_cuadre_billeteras`, que compara el saldo guardado con el
recalculado. `ReporteService.descuadres()` devuelve las filas que no cuadran y el panel del
administrador las muestra como indicador; su valor esperado es siempre 0.

## 5. Autenticación

`AuthService.login(correo, clave)`:

1. Valida el formato del correo y que la clave no esté vacía.
2. Busca por correo normalizado (sin espacios, en minúsculas).
3. Si no existe, lanza `CREDENCIALES_INVALIDAS` — **el mismo error que una clave incorrecta**, para
   no revelar qué correos están registrados.
4. Si `bloqueado_hasta` es futuro según la hora del servidor, lanza `CUENTA_BLOQUEADA` con los
   minutos restantes.
5. Si `estado = 0`, lanza `CUENTA_INACTIVA`.
6. Si la clave no verifica, suma el intento fallido **en una sola sentencia SQL** —para que dos
   intentos simultáneos no pierdan la cuenta— y bloquea la cuenta al llegar a `MAX_INTENTOS`.
7. Si verifica, reinicia el contador y actualiza `ultimo_acceso`.

Parámetros: `MAX_INTENTOS = 3`, `MINUTOS_BLOQUEO = 15`.

Las contraseñas se almacenan con **PBKDF2WithHmacSHA256**, 120 000 iteraciones, salt aleatorio de
16 bytes y hash de 256 bits, en el formato `iteraciones$saltBase64$hashBase64`. La comparación es
en tiempo constante con `MessageDigest.isEqual`; un hash malformado devuelve `false` en lugar de
lanzar una excepción. El log registra el `id_usuario` de los accesos correctos y fallidos, nunca
la contraseña.

## 6. Autorización

`Permisos.Accion` es la matriz de control de acceso. La comprobación es por rol, y los datos de un
estudiante concreto pasan además por `exigirAccesoAEstudiante`.

| Acción | ADMIN | PADRE | ESTUDIANTE | DOCENTE |
|---|:---:|:---:|:---:|:---:|
| `VER_BILLETERA_PROPIA` | — | — | Sí | — |
| `VER_BILLETERA_ESTUDIANTE` | Sí | Sí | — | — |
| `REGISTRAR_RECARGA` | Sí | Sí | — | — |
| `RESOLVER_RECARGA` | Sí | — | — | — |
| `COMPRAR_CON_DINERO` | — | — | Sí | — |
| `CANJEAR_CON_MONEDAS` | — | — | Sí | — |
| `VER_LOGROS_PROPIOS` | — | — | Sí | — |
| `VER_LOGROS_ESTUDIANTE` | Sí | Sí | — | — |
| `VER_LOGROS_OTORGADOS` | Sí | — | — | Sí |
| `REGISTRAR_LOGRO` | Sí | — | — | Sí |
| `LISTAR_ESTUDIANTES` | Sí | — | — | Sí |
| `ADMINISTRAR_USUARIOS` | Sí | — | — | — |
| `ADMINISTRAR_CATALOGO` | Sí | — | — | — |
| `VER_HISTORIAL_ESTUDIANTE` | Sí | Sí | — | — |
| `GENERAR_REPORTES` | Sí | Sí | — | — |
| `VER_CUADRE_BILLETERAS` | Sí | — | — | — |

`exigirAccesoAEstudiante(actor, idEstudiante, esSuHijo)` resuelve el acceso a los datos de un
alumno: el administrador ve a todos; el estudiante solo a sí mismo; el padre solo a sus hijos, y
el vínculo se comprueba contra `usuarios.id_padre` en la base de datos, no en la pantalla; el
docente no accede a billeteras ni historiales.

Candados adicionales de `UsuarioService`: un administrador no puede quitarse a sí mismo el rol
ADMIN, no puede darse de baja a sí mismo, y no se puede dejar el sistema sin ningún administrador
activo.

## 7. Navegación

**La aplicación no tiene navegación.** `MainFrame` muestra el panel de inicio del rol y nada
más; el `JTree` de módulos y el `CardLayout` que abría sus tarjetas se retiraron el 03/11/2026.

`Modulo` sigue siendo la matriz de módulos visibles por rol, sin ninguna dependencia de Swing, y
lo leen las pruebas CP-01 y CP-18. Hoy ninguna pantalla lo consume: es la especificación de la
navegación que la Etapa 2 debe reconstruir cuando M02–M09 tengan pantalla propia.

| Módulo | Título | Roles que lo ven |
|---|---|---|
| M01 | Inicio y sesión | los cuatro |
| M02 | Usuarios | ADMIN |
| M03 | Billetera | ESTUDIANTE, PADRE, ADMIN |
| M04 | Recargas | PADRE, ADMIN |
| M05 | Tienda y compras | ESTUDIANTE, ADMIN |
| M06 | Logros | los cuatro |
| M07 | Catálogo y canjes | ESTUDIANTE, ADMIN |
| M08 | Supervisión | PADRE, DOCENTE, ADMIN |
| M09 | Reportes y CSV | ADMIN |

Módulos por rol según el enumerado: ADMIN 9, PADRE 5, ESTUDIANTE 5, DOCENTE 3.

**Solo M01 tiene pantalla, y es la única accesible.** La funcionalidad de los demás módulos que
ya existe está implementada dentro de los paneles de inicio: el ABU de usuarios, la aprobación de
recargas y el catálogo en el del administrador; la billetera, la tienda y los logros en el del
estudiante; la supervisión y la solicitud de recarga en el del padre; el registro de logros en el
del docente. `PanelModuloPrototipo` ya no se instancia desde ningún sitio.

Esto tiene una consecuencia de diseño que la Etapa 2 debe deshacer: los paneles de inicio son
pantallas grandes y multipropósito (`PanelInicioAdmin` tiene 730 líneas y tres pestañas). Al
construir las pantallas de M02 a M09 hay que mover esa lógica, no duplicarla.

## 8. Pruebas

| Clase | Nivel | Qué cubre |
|---|---|---|
| `util.PermisosTest` | Unitario | Matriz de acciones y acceso a datos de un estudiante |
| `util.ModuloTest` | Unitario | Módulos visibles por rol |
| `util.ui.PanelTablaBaseTest` | Unitario | Filtrado, selección y estado vacío de la tabla base |
| `service.AuthServiceTest` | Unitario con doble en memoria | Intentos fallidos, bloqueo, cuenta inactiva |
| `service.UsuarioServiceTest` | Unitario con doble en memoria | Validación, permisos y candados del ABU |
| `dao.UsuarioDAOTest` | Integración | Consultas reales contra `usuarios` |
| `service.GuionT11Test` | Integración | Guion de verificación login → usuarios |
| `service.PruebasEtapa1Test` | Integración | Casos de salida de la Etapa 1 |

Las pruebas de integración se omiten solas si no hay conexión a SQL Server, y las que crean datos
usan su propio usuario de QA y lo eliminan al terminar. `util.ui.DemoComponentes` no es una
prueba: es una ventana manual de revisión visual.

## 9. Diferencias con el §8 del plan de desarrollo

El plan describe una estructura que el código no sigue. Quien lea el plan como referencia técnica
se equivocará en estos ocho puntos:

| # | El plan dice | El código tiene |
|---|---|---|
| 1 | Paquete `com.escuela.virtual`, clase principal `com.escuela.virtual.Main` | Paquete `com.mycompany.escuelavirtual`, clase principal `com.mycompany.escuelavirtual.Main` |
| 2 | `util/` contiene `DatabaseConnection`, `SessionManager` y `Transaccion` | `util.db` contiene `DatabaseConnection` y `Transaccion`; `SessionManager` vive en su propio paquete `session` |
| 3 | No menciona `util.ui` ni `config` como paquetes propios | Ambos existen: `util.ui` con 5 clases de componentes y `config` con `Config` |
| 4 | Enumerado `TipoMovimiento` | Enumerado `TipoMoneda` (DINERO, MONEDAS) |
| 5 | `view/` con subcarpetas `dialog/` y `panel/` | `view/` plano, con 8 clases; los componentes reutilizables están en `util.ui` |
| 6 | Servicios `Notificacion` y `Conciliacion` | No existen. La conciliación se consulta con `ReporteService` sobre `vw_cuadre_billeteras`; no hay notificaciones |
| 7 | `scripts/` con `00_usuario_bd.sql` a `04_extension_notificaciones.sql`; `mvnw`, `mvnw.cmd`, `.mvn/` | Ninguno de esos archivos está en el repositorio |
| 8 | `src/test/` dividido en `unit/` e `integration/` | Las pruebas están junto al paquete que prueban (`service`, `dao`, `util`, `util.ui`), sin esa división |

Los puntos 1 a 6 son decisiones ya tomadas y en producción: lo que corresponde es actualizar el
plan, no renombrar el código a estas alturas de la etapa. Los puntos 7 y 8 son deuda real.

## 10. Deuda técnica conocida

| # | Asunto | Impacto | Nota |
|---|---|---|---|
| 1 | **Los scripts SQL no están versionados.** No hay carpeta `scripts/` | Alto: no se puede reconstruir la base desde el repositorio ni verificar el esquema | Bloquea la reproducibilidad del entorno y la verificación del DDL contra este documento |
| 2 | **Sin Maven ni wrapper en el entorno de desarrollo** (T01 abierto) | Alto: el criterio «`mvn verify` verde» no se puede dar por cumplido | La compilación se hace con `javac` contra los JAR de `~/.m2`; la suite de pruebas no se puede lanzar sin un lanzador de JUnit |
| 3 | **Las consultas se ejecutan en el hilo de Swing** (D-T11-02, S3) | Medio: la ventana se congela con la base de datos lenta | Solo `LoginController` usa `SwingWorker`. Se corrige al dividir los paneles de inicio en S01–S06 |
| 4 | **Fechas sin conversión de zona horaria** (D-T11-03, S3) | Medio: afecta a lo que se muestra y a los futuros reportes CSV | Debe resolverse antes de S06. La decisión —fijar la zona del servidor o almacenar con desfase y convertir al mostrar— corresponde a base de datos con el líder |
| 5 | **Sin pool de conexiones** | Medio con varios usuarios | Cada operación abre y cierra su conexión |
| 6 | **Sin gestión de contraseñas** | Medio | No hay cambio ni restablecimiento de clave; al editar un usuario el campo se deshabilita |
| 7 | **`EscuelaVirtual.java` duplica el punto de entrada** | Bajo | Residuo de la plantilla de NetBeans; solo delega en `Main` |
| 8 | **Paneles de inicio multipropósito** | Bajo hoy, alto al crecer | `PanelInicioAdmin` concentra tres pestañas y 730 líneas. Las pantallas de M02–M09 deben absorber esa lógica, no copiarla |
| 9 | **Sin navegación y con una pantalla huérfana** | Medio | Al retirarse el árbol de módulos, M02–M09 quedaron inalcanzables y `PanelModuloPrototipo` sin usar. Decae el criterio de salida «9 paneles prototipo» de la Etapa 1 y las capturas de evidencia de T11/T12 ya no reflejan la ventana. La Etapa 2 debe decidir qué navegación pone en su lugar |
| 10 | **Sin empaquetado** | — | No hay JAR ejecutable ni instalador; el `pom.xml` no configura plugins de empaquetado. Es T14 |
| 11 | **Las reglas de dependencia entre capas se verifican a ojo** | Bajo | El plan contempla ArchUnit como opción; no está |
