# Plan de Desarrollo por Etapas

**Sistema de Gestión Escolar con Economía Virtual e Incentivos Educativos**

| Campo | Detalle |
|---|---|
| Documento | Plan de desarrollo, organización del trabajo y aseguramiento de la calidad |
| Versión | 1.0 |
| Fecha | 26/10/2026 |
| Estado | Propuesto para revisión y aprobación del equipo |
| Documentos fuente | Arquitectura_Sistema_Gestion_Escolar.docx (v1.0) y Cronograma Actividades.xlsx |
| Repositorio | github.com/LuisRR1996/Sistema-de-Gesti-n-Escolar-con-Econom-a-Virtual |
| Alcance temporal | Etapa 1: 21/10/2026 – 04/11/2026. Etapa 2: 10/11/2026 – 02/12/2026 |
| Equipo | BE (Backend), UI (Interfaces), BD (Datos), QA (Pruebas) y LP (Líder de proyecto) |
| Audiencia | Equipo de desarrollo, docente asesor y responsables académicos |

<!-- pagebreak -->

<!-- toc -->

## 1. Resumen ejecutivo

### 1.1 Objetivo del plan

Convertir la arquitectura aprobada y el cronograma de actividades en un plan ejecutable: qué se construye, en qué orden, con qué criterios de aceptación, quién es responsable y cómo se controla el riesgo. El plan mantiene las fechas y los identificadores del cronograma (T01–T14 y S01–S10), pero corrige las brechas detectadas entre ambos documentos y el estado real del repositorio.

### 1.2 Diagnóstico en una página

| Dimensión | Estado al 26/10/2026 | Lectura |
|---|---|---|
| Arquitectura funcional | Completa: 4 roles, 9 módulos, 6 tablas, 3 capas y 15 casos de prueba | Verde |
| Cronograma | 24 tareas con fechas y responsables, del 21/10 al 02/12 | Verde con alerta |
| Código | Proyecto Maven con `Config` y un `main` de saludo; sin modelo, DAO, servicios, controladores ni vistas | Rojo |
| Base de datos | Instancia SQL Server 2025 en contenedor, base `EscuelaVirtual` con 6 tablas y datos; sin scripts versionados ni deltas de ADR-05 | Amarillo |
| Build | `pom.xml` solo con `dotenv-java`; sin driver JDBC, sin JUnit, sin wrapper y con `release 25` | Rojo |
| Pruebas | Sin carpeta `src/test`, sin casos automatizados y sin base de pruebas | Rojo |
| Documentación | Arquitectura y cronograma existen, pero el plan de desarrollo no estaba escrito | Amarillo |
| Entorno | JDK 25, Docker y SQL Server 2025 en el puerto 1433; Maven no está en el PATH y `sqlcmd` no está en el host | Amarillo |
| Gestión | Repositorio `main` sin tablero, sin issues, sin etiquetas y sin releases | Amarillo |

La conclusión es directa: la especificación está madura y la base ya existe en el contenedor, pero el código y los scripts no están versionados. Las tareas T01 y T02, previstas para el 21/10, están vencidas; T03–T05 vencen el 27/10. El plan incluye un plan de recuperación de ocho días hábiles para cerrar la Etapa 1 el 04/11 sin recortar la seguridad ni la integridad de los saldos.

### 1.3 Decisiones que deben tomarse antes del 27/10/2026

| ID | Decisión | Recomendación | Responsable |
|---|---|---|---|
| D-01 | Versión de Java, paquete base y nombre de la clase principal | Java 17 (`release 17`), paquete `com.escuela.virtual`, clase `Main`; renombrar ahora que no hay código | LP + BE |
| D-02 | Fuente única de configuración | Mantener `Config` y `.env`; retirar `config/db.properties` y las contraseñas de la documentación | BE + LP |
| D-03 | Conciliación de saldos | Mantener las 6 tablas y conciliar con recargas aprobadas + logros − movimientos; no rediseñar el ledger en la Etapa 1 | BE + BD |
| D-04 | Alcance de M10a y M10b | Tratarlas como extensión condicionada, con banderas de activación y modo simulado si no hay credenciales | LP + BE |
| D-05 | Rebalanceo de carga | Transferir tareas de UI, BD y QA para que BE+QA no concentre el 72 % del esfuerzo | LP |
| D-06 | Módulos visibles para ADMIN | Incluir M01–M09 en el árbol del administrador, para que coincida con CP-01 y con la matriz de permisos | UI + BE |
| D-07 | Bloqueo por intentos fallidos | Mantenerlo en el MVP y añadir las columnas de control en el DDL de T02 | BE + BD |

### 1.4 Acciones inmediatas (26–28/10/2026)

| Prioridad | Acción | Responsable | Evidencia de cierre |
|---|---|---|---|
| 1 | Aprobar D-01 a D-07 y registrar las ADR del capítulo 4 | LP | Acta o comentario en el issue de decisión |
| 2 | Cerrar T01: dependencias, Java 17, paquete, wrapper, `Main` y `DatabaseConnection` | BE | `./mvnw clean verify` y arranque con mensaje de conexión |
| 3 | Cerrar T02: DDL idempotente, restricciones, índices y vista de cuadre | BD | Script versionado y comparado con la base existente |
| 4 | Cerrar T03: datos iniciales y hashes PBKDF2 generados con herramienta | BD + BE | Login de los 7 usuarios de prueba |
| 5 | Cerrar T04 y T05: modelo, enums, sesión, esqueleto de capas | BE | Compilación y prueba unitaria de `PasswordUtil` |
| 6 | Prototipar las 9 pantallas base y los componentes reutilizables | UI | Prototipo navegable con datos falsos |
| 7 | Implementar login real contra SQL Server | BE + UI | CP-01, CP-02 y CP-16 aprobados |
| 8 | Crear el tablero, los issues T01–T14 y las etiquetas | LP | Tablero con tareas y responsables |

### 1.5 Definición de éxito

El plan se considera cumplido cuando: la Etapa 1 cierra el 04/11 con login, usuarios, navegación por rol, base de datos y prototipo de pantallas; la Etapa 2 cierra el 02/12 con los módulos M03–M09 operativos, los casos CP-01 a CP-20 aprobados, reportes CSV, instalador y documentación; y ningún saldo puede quedar negativo ni descuadrado.

<!-- pagebreak -->

## 2. Análisis de los documentos fuente

### 2.1 Qué aporta cada documento

| Documento | Aporte principal | Uso en este plan |
|---|---|---|
| Arquitectura_Sistema_Gestion_Escolar.docx | Propósito, alcance, actores, permisos, 9 módulos, 14 reglas de negocio, 6 tablas, DDL T-SQL, diseño de JPanel, seguridad, pruebas, riesgos y anexos | Base técnica y de aceptación; fuente de las tareas y de los criterios de calidad |
| Cronograma Actividades.xlsx | Dos etapas, 24 tareas (T01–T14 y S01–S10), responsables, fechas y leyenda de alcance | Línea base de planificación; fuente de fechas, secuencia y entregables |
| Código y configuración del repositorio | `pom.xml`, `Config`, `.env.example`, `EscuelaVirtual.java` y estructura Maven | Diagnóstico de partida y ajuste de las primeras tareas |

### 2.2 Convergencias que se conservan

- Un solo `MainFrame` con `JTree` de módulos por rol y `CardLayout`; todas las pantallas son `JPanel`.
- Arquitectura de tres capas con MVC, DAO y servicios; la vista no ejecuta SQL.
- Dos saldos independientes: dinero para compras y monedas para recompensas; nunca se convierten entre sí.
- Seis tablas centrales: `usuarios`, `billeteras`, `productos`, `logros`, `recargas` y `movimientos`.
- SQL Server como base de datos, JDBC con `PreparedStatement` y transacciones explícitas.
- Contraseñas con PBKDF2, control de acceso por rol y protección de los datos de menores.
- Etapa 1 orientada a base de datos, diseño de interfaces, login y un módulo; Etapa 2 a los módulos restantes y a la entrega.

### 2.3 Brechas e inconsistencias detectadas

| ID | Hallazgo | Impacto | Resolución propuesta | Dueño | Cuándo |
|---|---|---|---|---|---|
| I-01 | La arquitectura pide Java 17, paquete `com.escuela.virtual` y clase `Main`; el repositorio usa Java 25, paquete `com.mycompany.escuelavirtual` y `EscuelaVirtual` | El JAR no arranca en equipos con JDK 17 y el código se separa de la documentación | ADR-01: renombrar ahora, fijar `release 17`, añadir wrapper | BE | 26/10 |
| I-02 | La arquitectura configura `config/db.properties`; el repositorio ya implementa `Config` con `.env` | Duplicidad de configuración y riesgo de secretos en dos sitios | ADR-02: `.env` como fuente única; actualizar secciones 4.11, 6.3 y 7.2 | BE + LP | 26/10 |
| I-03 | La arquitectura incluye la contraseña `ClaveSegura2026*` en el ejemplo de configuración | Secreto versionado en un documento que se comparte | Sustituir por marcador, rotar la clave si fue real y prohibir secretos en documentos | LP | 26/10 |
| I-04 | El plan de la sección 9 de la arquitectura ubica M01, M02 y M09 en la Fase II; el cronograma ubica M01 y M02 en la Etapa 1 y M09 en la Etapa 2 | Alcance de etapa ambiguo y posible duplicación de trabajo | El cronograma es la línea base; corregir la sección 9 de la arquitectura | LP | 27/10 |
| I-05 | La arquitectura define 9 módulos, pero el cronograma añade M10a y M10b (correo y WhatsApp) | Cambio de alcance sin diseño, dependencias ni tratamiento de datos personales | ADR-07: extensión condicionada, tabla de notificaciones y modo simulado | LP + BE | 27/10 |
| I-06 | La tabla de módulos asigna 7 módulos a ADMIN, la matriz de permisos le permite M03 y M06, y CP-01 exige 9 módulos | El árbol de navegación y las pruebas no coinciden | Matriz final del anexo C: ADMIN ve M01–M09 | UI + BE | 27/10 |
| I-07 | `PanelMisLogros` aparece para Padre, pero la tabla de módulos no le asigna M06; `PanelHijoLogros` aparece en el árbol y no existe en la tabla | Pantallas huérfanas y permisos contradictorios | Unificar: padre consulta logros de sus hijos en M06; eliminar `PanelHijoLogros` | UI | 28/10 |
| I-08 | M08 y M09 comparten `PanelReportes` | Doble responsabilidad y navegación confusa | M08 = historial operativo; M09 = reportes y CSV | UI + BE | 28/10 |
| I-09 | RN-05 y el criterio 8.3 exigen que la suma de `movimientos` cuadre con el saldo, pero las recargas y los logros no generan movimientos | La conciliación es imposible tal como está escrita y CP-20 fallaría | ADR-04: conciliación con recargas aprobadas + logros − movimientos; vista `vw_cuadre_billeteras` | BE + BD | 27/10 |
| I-10 | La seguridad exige bloquear la cuenta tras 3 intentos, pero `usuarios` no tiene columnas de intentos ni bloqueo | Requisito de seguridad no implementable | ADR-05: añadir `intentos_fallidos`, `bloqueado_hasta` y `ultimo_acceso` | BD | 27/10 |
| I-11 | La arquitectura dice que los servicios reciben el usuario de la sesión, pero las firmas de ejemplo no lo reciben | Los permisos quedarían solo en la interfaz o en estado global | ADR-03: `Usuario actor` como primer parámetro y clase `Permisos` | BE | 27/10 |
| I-12 | El `pom` de la arquitectura usa `mssql-jdbc 12.6.1.jre17`, artefacto que no existe en Maven Central; solo hay `jre8` y `jre11` para 12.6.1 | El build falla al resolver la dependencia | Usar `12.10.2.jre11` o `13.6.1.jre11`, compatibles con JDK 17 | BE | 26/10 |
| I-13 | El script de datos iniciales no puede generar hashes PBKDF2 desde T-SQL; la arquitectura afirma que usa `PasswordUtil` | Se acabaría guardando texto plano o hashes inválidos | Herramienta `GeneradorHash` y hashes precalculados en el script | BE + BD | 27/10 |
| I-14 | El ejemplo `UsuarioDAO.listar` contiene SQL inválido (`(?, NULL) IS NULL`) | Error de copia y pega en la primera implementación | Corregir el ejemplo y usar dos consultas o parámetros nullable bien formados | BE | 27/10 |
| I-15 | Nombres inconsistentes: `billetera` y `billeteras`, `util.db` y `util` | Confusión en paquetes, tablas y consultas | Convención única: tabla `billeteras`, paquete `util` | BD + BE | 27/10 |
| I-16 | CP-14 fuerza un error eliminando un producto, pero las claves foráneas `ON DELETE NO ACTION` lo impiden | La prueba de rollback no se puede ejecutar como está escrita | Provocar el fallo con un trigger de prueba o un DAO que falle; documentar el método | QA | 30/10 |
| I-17 | El cronograma asigna una jornada a módulos completos y concentra BE y QA en la misma persona | 176 h estimadas para una sola persona; el plan es inviable si se mantiene | Plan de recuperación y rebalanceo de los capítulos 5 y 6 | LP | 27/10 |

### 2.4 Los cinco bloqueadores reales

1. **Build no portable:** Java 25, paquete distinto y dependencia JDBC inexistente. Se corrige en T01.
2. **Base de datos no reproducible:** existe en el contenedor, pero sin DDL versionado, sin deltas de ADR-05 y sin datos reproducibles. Se corrige en T02 y T03.
3. **Permisos sin dueño claro:** la matriz, el árbol y los servicios no coinciden. Se corrige con ADR-03 y el anexo C.
4. **Conciliación de saldos mal definida:** RN-05 y el criterio 8.3 no se pueden cumplir con el esquema actual. Se corrige con ADR-04.
5. **Carga desbalanceada y fechas vencidas:** T01 y T02 ya están atrasadas y BE+QA concentra el 72 % del esfuerzo. Se corrige con el plan de recuperación y el rebalanceo.

<!-- pagebreak -->

## 3. Estado actual del repositorio

### 3.1 Inventario verificado

| Elemento | Estado | Evidencia |
|---|---|---|
| `pom.xml` | Parcial | `release 25`, `mainClass` de `com.mycompany.escuelavirtual.EscuelaVirtual`, solo dependencia `dotenv-java 3.2.0` |
| Clase principal | Placeholder | `EscuelaVirtual.main` imprime «Hello World!» |
| Configuración | Implementada | `config/Config.java` lee propiedades del sistema, variables de entorno y `.env`; precedencia documentada |
| Plantilla de entorno | Implementada | `.env.example` con `APP_NAME`, `APP_ENV`, `LOG_LEVEL`, `DB_URL`, `DB_USER`, `DB_PASSWORD` |
| Modelo | 0 % | No existen `model`, `model.enums` ni entidades |
| Persistencia | 0 % | No existen `dao`, `DatabaseConnection` ni scripts SQL |
| Servicios | 0 % | No existen `service` ni reglas de negocio |
| Controladores | 0 % | No existen `controller` |
| Vistas | 0 % | No existen `view`, `LoginFrame`, `MainFrame` ni paneles |
| Pruebas | 0 % | No existe `src/test/java` |
| Recursos | 0 % | No existe `src/main/resources` |
| Scripts | 0 % | No existen `scripts/01`, `02` ni `03`; la base del contenedor no es reproducible desde el repositorio |
| Base de datos en contenedor | Existente | `sqlserver` (SQL Server 2025) escucha en 1433; base `EscuelaVirtual` con 6 tablas y 7 usuarios |
| Documentación | Parcial | Arquitectura y cronograma en `docs/`; este plan se añade; la carpeta está sin versionar |
| Git | Activo | Rama `main`, remoto de GitHub, último commit merge del 24/10; sin tags ni releases |
| Calidad | 0 % | Sin CI, sin tablero, sin issues, sin revisión de pares |

### 3.2 Brecha contra la arquitectura

| Componente de la arquitectura | Esperado | Real | Tarea que lo cierra |
|---|---|---|---|
| Base de datos `EscuelaVirtual` | 6 tablas, restricciones, índices y vista de cuadre | Existe con PK, UQ e índices; sin vista de cuadre, sin deltas de ADR-05 y sin scripts versionados | T02 |
| Datos iniciales | 7 usuarios, 3 billeteras y catálogo | Existen en el contenedor (7/3/10), pero no están versionados | T03 |
| Modelo | 6 entidades y 4 enums | No existe | T04 |
| Sesión y utilidades | `SessionManager`, `PasswordUtil`, `Validador` | No existen | T04 |
| Esqueleto de capas | Paquetes y contratos | No existe | T05 |
| Login | `LoginFrame` + `AuthService` + SQL Server | No existe | T08 |
| Navegación | `MainFrame` + `JTree` + `CardLayout` | No existe | T09 |
| Usuarios | ABU y vínculo padre–hijo | No existe | T10 |
| Billetera | Saldos, movimientos y logros | No existe | S01 |
| Recargas | Registro y aprobación | No existe | S02 |
| Tienda | Catálogo, carrito y compra transaccional | No existe | S03 |
| Recompensas | Logros, catálogo y canjes | No existe | S04 |
| Supervisión | Historial por rol | No existe | S05 |
| Reportes | Indicadores y CSV | No existe | S06 |
| Pruebas | Unitarias, integración y CP-01 a CP-20 | No existe | T12 y S10 |
| Empaquetado | JAR y instalador | No existe | T14 y S09 |

### 3.3 Entorno de desarrollo verificado

| Elemento | Resultado | Consecuencia |
|---|---|---|
| JDK | OpenJDK 25.0.4.1 | Compila, pero hay que fijar `release 17` para portabilidad |
| Maven | No está en el PATH | NetBeans usa su Maven embebido; conviene añadir Maven Wrapper |
| Docker | Disponible | Contenedor `sqlserver` con SQL Server 2025 y 4 GB de memoria |
| SQL Server | Activo en 1433 | La base `EscuelaVirtual` existe con 6 tablas y datos; se puede validar de inmediato |
| `sqlcmd` | No instalado en el host | Usar `docker exec sqlserver /opt/mssql-tools18/bin/sqlcmd` o Azure Data Studio |
| Sistema | Linux | El instalador final se compila en Windows; el desarrollo es multiplataforma |

### 3.4 Lectura de estado

El repositorio no está «casi listo»: está correctamente inicializado y la base ya existe en el contenedor, pero el código y los scripts no están versionados. La ventaja es que el renombrado de paquete, el cambio de Java y la elección de configuración todavía son baratos, y que el esquema y los datos se pueden comparar contra una instancia real. La desventaja es que T01 y T02 ya están vencidas y T03–T05 vencen mañana. Por eso el plan de recuperación empieza el mismo 26/10 y no espera a la próxima sesión.

<!-- pagebreak -->
## 4. Decisiones de arquitectura

Las siguientes ADR cierran las brechas del capítulo 2. Se consideran aprobadas cuando el líder las registra en el acta del 27/10; hasta entonces son la propuesta técnica del plan.

### ADR-01 · Base tecnológica, paquete y build

**Contexto.** El repositorio compila con JDK 25 y paquete `com.mycompany.escuelavirtual`; la arquitectura exige JDK 17, paquete `com.escuela.virtual`, clase `Main` y un driver JDBC que no existe en la versión indicada.

**Decisión.** Fijar `maven.compiler.release=17`, renombrar el paquete base a `com.escuela.virtual`, la clase principal a `Main` y el `groupId` a `com.escuela.virtual`. Añadir Maven Wrapper, `mssql-jdbc 12.10.2.jre11`, JUnit Jupiter 5.11.4, `maven-compiler-plugin`, `maven-surefire-plugin`, `maven-failsafe-plugin` y `maven-shade-plugin`. El artefacto `12.10.2.jre11` es compatible con JDK 17 y es la versión estable más reciente de la rama 12.

**Consecuencias.** Un refactor de dos archivos ahora evita decenas de referencias después. Hay que actualizar la configuración de ejecución de NetBeans, el `mainClass` del POM y la documentación. Si el equipo decide conservar `com.mycompany`, la alternativa es corregir la arquitectura, no dejar ambas versiones.

### ADR-02 · Configuración y secretos

**Contexto.** La arquitectura propone `config/db.properties` y contiene una contraseña de ejemplo; el repositorio ya tiene `Config` con `.env`, `.env.example` y `.gitignore`.

**Decisión.** `.env` es la única fuente de configuración local. `Config` mantiene la precedencia propiedad del sistema > variable de entorno > archivo `.env` > valor por defecto. `DatabaseConnection` consume `Config.require("DB_URL")`, `DB_USER` y `DB_PASSWORD`, y falla con un mensaje accionable si falta algo. `.env.example` se versiona con valores vacíos; `.env` nunca se versiona. Se elimina la contraseña real de la arquitectura y se actualizan las secciones 4.11, 6.3, 7.2 y 7.4.

**Consecuencias.** Una sola configuración para desarrollo y producción; el instalador entregará una plantilla y un primer arranque guiado. Si la institución exige un archivo `.properties`, se puede añadir un adaptador sin cambiar el resto del código.

### ADR-03 · Autorización en la frontera del servicio

**Contexto.** La arquitectura afirma que los servicios reciben el usuario de la sesión, pero las firmas de ejemplo no lo hacen. Si el controlador solo oculta botones, un cambio de rol en la base o una llamada directa al servicio puede saltarse el control.

**Decisión.** Todo método de servicio que modifica o consulta datos sensibles recibe `Usuario actor` como primer parámetro. La clase `Permisos` (utilidad pura, sin estado) responde `puedeVerModulo`, `puedeConsultarBilletera`, `puedeComprar`, `puedeCanjear`, `puedeRegistrarLogro`, `puedeAprobarRecarga`, `puedeAdministrar` y `esPropietarioOPadre`. El servicio verifica rol y vínculo padre–hijo antes de tocar la base. `SessionManager` se usa solo en controladores y vistas para obtener el actor; no se consulta desde los DAO.

**Consecuencias.** Las firmas cambian respecto de la arquitectura y hay que actualizarlas en la documentación. Las pruebas de denegación se vuelven directas y no dependen de la interfaz.

### ADR-04 · Conciliación de saldos sin rediseñar el modelo

**Contexto.** RN-05 dice que toda modificación de saldo genera un movimiento, pero las recargas y los logros se registran en sus propias tablas. El criterio 8.3 exige que la suma de movimientos coincida con el saldo actual, lo que hoy es falso por diseño.

**Decisión.** Mantener las seis tablas y definir la conciliación así:

- `saldo_dinero = suma de recargas APROBADAS − suma de movimientos con metodo_pago = 'DINERO'`.
- `saldo_monedas = suma de logros − suma de movimientos con metodo_pago = 'MONEDAS'`.

Cada operación se ejecuta en una transacción única y actualiza el saldo con una condición (`saldo >= monto`) para que la base rechace un saldo negativo incluso con dos sesiones. Se crea la vista `vw_cuadre_billeteras` y una prueba CP-20 que compara saldo y cálculo para todas las billeteras. Se actualizan RN-05 y el criterio 8.3 para hablar de «registros auditables» (recarga, logro o movimiento), no solo de `movimientos`. El ledger unificado queda como extensión opcional del anexo B.

**Consecuencias.** Menos rework y compatibilidad con los scripts y las consultas ya documentadas. La contrapartida es que la auditoría se reparte en tres tablas; por eso se añaden campos de actor y de aprobación en el DDL de ADR-05.

### ADR-05 · Ajustes mínimos al esquema

**Contexto.** Faltan columnas para bloqueo de cuenta, aprobación de recargas y actor de un movimiento; también faltan restricciones que impidan saldos negativos a nivel de base.

**Decisión.** Añadir a `usuarios`: `intentos_fallidos`, `bloqueado_hasta` y `ultimo_acceso`. Añadir a `recargas`: `id_admin_aprueba`, `fecha_aprobacion` y `motivo_rechazo`. Añadir a `movimientos`: `id_usuario` (actor que ejecuta la compra o el canje). Añadir `CHECK` de saldo no negativo en `billeteras`, de stock y precios no negativos en `productos`, y los índices por billetera, estudiante, estado y rol. Los scripts deben ser idempotentes y no destructivos.

**Consecuencias.** El DDL crece un poco, pero cubre requisitos que ya están en la arquitectura. T02 debe cerrarse antes de T03 para no sembrar datos incompatibles.

### ADR-06 · Reglas de interfaz y concurrencia en Swing

**Contexto.** La arquitectura exige un único `MainFrame` con `JTree` y `CardLayout`, pero no define cómo evitar que una consulta bloquee el EDT ni cómo convivir con el GUI Builder de NetBeans.

**Decisión.** Cada pantalla es un `JPanel` que implementa `Refrescable` (`refrescar()`). `MainFrame` construye el árbol desde un enum `Modulo` filtrado por `Permisos`, muestra el panel con `CardLayout` y llama a `refrescar()` al activarlo. Las llamadas a servicios se ejecutan en `SwingWorker`; los botones se deshabilitan mientras hay una operación y los errores se publican en el EDT. Los paneles se editan con el GUI Builder y se versionan los `.form`; los componentes base (`PanelFormularioBase`, `PanelTablaBase`, `Toast`, `DialogoConfirmacion`, `Estilos`) se escriben a mano. Ningún panel contiene SQL ni reglas de negocio.

**Consecuencias.** La interfaz no se congela con consultas lentas y el código generado se mantiene estable. Hay que acordar que solo se edita dentro de los bloques protegidos del GUI Builder.

### ADR-07 · Notificaciones M10a y M10b como extensión condicionada

**Contexto.** El cronograma incluye correo SMTP y WhatsApp, pero la arquitectura no los diseña y el alcance inicial los excluye. Además, WhatsApp exige datos de contacto, credenciales y plantillas aprobadas, y se trata de menores de edad.

**Decisión.** M10a y M10b son extensiones de la Etapa 2, activadas por banderas `NOTIF_EMAIL_ENABLED` y `NOTIF_WHATSAPP_ENABLED` (por defecto `false`). Se implementa un patrón outbox con la tabla `notificaciones` (Anexo B), un `NotificacionService` que procesa pendientes con reintentos y un modo simulado que escribe en el log. Correo con Jakarta Mail (Angus Mail); WhatsApp con `java.net.http.HttpClient` contra WhatsApp Cloud API y plantillas aprobadas. Solo se notifica a padres o tutores, con consentimiento y sin datos académicos sensibles. Las credenciales viven en `.env`, nunca en el repositorio.

**Consecuencias.** Si no hay credenciales o red, la entrega sigue siendo válida con el modo simulado y la guía de configuración. La arquitectura debe recibir un addendum con la tabla, el flujo y la política de privacidad antes de implementar.

### ADR-08 · Estrategia de pruebas

**Contexto.** La arquitectura define 15 casos funcionales, pero no indica qué se automatiza ni cómo se prueba la base.

**Decisión.** Tres niveles: unitario para dominio y utilidades (`Billetera`, `Producto`, `PasswordUtil`, `Validador`, `Permisos`); integración para DAO y servicios contra una base `EscuelaVirtualTest` creada por `03_base_pruebas.sql`; funcional manual para los recorridos de UI y los CP. Se usan etiquetas JUnit `unit` e `integration`; Surefire ejecuta unitarios y Failsafe los de integración con `DB_URL_TEST`. No se usa H2 porque el dialecto y las transacciones no representan a SQL Server. La cobertura objetivo es 60 % en servicios y dominio, no en vistas.

**Consecuencias.** Hay que mantener dos bases y datos de prueba, pero los defectos de transacción y concurrencia se detectan antes de la demo.

<!-- pagebreak -->

## 5. Alcance, equipo y definición de terminado

### 5.1 Alcance

**Incluido en el MVP:** M01 autenticación; M02 usuarios; M03 billetera; M04 recargas; M05 tienda y compras; M06 logros; M07 catálogo y canjes; M08 supervisión; M09 reportes y CSV; seis tablas; scripts; pruebas CP-01 a CP-20; JAR ejecutable; manual de usuario y documento de pruebas.

**Incluido como extensión condicionada:** M10a correo SMTP y M10b WhatsApp, con banderas, outbox y modo simulado; instalador MSI o app-image; tablas opcionales del anexo B (bitácora, parámetros, periodos).

**Excluido:** pasarelas de pago reales, aplicación web o móvil, multiinstitución o multisede, inventario físico en varias sedes, conciliación bancaria, SSO y analítica avanzada.

### 5.2 Supuestos

- El equipo dispone de 12 horas semanales por persona; con 8 horas, el alcance debe recortarse según la línea de corte.
- SQL Server 2019 o superior está disponible para desarrollo y demo; en Linux se usa el contenedor de SQL Server 2022 o 2025 existente.
- El instalador se compila en una máquina Windows con WiX Toolset; si no existe, se entrega app-image portable.
- El docente asesor revisa avances los miércoles y valida la demo de cierre de cada etapa.
- Los correos y teléfonos de padres se usan solo para notificaciones y con consentimiento.

### 5.3 Definición de listo y de terminado

**Una tarea está lista (DoR) cuando:** tiene criterio de aceptación verificable, dependencia identificada, dueño, estimación, datos de prueba y, si aplica, wireframe o DDL aprobado.

**Una tarea está terminada (DoD) cuando:** compila; `./mvnw clean verify` pasa; las pruebas nuevas pasan; no introduce secretos ni SQL en la vista; fue revisada por otra persona; actualiza la documentación afectada; deja evidencia de demo o captura; y no deja defectos S1 o S2 abiertos.

### 5.4 Puertas de calidad

| Puerta | Criterio | Se verifica |
|---|---|---|
| G1 · Build | `./mvnw clean verify` verde y JAR generado | Cada PR y cada cierre de tarea |
| G2 · Datos | Scripts idempotentes; ninguna operación deja saldo negativo; CP-20 verde | T02, S01–S04 y cierre de etapa |
| G3 · Seguridad | Sin secretos versionados; permisos en servicio; CP-03, CP-10, CP-16 y CP-17 verdes | T08, T10 y cierre de etapa |
| G4 · Transacciones | Compra, canje, recarga y logro en una sola transacción; CP-14 y CP-19 verdes | S02–S04 |
| G5 · Usabilidad | Mensajes en español, errores junto al campo, confirmación de operaciones con saldo | T06, T07 y cierre de etapa |
| G6 · Entrega | Manual, informe de pruebas, instalador y demo | T13, T14, S09 y S10 |

### 5.5 Roles y RACI

| Actividad | LP | BE | UI | BD | QA |
|---|---|---|---|---|---|
| Plan, alcance y decisiones | A/R | C | C | C | C |
| Scripts y modelo de datos | A | C | I | R | C |
| Servicios, DAO y transacciones | A | R | C | C | C |
| Paneles, navegación y componentes | A | C | R | I | C |
| Pruebas y evidencia | A | C | C | C | R |
| Documentación y manual | A/R | C | C | C | C |
| Empaquetado y despliegue | A | R | I | C | C |

**Segregación de funciones.** El cronograma asigna BE y QA a la misma persona. Para que la prueba sea independiente, el líder firma la aceptación de cada etapa y al menos otro integrante ejecuta los casos críticos CP-03, CP-06, CP-10 y CP-20. Rodrigo puede automatizar y corregir, pero no ser el único que valida.

### 5.6 Carga de trabajo y rebalanceo

| Rol | Persona | Tareas del cronograma | Horas estimadas | Participación |
|---|---|---|---|---|
| BE + QA | Rodrigo Fontela | T01, T04, T05, T08, T10, T11, T12, S01–S04, S06–S10 | 176 h | 72 % |
| UI | Paolo Pacheco | T06, T07, T09, S05 | 50 h | 21 % |
| BD | Yuber Guerrero | T02, T03 | 10 h | 4 % |
| LP | Luis Rojas | T13 | 8 h | 3 % |
| Total | | 24 tareas | 244 h | 100 % |

La carga está mal distribuida y además BE y QA son la misma persona. Con 12 h semanales por persona y seis semanas hay 288 h de capacidad; el plan consume 244 h y deja apenas 44 h para correcciones, demos y documentación. Con 8 h semanales, la capacidad baja a 192 h y el déficit es de 52 h. La propuesta de rebalanceo es:

- **UI** asume la parte visual y de controlador de M02, M05 y M06: `PanelUsuarios`, `PanelTienda`, `PanelCarrito`, `PanelRegistrarLogro`, `PanelCatalogo` y `PanelCanjear`.
- **BD** asume los DAO y las pruebas de integración de S01–S04, además de los scripts y la base de pruebas.
- **LP** asume S06 (reportes y CSV) con revisión de BE, la documentación y la ejecución de pruebas de aceptación.
- **QA** rota: Rodrigo automatiza, pero LP, UI y BD ejecutan y firman los casos críticos.

El objetivo es que ninguna persona supere el 55 % de la carga y que BE se concentre en servicios, transacciones y seguridad. El rebalanceo requiere aprobación del líder porque modifica la columna «Responsable» del cronograma; las fechas no cambian.

<!-- pagebreak -->
## 6. Plan por etapas y cronograma

### 6.1 Hitos

| Hito | Fecha | Criterio de cierre | Evidencia |
|---|---|---|---|
| M0 · Decisiones y entorno | 27/10/2026 | ADR aprobadas; build Java 17; SQL Server de desarrollo operativo; scripts 01–03 ejecutados | Acta, `mvn verify`, captura de login |
| M1 · Etapa 1 cerrada | 04/11/2026 | Login, usuarios, navegación por rol, 9 paneles prototipo, CP-01 a CP-03 y CP-16 a CP-18 verdes; JAR y manual | Tag `v0.1-etapa1`, informe de pruebas, demo |
| M2 · Billetera y recargas | 11/11/2026 | M03 y M04 operativos; CP-04, CP-05, CP-13 y CP-20 verdes | Demo y evidencias |
| M3 · Tienda y recompensas | 18/11/2026 | M05, M06 y M07 operativos; CP-06 a CP-12, CP-14 y CP-19 verdes | Demo y evidencias |
| M4 · Supervisión y reportes | 25/11/2026 | M08 y M09 operativos; CP-15 y CP-20 verdes; CSV validado | Demo e informe |
| M5 · Notificaciones e instalador | 02/12/2026 | M10a/M10b en modo real o simulado; instalador probado en Windows limpio | Tag `v0.2-etapa2`, instalador, guía |
| M6 · Entrega final | 02/12/2026 | Informe de pruebas, manual, código y demo final | Carpeta de entrega |

### 6.2 Etapa 1 · Base, login y usuarios (21/10 – 04/11)

**Objetivo.** Dejar una aplicación que arranque, conecte a SQL Server, autentique a los cuatro roles, navegue por un árbol de módulos y administre usuarios. El resto de paneles existe en modo prototipo, sin lógica completa.

| ID | Actividad | Resp. | Fecha | Entregable verificable | Criterio de cierre |
|---|---|---|---|---|---|
| T01 | Entorno, build y proyecto base con driver JDBC | BE | 21/10 (vencida) | POM con Java 17, wrapper, driver, `Main` y `DatabaseConnection` | `./mvnw clean verify` y arranque con mensaje de conexión |
| T02 | Script DDL de SQL Server para las 6 tablas | BD | 21/10 (vencida) | `scripts/01_creacion_base_datos.sql` y `00_usuario_bd.sql` | Reproduce la base existente, aplica deltas, crea CHECK/FK/índices/vista y se ejecuta dos veces |
| T03 | Datos iniciales y usuarios de prueba por rol | BD | 27/10 | `scripts/02_datos_iniciales.sql` y `GeneradorHash` | Los 7 usuarios, 3 billeteras y catálogo existentes se reproducen desde el script; hashes válidos |
| T04 | Entidades, enumeraciones y `SessionManager` | BE | 27/10 | Paquetes `model`, `model.enums` y `util` | Compila; pruebas de `PasswordUtil` y `Validador` verdes |
| T05 | Esqueleto de 3 capas y `Main` de arranque | BE | 27/10 | Paquetes `dao`, `service`, `controller`, `view` y contratos base | `Main` valida conexión y abre el login; sin lógica duplicada |
| T06 | Diseño de las interfaces JPanel en modo prototipo | UI | 28/10 | 9 paneles base y wireframes de los restantes | Navegación estática con datos falsos; sin SQL |
| T07 | Componentes base: formulario, tabla, Toast y estilos | UI | 28/10 | `PanelFormularioBase`, `PanelTablaBase`, `Toast`, `Estilos` | Componentes reutilizables y probados en dos pantallas |
| T08 | M01 Autenticación real contra SQL Server | BE + UI | 28/10 | `LoginFrame`, `PanelLogin`, `AuthService`, `UsuarioDAO` | CP-01, CP-02, CP-16 y CP-17 verdes |
| T09 | Navegación por rol: `MainFrame` + `JTree` + `CardLayout` | UI | 03/11 | `MainFrame`, enum `Modulo` y árbol filtrado | CP-18 verde; cada rol ve solo sus módulos |
| T10 | M02 Usuarios: ABU, roles y vínculo padre–hijo | BE + UI | 03/11 | `PanelUsuarios`, `UsuarioService`, `UsuarioDAO` | CP-03 verde; alta, edición, baja lógica y asignación de padre |
| T11 | Guion de verificación login → usuarios | QA | 03/11 | Guion con pasos, datos y evidencia | CP-01 a CP-03 y CP-18 ejecutados y firmados |
| T12 | Pruebas de integración de Etapa 1 y corrección | QA | 04/11 | Informe de pruebas de Etapa 1 | Sin defectos S1/S2; CP de etapa verdes |
| T13 | Documentación técnica y manual de usuario de Etapa 1 | LP | 04/11 | Manual y arquitectura actualizada | Documentos revisados y versionados |
| T14 | Empaquetado ejecutable y cierre de Etapa 1 | BE | 04/11 | JAR con dependencias y tag `v0.1-etapa1` | JAR arranca en otra máquina; demo de cierre |

**Criterios de salida de la Etapa 1.** Login de ADMIN, DOCENTE, PADRE y ESTUDIANTE; árbol de módulos correcto; ABU de usuarios; 9 paneles prototipo; scripts reproducibles; `mvn verify` verde; manual y JAR; tag `v0.1-etapa1`.

### 6.3 Etapa 2 · Módulos, reportes, notificaciones e instalador (10/11 – 02/12)

**Objetivo.** Completar el flujo económico y de recompensas, la supervisión, los reportes, las notificaciones y la entrega instalable.

| ID | Actividad | Resp. | Fecha | Entregable verificable | Criterio de cierre |
|---|---|---|---|---|---|
| S01 | M03 Billetera: saldos, movimientos y logros | BE + UI | 10/11 | `PanelMiBilletera`, `BilleteraService`, `BilleteraDAO` | CP-13 y CP-20 verdes; saldos y movimientos correctos |
| S02 | M04 Recargas: registro, aprobación y acreditación | BE + UI | 11/11 | `PanelRecargarSaldo`, `PanelAprobarRecargas`, `RecargaService` | CP-04 y CP-05 verdes; recarga pendiente no acredita |
| S03 | M05 Tienda y compras con transacción y stock | BE + UI | 17/11 | `PanelTienda`, `PanelCarrito`, `CompraService` | CP-06, CP-07, CP-08, CP-14 y CP-19 verdes |
| S04 | M06 Logros y M07 catálogo y canjes | BE + UI | 18/11 | `PanelRegistrarLogro`, `PanelMisLogros`, `PanelCatalogo`, `PanelCanjear` | CP-09 a CP-12 verdes; monedas y stock consistentes |
| S05 | M08 Seguimiento y supervisión por rol | UI | 24/11 | `PanelHistorialCompras` y consultas por rol | Padre ve solo sus hijos; admin ve el consolidado |
| S06 | M09 Reportes desde la app y exportación CSV | LP + BE | 25/11 | `PanelReportes`, `ReporteService`, CSV | CP-15 verde; CSV abre en Excel con tildes y decimales |
| S07 | M10a Correo SMTP, plantillas y avisos | BE | 01/12 | `NotificacionService`, outbox y plantillas | Envío real o simulado; reintentos y log sin PII |
| S08 | M10b Notificación por WhatsApp Cloud API | BE | 01/12 | Adaptador WhatsApp, bandera y guía | Envío simulado o real con plantilla aprobada |
| S09 | Instalador `jpackage` y despliegue | BE + BD | 02/12 | MSI o app-image, guía de instalación | Instalación y login en Windows limpio sin JDK |
| S10 | Pruebas integrales, documentación final y entrega | QA + LP | 02/12 | Informe final, manual y tag `v0.2-etapa2` | CP-01 a CP-20 verdes; entrega completa |

**Criterios de salida de la Etapa 2.** M03–M09 operativos; M10a/M10b reales o simulados con banderas; CP-01 a CP-20 aprobados; reportes CSV; instalador probado; manual, informe y tag `v0.2-etapa2`.

### 6.4 Ruta crítica y dependencias

```text
T01 → T02 → T03 → T04/T05 → T08 → T09/T10 → T11/T12 → T14
                                      ↘
S01 → S02 → S03 → S04 → S05/S06 → S07/S08 → S09/S10
T06/T07 (UI) ───────────────────────↗        (prototipo y componentes base)
```

- T01 bloquea todo: sin build y conexión no hay tarea verificable.
- T02 y T03 bloquean T04, T08, S01 y S02.
- T08 bloquea T09 y T10; T09 y T10 bloquean la demo de Etapa 1.
- S01 bloquea S02, S03 y S04; S03 y S04 bloquean S05 y S06.
- S07/S08 no bloquean el núcleo, pero consumen tiempo de BE al final; por eso se adelantan al 26/11.
- S09 depende de que el JAR y las dependencias estén estables; se puede preparar en paralelo desde el 26/11.

### 6.5 Plan de recuperación al 26/10/2026

El cronograma original concentra las tareas en martes y miércoles. Para recuperar T01 y T02 y llegar al 04/11, se trabaja también jueves, viernes y lunes, manteniendo las fechas de cierre y las demos.

| Fecha | BE | UI | BD | QA / LP | Hito del día |
|---|---|---|---|---|---|
| Lun 26/10 | Cerrar T01; `DatabaseConnection`; renombrar paquete | Wireframes de 9 pantallas | Comparar la base existente con el DDL, aplicar deltas y versionar scripts | Registrar ADR y tablero | Decisiones y build |
| Mar 27/10 | T04 y T05 | Componentes base | Cerrar T02 y T03; ejecutar scripts | Guion CP-01 a CP-03 | T01–T05 cerradas |
| Mié 28/10 | T08 login real | T06 y T07 | Apoyo de datos | Ejecutar CP-01/02/16/17 | Login funcionando |
| Jue 29/10 | Corrección de login; inicio T10 | Terminar paneles | Índices y vista de cuadre | Revisión de código | Login estable |
| Vie 30/10 | T10 usuarios | T09 navegación | Pruebas de integración | CP-03/18; documentación | Usuarios y árbol |
| Lun 02/11 | T10 y soporte a T12 | Ajustes de paneles | Datos de prueba | Ejecutar CP de Etapa 1 | Integración |
| Mar 03/11 | Cierre T10; demo | Cierre T09 | Soporte | T11 y demo | Demo de Etapa 1 |
| Mié 04/11 | T14 JAR | Correcciones visuales | Backup de BD | T12, T13 y cierre | Etapa 1 cerrada |

**Ajustes de Etapa 2.** S03 se inicia el 13/11 y no el 17/11; S05 y S06 se solapan el 20–25/11; S07 y S08 se inician el 26/11 y no el 01/12; S09 se prepara desde el 27/11. Las fechas de cierre no se mueven.

### 6.6 Línea de corte

| Prioridad | Alcance | Si el tiempo no alcanza |
|---|---|---|
| Must | M01–M07, M09 básico, base de datos, transacciones, permisos, CP críticos, JAR | No se recorta; se reduce el alcance visual o se trabaja horas extra |
| Should | M08 completo, CSV con formato, instalador MSI, componentes de pulido | Se entrega app-image y CSV básico; se documenta la deuda |
| Could | M10a/M10b, bitácora, parámetros, periodos académicos, reportes avanzados | Se entregan simulados, con banderas y guía; no bloquean la aceptación |

**Orden de recorte si hay retraso:** primero M10b, luego M10a, después el MSI (se entrega app-image), luego M08 avanzado y por último el pulido visual de T06. Nunca se recortan la seguridad, las transacciones, la conciliación de saldos ni los CP críticos.

<!-- pagebreak -->
## 7. Fichas de tareas críticas

Las fichas siguientes son el contrato de trabajo de las tareas con mayor riesgo. Las tareas simples se gobiernan por la tabla del capítulo 6 y por la definición de terminado.

### F01 · T01 — Entorno, build y arranque

**Objetivo.** Que cualquier integrante clone el repositorio, ejecute `./mvnw clean verify` y arranque la aplicación con un mensaje claro de conexión.

**Pasos.**
- [ ] Renombrar el paquete a `com.escuela.virtual`, la clase principal a `Main` y el `groupId` a `com.escuela.virtual`.
- [ ] Fijar `maven.compiler.release=17`; añadir `maven-compiler-plugin 3.13.0`, Surefire 3.2.5, Failsafe 3.2.5 y Shade 3.6.0.
- [ ] Crear el perfil Maven `integration`, que activa Failsafe y las pruebas etiquetadas `integration`; sin el perfil, `./mvnw verify` ejecuta solo unitarias.
- [ ] Añadir `mssql-jdbc 12.10.2.jre11` y `junit-jupiter 5.11.4` con alcance `test`.
- [ ] Generar el Maven Wrapper (`mvn wrapper:wrapper`) y versionar `.mvn/`, `mvnw` y `mvnw.cmd`; documentar que NetBeans puede usar su Maven embebido.
- [ ] Implementar `DatabaseConnection` sobre `Config.require("DB_URL")`, con cierre seguro y prueba de conexión al arranque.
- [ ] Actualizar `.env.example` con un ejemplo de URL para Windows y otro para Docker, sin credenciales reales.
- [ ] Añadir un `README.md` con requisitos, configuración, ejecución y solución de problemas.

**Archivos.** `pom.xml`, `mvnw`, `.mvn/`, `src/main/java/com/escuela/virtual/Main.java`, `src/main/java/com/escuela/virtual/util/DatabaseConnection.java`, `.env.example`, `README.md`.

**Aceptación.** `./mvnw clean verify` verde; `java -jar target/escuelavirtual-*.jar` arranca; sin `.env` muestra un error accionable; con `.env` válido abre el login; el JAR no requiere JDK 25.

**Riesgo.** El renombrado puede romper la configuración de NetBeans. Se hace en una sola rama y se avisa al equipo antes de mezclar.

### F02 · T02 — DDL, restricciones e índices

**Objetivo.** Crear una base reproducible, idempotente y compatible con las reglas de negocio.

**Pasos.**
- [ ] Crear `scripts/00_usuario_bd.sql` con login y usuario de aplicación de permisos mínimos (`db_datareader` y `db_datawriter`, sin DDL).
- [ ] Crear `scripts/01_creacion_base_datos.sql` con `IF DB_ID(...) IS NULL`, las seis tablas, CHECK, FK, índices y la vista `vw_cuadre_billeteras`.
- [ ] Aplicar ADR-05: columnas de bloqueo en `usuarios`, aprobación en `recargas`, actor en `movimientos` y CHECK de saldos, stock y precios.
- [ ] Añadir índices por billetera y fecha, estudiante y fecha, estado de recarga, rol y estado, y padre.
- [ ] Probar el script en una base vacía y volver a ejecutarlo para verificar idempotencia.
- [ ] Documentar el orden de ejecución y el usuario con el que se ejecuta cada script.

**Aceptación.** El script crea todo desde cero; una segunda ejecución no falla; las FK impiden borrar usuarios o productos con historial; `vw_cuadre_billeteras` devuelve cero diferencias con los datos iniciales.

**Riesgo.** El contenedor de SQL Server consume 4 GB de RAM. Si el equipo no lo soporta, usar una instancia compartida o Azure SQL Database para desarrollo.

### F03 · T03 — Datos iniciales y hashes

**Objetivo.** Poder iniciar sesión con los cuatro roles desde el primer arranque, sin contraseñas en claro.

**Pasos.**
- [ ] Implementar `GeneradorHash` en `src/test/java` o como utilidad de desarrollo; recibe una clave e imprime el formato `iteraciones$salt$hash`.
- [ ] Generar los hashes de `Admin123*`, `Docente123*`, `Padre123*` y `Estudiante123*`.
- [ ] Insertar 1 admin, 1 docente, 2 padres y 3 estudiantes; asignar `id_padre` a los estudiantes que correspondan.
- [ ] Crear una billetera por estudiante con los saldos del documento (50/120, 25/80 y 0/45).
- [ ] Insertar 8 productos de tienda y 4 recompensas con precios y stock.
- [ ] Hacer el script idempotente con `IF NOT EXISTS` o `MERGE`; no duplicar correos.

**Aceptación.** Los 7 usuarios inician sesión; los hashes cambian si se cambia la clave; no hay texto plano en la base ni en el script; ejecutar dos veces no duplica datos.

### F04 · T04 y T05 — Modelo, sesión y esqueleto de capas

**Objetivo.** Tener las entidades, enums, utilidades y contratos que permiten programar los módulos en paralelo.

**Pasos.**
- [ ] Crear `Usuario`, `Billetera`, `Producto`, `Logro`, `Recarga` y `Movimiento` con JavaBeans, validación y sin lógica de acceso a datos.
- [ ] Crear los enums `Rol`, `MetodoPago`, `EstadoRecarga` y `TipoMovimiento`; mapear los valores a los CHECK de la base.
- [ ] Implementar `PasswordUtil` (PBKDF2 120 000 iteraciones, salt de 16 bytes, comparación en tiempo constante), `Validador`, `Formato` y `SessionManager`.
- [ ] Implementar `Permisos` con las reglas de rol y de vínculo padre–hijo; cubrirlo con pruebas unitarias.
- [ ] Definir `NegocioException`, `CodigoError` y `Transaccion.ejecutar(...)` para estandarizar el manejo de errores y el commit/rollback.
- [ ] Crear los paquetes `dao`, `service`, `controller`, `view` y `util.ui` con las clases base vacías y sus contratos.

**Aceptación.** Compila; las pruebas unitarias de `PasswordUtil`, `Validador` y `Permisos` pasan; `SessionManager` no permite dos sesiones simultáneas en la misma instancia; `Transaccion` revierte ante una excepción.

### F05 · T06 y T07 — Base visual y prototipo

**Objetivo.** Que la interfaz exista antes de que los servicios estén completos, para validar navegación, permisos visuales y experiencia.

**Pasos.**
- [ ] Crear `Estilos` con colores, fuentes, bordes, espaciados y tamaños consistentes.
- [ ] Crear `PanelFormularioBase` (cabecera, formulario en `GridBagLayout`, pie de botones, `mostrarError`, `limpiar`, `bloquear`).
- [ ] Crear `PanelTablaBase` con `TableModel` propio, búsqueda, filtro por fecha, estado vacío y exportación.
- [ ] Crear `Toast` no modal y `DialogoConfirmacion` para operaciones que modifican saldo.
- [ ] Prototipar las 9 pantallas base: login, inicio por rol, billetera, tienda, carrito, recarga, aprobación, logros, usuarios y reportes.
- [ ] Validar con el equipo que el árbol y las etiquetas coinciden con el anexo C.

**Aceptación.** El prototipo navega sin SQL y sin excepciones; los paneles se ven bien a 1366×768 y 1920×1080; los formularios muestran errores junto al campo; el equipo aprueba el flujo.

### F06 · T08 — Login real

**Objetivo.** Autenticar contra SQL Server con bloqueo por intentos y mensajes claros.

**Pasos.**
- [ ] Implementar `UsuarioDAO.findByCorreo` con `PreparedStatement`, normalización de correo y mapeo de filas.
- [ ] Implementar `AuthService.login(correo, clave)` con verificación PBKDF2, estado activo, bloqueo y actualización de `ultimo_acceso`.
- [ ] Implementar el bloqueo: 3 intentos fallidos, 15 minutos, reinicio al éxito; mensaje distinto para cuenta bloqueada.
- [ ] Implementar `LoginController` y `PanelLogin`; Enter envía el formulario; mostrar/ocultar clave.
- [ ] Implementar `LoginFrame` y el cambio a `MainFrame`; cerrar la ventana anterior sin dejar hilos vivos.
- [ ] Añadir el diálogo de error de conexión con instrucciones sobre SQL Server, puerto y `.env`.

**Aceptación.** CP-01, CP-02, CP-16 y CP-17 verdes; un usuario inactivo no entra; la clave nunca aparece en logs; el login responde en menos de 2 s.

### F07 · T09, T10 y T11 — Navegación, usuarios y guion

**Objetivo.** Cerrar el recorrido «login → árbol por rol → gestión de usuarios» con evidencia de QA.

**Pasos.**
- [ ] Crear el enum `Modulo` con código, título, panel y roles permitidos; derivar el `JTree` de `Permisos`.
- [ ] Implementar `MainFrame` con cabecera, árbol, `CardLayout`, barra de estado y cierre de sesión con confirmación.
- [ ] Implementar `Refrescable` y llamar a `refrescar()` al mostrar cada panel.
- [ ] Implementar `PanelUsuarios`, `DialogoUsuario`, `UsuarioService` y `UsuarioDAO`: alta, edición, baja lógica, filtro y vínculo padre–hijo.
- [ ] Impedir que un usuario se dé de baja a sí mismo o deje el sistema sin administrador.
- [ ] Escribir el guion T11 con datos, pasos, resultado esperado y evidencia; ejecutar CP-01, CP-02, CP-03 y CP-18.

**Aceptación.** Cada rol ve exactamente los módulos del anexo C; ADMIN ve 9; el padre solo ve sus hijos; la baja lógica impide el login; el guion está firmado.

### F08 · S01 — Billetera y movimientos

**Objetivo.** Mostrar los dos saldos, el historial y los logros, con conciliación verificable.

**Pasos.**
- [ ] Implementar `BilleteraDAO.findByEstudiante`, `obtenerSaldo`, `listarMovimientos` y `listarLogros`.
- [ ] Implementar `BilleteraService.consultarSaldo(actor, idEstudiante)` con verificación de propiedad o vínculo padre–hijo.
- [ ] Implementar `PanelMiBilletera` con tarjetas de saldo, pestañas de movimientos y logros, y botón de actualización.
- [ ] Añadir el filtro por fecha y la exportación del historial.
- [ ] Crear la consulta de cuadre y exponerla en `ReporteService` o en una clase `Conciliacion`.
- [ ] Ejecutar CP-13 y CP-20 con datos reales.

**Aceptación.** El estudiante ve su billetera; el padre ve solo la de sus hijos; el admin ve todas; la suma de recargas, logros y movimientos coincide con ambos saldos; el historial carga en menos de 2 s con 5 000 registros.

<!-- pagebreak -->
### F09 · S02 — Recargas y aprobación

**Objetivo.** Que el padre registre una recarga y el administrador la apruebe sin acreditar dos veces.

**Pasos.**
- [ ] Implementar `RecargaDAO.insertar`, `listarPorPadre`, `listarPendientes`, `actualizarEstado` y `bloquearPorId`.
- [ ] Implementar `RecargaService.registrar(actor, idEstudiante, monto, metodo)` con verificación de padre–hijo y monto positivo.
- [ ] Implementar `aprobar(actor, idRecarga)` en una transacción: bloquear la recarga, verificar estado PENDIENTE, cambiar a APROBADA, acreditar billetera y registrar `id_admin_aprueba` y `fecha_aprobacion`.
- [ ] Implementar `rechazar(actor, idRecarga, motivo)` sin tocar el saldo.
- [ ] Implementar `PanelRecargarSaldo` y `PanelAprobarRecargas` con confirmación y actualización de la tabla.
- [ ] Ejecutar CP-04 y CP-05; probar doble aprobación y aprobación concurrente.

**Aceptación.** Una recarga PENDIENTE no cambia el saldo; la aprobación acredita exactamente una vez; el rechazo no acredita; una segunda aprobación devuelve error de negocio; el padre no puede recargar a un hijo ajeno.

### F10 · S03 — Tienda, compra y transacción

**Objetivo.** Comprar con dinero descontando saldo y stock en una sola transacción, sin condiciones de carrera.

**Pasos.**
- [ ] Implementar `ProductoDAO.listar`, `findById`, `descontarStock` y `ProductoService`/`CatalogoService`.
- [ ] Implementar `CompraService.comprar(actor, idProducto, cantidad)` con `idEstudiante` derivado del actor y verificación de rol ESTUDIANTE.
- [ ] Abrir la conexión, desactivar autocommit, seleccionar producto y billetera con `UPDLOCK, ROWLOCK`, validar stock y saldo, actualizar con condición y verificar `rowcount = 1`.
- [ ] Insertar el movimiento con `id_usuario`, `metodo_pago = 'DINERO'`, cantidad y monto total; hacer commit.
- [ ] Implementar `PanelTienda` con productos deshabilitados sin stock y `PanelCarrito` con elección de método y confirmación.
- [ ] Probar CP-06, CP-07, CP-08, CP-14 (trigger de prueba) y CP-19 (dos sesiones por la última unidad).

**Aceptación.** La compra exitosa descuenta saldo y stock y registra un movimiento; sin saldo o sin stock no hay cambios; el rollback deja todo igual; dos compras concurrentes no dejan stock negativo.

### F11 · S04 — Logros, catálogo y canjes

**Objetivo.** Otorgar monedas solo desde DOCENTE o ADMIN y canjearlas con control de stock.

**Pasos.**
- [ ] Implementar `LogroDAO.insertar`, `listarPorEstudiante`, `listarPorDocente` y `LogroService.registrar(actor, ...)`.
- [ ] Registrar el logro y acreditar monedas en la misma transacción; validar que el estudiante esté activo y que las monedas sean mayores que cero.
- [ ] Implementar `ProductoService.guardar`, `activar` y el catálogo de recompensas con `precio_monedas`.
- [ ] Implementar `CanjeService.canjear(actor, idProducto, cantidad)` con validación de saldo de monedas, stock y transacción.
- [ ] Implementar `PanelRegistrarLogro`, `PanelMisLogros`, `PanelCatalogo` y `PanelCanjear`.
- [ ] Ejecutar CP-09, CP-10, CP-11, CP-12 y CP-13; verificar que un estudiante no puede autoasignarse monedas.

**Aceptación.** El logro acredita monedas una sola vez; el canje descuenta monedas y stock; no se puede canjear sin monedas; las monedas no se convierten en dinero ni al revés.

### F12 · S05 y S06 — Supervisión y reportes

**Objetivo.** Dar a cada rol la información que le corresponde y exportar reportes útiles.

**Pasos.**
- [ ] Implementar `PanelHistorialCompras` con filtro por hijo, fecha y producto; el padre solo ve sus hijos y el admin ve todo.
- [ ] Implementar consultas agregadas de consumo por estudiante, recompensas por docente y saldos por periodo.
- [ ] Implementar `ReporteService.exportarCSV` con BOM UTF-8, separador `;`, escape de comillas y formato de fecha `dd/MM/yyyy HH:mm`.
- [ ] Implementar `PanelReportes` con filtros, tabla y botón de exportación; mostrar el total antes de exportar.
- [ ] Ejecutar CP-15 y CP-20; abrir el CSV en Excel y LibreOffice; validar tildes, decimales y fechas.
- [ ] Medir el tiempo de consulta con 5 000 movimientos y documentar el hardware.

**Aceptación.** El CSV abre sin errores; los totales coinciden con la base; el padre no ve datos de otros hijos; la consulta cumple menos de 2 s con 5 000 registros.

### F13 · S07 y S08 — Notificaciones

**Objetivo.** Avisar a padres por correo y, opcionalmente, por WhatsApp, sin bloquear la operación principal.

**Pasos.**
- [ ] Crear la tabla `notificaciones` (outbox) con canal, destino, plantilla, estado, intentos, último error y fechas.
- [ ] Añadir `telefono` y `acepta_notificaciones` a `usuarios` mediante `scripts/04_extension_notificaciones.sql`.
- [ ] Implementar `NotificacionService.encolar`, `procesarPendientes` y reintentos con backoff; banderas `NOTIF_EMAIL_ENABLED` y `NOTIF_WHATSAPP_ENABLED`.
- [ ] Implementar el adaptador SMTP con Jakarta Mail (Angus Mail) y el adaptador WhatsApp con `HttpClient`; modo `dry-run` que escribe en el log.
- [ ] Crear plantillas de recarga aprobada, logro otorgado y compra realizada; no incluir datos académicos sensibles.
- [ ] Probar con MailHog o GreenMail y con el sandbox de WhatsApp; documentar credenciales y plantillas.

**Aceptación.** Con las banderas en `false` no se envía nada y la app funciona; con `true` y credenciales válidas se envía y se registra el estado; un fallo de red no revierte la recarga ni la compra; no hay PII en el log.

### F14 · S09 — Instalador y despliegue

**Objetivo.** Instalar la aplicación en un equipo Windows sin JDK y con configuración externa.

**Pasos.**
- [ ] Generar el JAR con dependencias y copiar `target/lib`; verificar que el `Main-Class` es `com.escuela.virtual.Main`.
- [ ] Crear el script `jpackage` con `--type msi` o `--type app-image`, nombre, versión, vendor, icono, menú y acceso directo.
- [ ] Incluir una plantilla `.env.example` y una guía de primer arranque; no incluir credenciales.
- [ ] Probar en una VM Windows limpia sin JDK: instalar, configurar, iniciar sesión, comprar, desinstalar.
- [ ] Documentar requisitos, puertos, SQL Server, solución de problemas y rollback.
- [ ] Si no hay WiX, entregar app-image y documentar la limitación.

**Aceptación.** La instalación termina sin errores; la app arranca desde el menú; el login funciona; la desinstalación no deja datos sensibles; la guía permite reproducir la instalación.

### F15 · T12, T13, T14 y S10 — Cierre, pruebas y entrega

**Objetivo.** Cerrar cada etapa con evidencia, no solo con código.

**Pasos.**
- [ ] Ejecutar la matriz CP completa y registrar resultado, evidencia y defecto.
- [ ] Corregir defectos S1/S2 y volver a ejecutar los CP afectados.
- [ ] Actualizar arquitectura, manual, README y este plan con los cambios aprobados.
- [ ] Generar JAR/instalador, tag de release y nota de versión.
- [ ] Preparar la demo con un guion de 10 minutos por rol.
- [ ] Guardar la carpeta de entrega con código, scripts, documentos y evidencias.

**Aceptación.** Todos los CP de la etapa verdes; sin S1/S2 abiertos; documentos actualizados; tag creado; demo ensayada; entrega reproducible.

<!-- pagebreak -->

## 8. Arquitectura de la solución a implementar

### 8.1 Estructura de paquetes

```text
EscuelaVirtual/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
├── .env.example
├── README.md
├── docs/
│   ├── Arquitectura_Sistema_Gestion_Escolar.docx
│   ├── Cronograma Actividades.xlsx
│   └── Plan_Desarrollo_por_Etapas.md
├── scripts/
│   ├── 00_usuario_bd.sql
│   ├── 01_creacion_base_datos.sql
│   ├── 02_datos_iniciales.sql
│   ├── 03_base_pruebas.sql
│   └── 04_extension_notificaciones.sql
├── src/main/java/com/escuela/virtual/
│   ├── Main.java
│   ├── config/Config.java
│   ├── model/ (Usuario, Billetera, Producto, Logro, Recarga, Movimiento)
│   ├── model/enums/ (Rol, MetodoPago, EstadoRecarga, TipoMovimiento)
│   ├── dao/ (BaseDAO y un DAO por entidad)
│   ├── service/ (Auth, Usuario, Billetera, Recarga, Compra, Logro, Catalogo, Canje, Reporte, Notificacion, Conciliacion)
│   ├── controller/ (uno por módulo)
│   ├── view/ (LoginFrame, MainFrame, dialog/, panel/)
│   └── util/ (DatabaseConnection, PasswordUtil, SessionManager, Validador, Formato, Permisos, Transaccion, NegocioException)
└── src/test/java/com/escuela/virtual/
    ├── unit/ (PasswordUtil, Validador, Permisos, Billetera, Producto)
    └── integration/ (DAO y servicios contra EscuelaVirtualTest)
```

### 8.2 Reglas de dependencia

| Capa | Paquetes | Puede depender de | Prohibido |
|---|---|---|---|
| Presentación | `view`, `controller`, `util.ui` | `service`, `model`, `util` | `dao`, JDBC, SQL |
| Lógica | `service`, `model` | `dao`, `model`, `util` | Swing, `view`, `controller` |
| Persistencia | `dao`, `util` (conexión) | `model`, JDBC | Swing, `service`, reglas de negocio |
| Transversal | `config`, `util` | JDK, dotenv | Lógica de módulos |

Se verifica en revisión de código. Opcionalmente, ArchUnit puede automatizar la regla si el equipo acepta una dependencia adicional de prueba.

### 8.3 Modelo de dominio

| Entidad | Campos clave | Invariantes |
|---|---|---|
| `Usuario` | id, nombres, apellidos, correo, hash, rol, grado, sección, idPadre, estado, intentos, bloqueo | Correo único y normalizado; rol válido; estudiante con billetera; padre con hijos |
| `Billetera` | id, idEstudiante, saldoDinero, saldoMonedas | Saldos no negativos; una por estudiante; monedas enteras |
| `Producto` | id, nombre, descripción, precioDinero, precioMonedas, stock, activo | Al menos un precio; precios y stock no negativos; activo para venderse |
| `Logro` | id, idEstudiante, idDocente, tipo, descripción, monedas, fecha | Monedas mayores que cero; docente o admin como autor |
| `Recarga` | id, idBilletera, idPadre, monto, método, estado, fechas, admin | Monto mayor que cero; solo APROBADA acredita; una sola aprobación |
| `Movimiento` | id, idBilletera, idProducto, idUsuario, método, cantidad, monto, fecha | Compra o canje; cantidad y monto mayores que cero; actor registrado |

### 8.4 Esquema de datos y deltas

Las seis tablas del documento se mantienen. Los deltas de ADR-05 son:

```sql
ALTER TABLE usuarios ADD
    intentos_fallidos INT NOT NULL CONSTRAINT DF_usuarios_intentos DEFAULT 0,
    bloqueado_hasta DATETIME2 NULL,
    ultimo_acceso DATETIME2 NULL;

ALTER TABLE recargas ADD
    id_admin_aprueba INT NULL,
    fecha_aprobacion DATETIME2 NULL,
    motivo_rechazo NVARCHAR(255) NULL;

ALTER TABLE movimientos ADD
    id_usuario INT NULL;

ALTER TABLE billeteras ADD CONSTRAINT CK_billeteras_saldos
    CHECK (saldo_dinero >= 0 AND saldo_monedas >= 0);

ALTER TABLE productos ADD CONSTRAINT CK_productos_stock
    CHECK (stock >= 0);

ALTER TABLE productos ADD CONSTRAINT CK_productos_precios
    CHECK ((precio_dinero IS NULL OR precio_dinero > 0)
       AND (precio_monedas IS NULL OR precio_monedas > 0));

CREATE INDEX IX_usuarios_padre ON usuarios(id_padre);
CREATE INDEX IX_movimientos_usuario ON movimientos(id_usuario);
```

`id_usuario` en `movimientos` se deja nullable para no romper datos históricos; las filas nuevas lo exigen desde el servicio. La vista de cuadre se define en el capítulo 9.

<!-- pagebreak -->
### 8.5 Flujos críticos

| Flujo | Pasos principales | Transacción | Errores esperados |
|---|---|---|---|
| Login | Validar formato; buscar por correo; verificar estado y bloqueo; comparar PBKDF2; crear sesión; abrir `MainFrame` | No | Credenciales incorrectas, cuenta bloqueada, cuenta inactiva, sin conexión |
| Recarga | Padre registra PENDIENTE; admin aprueba; bloquear recarga; cambiar estado; acreditar saldo; registrar aprobador | Sí | Monto inválido, hijo ajeno, recarga ya aprobada, sin conexión |
| Compra | Autorizar estudiante; bloquear producto y billetera; validar stock y saldo; descontar; insertar movimiento; commit | Sí | Sin stock, saldo insuficiente, producto inactivo, conflicto de concurrencia |
| Logro | Autorizar docente/admin; validar estudiante; insertar logro; acreditar monedas; commit | Sí | Monedas inválidas, estudiante inactivo, autoasignación |
| Canje | Autorizar estudiante; validar producto y stock; validar monedas; descontar; insertar movimiento; commit | Sí | Sin monedas, sin stock, producto no canjeable |
| Reporte | Validar rol; consultar con filtros; agregar; mostrar; exportar CSV | No | Periodo inválido, sin datos, error de escritura |

**Patrón de transacción.** Los servicios abren una conexión, desactivan autocommit, ejecutan las operaciones con DAOs que reciben la misma conexión, hacen commit y restauran el estado. Ante cualquier excepción hacen rollback y lanzan `NegocioException` o `ErrorTecnico`.

```java
try (Connection cn = DatabaseConnection.getInstance().getConnection()) {
    cn.setAutoCommit(false);
    try {
        Producto p = productoDAO.findById(idProducto, cn, true); // UPDLOCK, ROWLOCK
        Billetera b = billeteraDAO.findByEstudiante(idEstudiante, cn, true);
        // validaciones y actualizaciones con condición
        cn.commit();
    } catch (SQLException | RuntimeException e) {
        cn.rollback();
        throw traducir(e);
    }
}
```

**Concurrencia.** Usar `SELECT ... WITH (UPDLOCK, ROWLOCK)` y `UPDATE ... WHERE stock >= ?` / `WHERE saldo_dinero >= ?`; comprobar `rowcount = 1`. Reintentar una vez si SQL Server devuelve el error 1205 (deadlock). Probar con dos hilos y la última unidad disponible.

### 8.6 Seguridad

- **Contraseñas:** PBKDF2WithHmacSHA256, 120 000 iteraciones, salt de 16 bytes, 256 bits, comparación con `MessageDigest.isEqual`; nunca se registran.
- **Sesión:** `SessionManager` guarda el usuario autenticado; se limpia al cerrar sesión; no hay «recordarme» ni sesión persistente.
- **Autorización:** en el servicio, no solo en la vista; `Permisos` cubre rol, propiedad y vínculo padre–hijo.
- **SQL:** solo `PreparedStatement`; los filtros usan parámetros nullable bien formados; prohibido concatenar texto del usuario.
- **Base de datos:** usuario de aplicación con `db_datareader` y `db_datawriter`; sin DDL ni `sa`; `encrypt=true` en producción con certificado válido.
- **Secretos:** `.env` fuera del repositorio; `.env.example` con marcadores; CI con secretos; rotar cualquier clave que haya estado en un documento.
- **Menores:** el padre solo ve a sus hijos; los reportes consolidados son solo para ADMIN; no se registran datos sensibles en logs; consentimiento para notificaciones.
- **Auditoría:** `recargas.id_admin_aprueba`, `movimientos.id_usuario`, `logros.id_docente`; la bitácora completa es una extensión del anexo B.

### 8.7 Errores, logging y mensajes

| Situación | Comportamiento |
|---|---|
| Error de validación | Se marca el campo y se muestra el mensaje debajo; no se llama al servicio |
| Error de negocio | `NegocioException` con código y mensaje en español; se muestra en diálogo o Toast |
| Error técnico | Se registra con traza en `logs/app.log`; al usuario se le muestra un mensaje comprensible con código de referencia |
| Sin conexión | Diálogo con pasos: revisar servicio, puerto 1433, `.env`, firewall y nombre de instancia |
| Operación exitosa con saldo | Toast de confirmación; se actualiza la tabla y la barra de estado |
| Acceso no autorizado | «No tiene permisos para realizar esta acción»; no llega al DAO |

El log usa `java.util.logging` con `FileHandler` rotativo (5 MB, 3 archivos), nivel configurable por `LOG_LEVEL`, y nunca incluye contraseñas, tokens ni datos completos de menores.

### 8.8 Interfaz y navegación

- `LoginFrame` contiene `PanelLogin`; al autenticar se crea `MainFrame` y se cierra el login.
- `MainFrame` tiene cabecera (título, usuario, rol, cerrar sesión), oeste con `JScrollPane` y `JTree`, centro con `CardLayout` y sur con barra de estado (nombre, rol, saldos si aplica).
- El árbol se construye desde `Modulo.values()` filtrado por `Permisos.puedeVerModulo(rol, modulo)`; no se codifican listas por rol en la vista.
- Al seleccionar un nodo se muestra el panel y se llama a `refrescar()`; las consultas usan `SwingWorker` y deshabilitan el botón que las dispara.
- Los paneles no tienen SQL ni reglas: reciben datos del controlador y muestran errores.
- Los formularios usan `GridBagLayout`; las tablas usan `PanelTablaBase`; los mensajes usan `Toast` o `DialogoConfirmacion`.

### 8.9 Notificaciones (M10a y M10b)

La tabla `notificaciones` es una cola de salida:

| Columna | Tipo | Uso |
|---|---|---|
| id_notificacion | INT IDENTITY | PK |
| id_usuario | INT | Destinatario (padre o tutor) |
| canal | VARCHAR(10) | EMAIL o WHATSAPP |
| destino | NVARCHAR(150) | Correo o teléfono enmascarado en logs |
| plantilla | VARCHAR(50) | recarga_aprobada, logro_otorgado, compra_realizada |
| estado | VARCHAR(12) | PENDIENTE, ENVIADO, ERROR |
| intentos | INT | Reintentos con backoff |
| ultimo_error | NVARCHAR(255) | Sin datos sensibles |
| fecha_creacion | DATETIME2 | Alta en la cola |
| fecha_envio | DATETIME2 | Confirmación |

El servicio encola en la misma transacción de la operación de negocio cuando la bandera está activa; un procesador aparte envía y actualiza el estado. Si el envío falla, la recarga o compra ya está confirmada: la notificación queda en ERROR y se reintenta. En modo `dry-run` se escribe una línea en el log y se marca ENVIADO con la marca `SIMULADO`.

### 8.10 Configuración

```properties
# .env.example (sin valores reales)
APP_NAME=EscuelaVirtual
APP_ENV=dev
LOG_LEVEL=INFO

DB_URL=jdbc:sqlserver://localhost:1433;databaseName=EscuelaVirtual;encrypt=true;trustServerCertificate=true
DB_USER=app_escuela
DB_PASSWORD=

NOTIF_EMAIL_ENABLED=false
NOTIF_WHATSAPP_ENABLED=false
SMTP_HOST=
SMTP_PORT=587
SMTP_USER=
SMTP_PASSWORD=
WHATSAPP_TOKEN=
WHATSAPP_PHONE_ID=
```

`Config` ya resuelve propiedad del sistema > variable de entorno > `.env`. El instalador incluye `.env.example` y una guía; el usuario final completa los valores en el primer arranque.

<!-- pagebreak -->

## 9. Modelo de datos, scripts y conciliación

### 9.1 Convenciones

- Tablas en plural y `snake_case`: `usuarios`, `billeteras`, `productos`, `logros`, `recargas`, `movimientos`, `notificaciones`.
- Claves primarias `id_<entidad>` con `IDENTITY(1,1)`; claves foráneas `<entidad>_id` o `id_<entidad>` según el documento, pero consistente.
- Fechas `DATETIME2` con `SYSDATETIME()`; dinero `DECIMAL(10,2)`; texto `NVARCHAR`; booleanos `BIT`.
- Nombres de restricciones `CK_<tabla>_<regla>`, `FK_<tabla>_<referencia>`, `IX_<tabla>_<columnas>`.
- Los scripts no contienen contraseñas reales ni datos personales de estudiantes reales.

### 9.2 Orden de scripts

| Script | Contenido | Ejecuta | ¿Destructivo? |
|---|---|---|---|
| `00_usuario_bd.sql` | Login y usuario de aplicación con permisos mínimos | DBA | No |
| `01_creacion_base_datos.sql` | Base, tablas, CHECK, FK, índices, vista de cuadre | DBA o sa en desarrollo | No (solo crea si no existe) |
| `02_datos_iniciales.sql` | Usuarios, billeteras, productos y recompensas | DBA | No (idempotente) |
| `03_base_pruebas.sql` | Base `EscuelaVirtualTest`, mismo esquema y datos mínimos | Desarrollador | Sí, solo sobre la base de pruebas |
| `04_extension_notificaciones.sql` | Tabla `notificaciones`, teléfono y consentimiento | DBA | No |

Nunca se ejecuta un `DROP DATABASE` sobre `EscuelaVirtual`. Para reiniciar desarrollo existe un script separado `99_reset_dev.sql`, marcado como destructivo y prohibido en producción.

### 9.3 Vista de cuadre

```sql
CREATE VIEW vw_cuadre_billeteras AS
SELECT b.id_billetera,
       b.id_estudiante,
       b.saldo_dinero,
       ISNULL((SELECT SUM(r.monto) FROM recargas r
               WHERE r.id_billetera = b.id_billetera AND r.estado = 'APROBADA'), 0)
       - ISNULL((SELECT SUM(m.monto_total) FROM movimientos m
                 WHERE m.id_billetera = b.id_billetera AND m.metodo_pago = 'DINERO'), 0)
           AS dinero_calculado,
       b.saldo_monedas,
       ISNULL((SELECT SUM(l.monedas) FROM logros l
               WHERE l.id_estudiante = b.id_estudiante), 0)
       - ISNULL((SELECT SUM(m.monto_total) FROM movimientos m
                 WHERE m.id_billetera = b.id_billetera AND m.metodo_pago = 'MONEDAS'), 0)
           AS monedas_calculadas
FROM billeteras b;
```

La prueba CP-20 falla si `saldo_dinero <> dinero_calculado` o `saldo_monedas <> monedas_calculadas` en alguna fila. Se ejecuta al cerrar cada etapa y antes de la demo.

### 9.4 Datos iniciales y hashes

- El script `02_datos_iniciales.sql` contiene hashes PBKDF2 precalculados, no contraseñas.
- La herramienta `GeneradorHash` recibe una clave y devuelve `120000$saltBase64$hashBase64`; se ejecuta desde el IDE o con `mvn exec:java`.
- Los usuarios de prueba usan las claves del documento; se cambian en la primera demo si el repositorio se hace público.
- Los productos se cargan con precios en dinero y/o monedas; las recompensas solo con monedas.

### 9.5 Base de pruebas

`03_base_pruebas.sql` crea `EscuelaVirtualTest` con el mismo DDL, un usuario de prueba y datos mínimos. Las pruebas de integración leen `DB_URL_TEST`, `DB_USER_TEST` y `DB_PASSWORD_TEST`. Cada prueba limpia sus tablas en `@BeforeEach` o envuelve el caso en una transacción con rollback. La base de pruebas no se usa para la demo.

### 9.6 Respaldos y migraciones

- Antes de cada demo: backup completo de `EscuelaVirtual` y copia de los scripts.
- Los cambios de esquema se hacen con un script nuevo numerado; no se edita un script ya ejecutado en la demo.
- Si el proyecto crece, migrar a Flyway o Liquibase; para el alcance actual, scripts versionados son suficientes.
- Los datos reales de la institución no se usan en desarrollo; solo datos ficticios.

<!-- pagebreak -->
## 10. Plan de pruebas y aseguramiento de la calidad

### 10.1 Niveles y herramientas

| Nivel | Alcance | Herramienta | Cuándo | Responsable |
|---|---|---|---|---|
| Unitario | Dominio, validaciones, permisos, hashes, cálculos | JUnit 5, etiqueta `unit` | Cada PR | BE + QA |
| Integración | DAO y servicios contra SQL Server de pruebas | JUnit 5, Failsafe, etiqueta `integration` | Cada PR que toque datos | BE + BD |
| Funcional | Recorridos por rol y CP | Guion manual y evidencia | Cierre de tarea y etapa | QA + LP |
| Regresión | CP críticos después de un cambio | Guion reducido | Antes de demo | QA |
| Rendimiento | Listados y saldo con 5 000 registros | Medición con `System.nanoTime` y datos de carga | S01, S06 y cierre | BE |
| Aceptación | Criterios de la institución | Checklist firmado | Cierre de etapa | LP + asesor |

### 10.2 Matriz de casos de prueba

| CP | Caso | Tarea | Nivel | Evidencia |
|---|---|---|---|---|
| CP-01 | Login de administrador y 9 módulos | T08, T09 | Funcional | Captura y log |
| CP-02 | Login con clave incorrecta | T08 | Unitario/funcional | Mensaje y contador |
| CP-03 | Padre consulta un hijo ajeno | T10, S01 | Integración | Excepción de permiso |
| CP-04 | Registro de recarga PENDIENTE | S02 | Integración | Fila y saldo sin cambio |
| CP-05 | Aprobación de recarga | S02 | Integración | Saldo +20 y estado APROBADA |
| CP-06 | Compra con dinero | S03 | Integración | Movimiento, saldo y stock |
| CP-07 | Compra sin saldo | S03 | Integración | Sin movimiento ni stock |
| CP-08 | Compra sin stock | S03 | Integración | Rechazo y sin descuento |
| CP-09 | Logro por docente | S04 | Integración | Logro y +10 monedas |
| CP-10 | Autoasignación de monedas | S04 | Integración | Permiso denegado |
| CP-11 | Canje con monedas | S04 | Integración | −100 monedas y −1 stock |
| CP-12 | Canje sin monedas | S04 | Integración | Sin cambios |
| CP-13 | Coherencia de los dos saldos | S01, S04 | Integración | 100.00 y 50 monedas |
| CP-14 | Rollback de compra | S03 | Integración | Trigger de prueba y saldo intacto |
| CP-15 | Reporte y CSV | S06 | Funcional | Archivo abierto en Excel |
| CP-16 | Bloqueo tras 3 intentos | T08 | Integración | Cuenta bloqueada 15 min |
| CP-17 | Usuario inactivo | T08 | Integración | Login denegado |
| CP-18 | Árbol de módulos por rol | T09 | Funcional | Captura por rol |
| CP-19 | Compra concurrente de la última unidad | S03 | Integración | Un éxito, un rechazo, stock 0 |
| CP-20 | Cuadre de todas las billeteras | S01, S06 | Integración | `vw_cuadre_billeteras` sin diferencias |

### 10.3 Datos y ambientes

- **Desarrollo:** datos ficticios, 7 usuarios, 3 billeteras, 12 productos.
- **Pruebas:** `EscuelaVirtualTest`, datos mínimos y limpieza por prueba.
- **Carga:** 500 usuarios, 100 productos y 5 000 movimientos generados con `05_datos_carga.sql`; se mide el listado y el saldo.
- **Demo:** base restaurada desde backup, datos consistentes y sin errores abiertos.

### 10.4 Pruebas especiales

**Concurrencia.** Dos hilos compran la última unidad con `CountDownLatch`; se espera exactamente un éxito, un `STOCK_INSUFICIENTE`, stock 0 y un solo movimiento. Se repite para el saldo con dos compras que juntas superan el saldo.

**Rollback.** En la base de pruebas se crea un trigger `TR_Movimientos_Error` que lanza un error al insertar; se ejecuta la compra y se verifica que saldo, stock y movimientos no cambian. El trigger se elimina al terminar la prueba.

**Seguridad.** Intentos de inyección en login y búsqueda; llamada directa al servicio con rol incorrecto; acceso a billetera ajena; usuario inactivo; verificación de que el log no contiene contraseñas ni tokens.

**CSV.** Abrir el archivo en Excel y LibreOffice; validar BOM UTF-8, separador `;`, tildes, comillas, saltos de línea y fechas.

**Instalador.** VM Windows limpia sin JDK; instalar, configurar `.env`, iniciar sesión, comprar, desinstalar y verificar que no quedan credenciales.

### 10.5 Defectos y severidad

| Severidad | Definición | Tiempo objetivo | Bloquea entrega |
|---|---|---|---|
| S1 | Pérdida de datos, saldo negativo, acceso indebido o caída total | 4 h | Sí |
| S2 | Función principal incorrecta sin pérdida de datos | 1 día | Sí |
| S3 | Error secundario con solución manual | 3 días | No |
| S4 | Cosmético o texto | Backlog | No |

Un defecto se cierra solo con la corrección verificada y el CP afectado reejecutado. Los S1 y S2 se revisan en la daily y se escalan al líder.

### 10.6 Criterios de aceptación por etapa

**Etapa 1:** CP-01, CP-02, CP-03, CP-16, CP-17 y CP-18 verdes; login y ABU de usuarios; 9 paneles prototipo; scripts reproducibles; `mvn verify`; JAR; manual.

**Etapa 2:** CP-01 a CP-20 verdes; M03–M09 operativos; reportes CSV; M10a/M10b reales o simulados; instalador; manual e informe final.

**Criterio transversal:** ningún saldo negativo, ninguna diferencia en `vw_cuadre_billeteras`, ningún acceso a datos de otro estudiante y ningún secreto en el repositorio.

<!-- pagebreak -->

## 11. Gestión del proyecto

### 11.1 Flujo de trabajo en Git

- `main` protegida; no se empuja directamente.
- Ramas `feat/T08-login`, `fix/CP-06-saldo`, `docs/plan` y `chore/build`.
- Un PR por tarea o por corrección; título con el ID; descripción con qué, por qué, cómo se probó y capturas.
- Al menos una revisión de otra persona; el autor no aprueba su propio PR.
- Commits con convención `tipo(scope): descripción` en español: `feat(auth): login contra SQL Server`, `fix(compra): controlar stock concurrente`.
- Tags `v0.1-etapa1` y `v0.2-etapa2`; nota de versión con alcance y limitaciones.

### 11.2 Tablero y trazabilidad

Cada tarea del cronograma es un issue con: ID, objetivo, criterio de aceptación, responsable, estimación, dependencia y checklist. Columnas: Backlog, Listo, En curso, Revisión, Hecho. Etiquetas: `etapa-1`, `etapa-2`, `modulo-m01`…`modulo-m10`, `tipo-bug`, `tipo-doc`, `prioridad-alta`, `bloqueado`.

El issue se cierra solo cuando el PR está mezclado, el CP está verificado y la documentación está actualizada. El tablero es la fuente de verdad del avance; el cronograma es la línea base de fechas.

### 11.3 Ceremonias y comunicación

| Ceremonia | Frecuencia | Duración | Salida |
|---|---|---|---|
| Daily asíncrona | Lunes a viernes | 10 min | Qué hice, qué haré, qué me bloquea |
| Revisión con asesor | Miércoles | 30 min | Demo, riesgos y decisiones |
| Revisión de código | Por PR | 24 h | Aprobación o cambios |
| Retrospectiva | Viernes | 30 min | Una mejora concreta para la semana |
| Revisión de riesgos | Semanal | 15 min | Riesgo, dueño, mitigación y fecha |

El canal principal es el repositorio (issues y PR); el canal inmediato es el grupo del equipo. Las decisiones que cambian alcance, esquema o fechas se registran en una ADR y se comunican al asesor.

### 11.4 Métricas

| Métrica | Meta | Frecuencia |
|---|---|---|
| Tareas cerradas vs. planificadas | ≥ 85 % por etapa | Semanal |
| CP críticos verdes | 100 % al cierre | Por etapa |
| Build verde | ≥ 95 % de PR | Semanal |
| Cobertura de servicios y dominio | ≥ 60 % | Por etapa |
| Defectos S1/S2 abiertos | 0 al cierre | Diario |
| Tiempo de consulta con 5 000 registros | < 2 s | S01, S06 |
| Desviación de cronograma | ≤ 2 días por etapa | Semanal |

### 11.5 Control de cambios

Un cambio de alcance, esquema o fecha requiere: issue de cambio, impacto en horas y riesgo, ADR si afecta arquitectura, aprobación del líder y actualización de este plan. No se aceptan cambios verbales en la demo. Los cambios aprobados se registran en el historial de versiones del documento.

### 11.6 Documentación

- `docs/Arquitectura_Sistema_Gestion_Escolar.docx`: se actualiza con las ADR y las correcciones de los hallazgos.
- `docs/Arquitectura_Real_Etapa1.md`: arquitectura construida al cierre de la Etapa 1, con las diferencias frente al §8 de este plan (T13). Es la referencia técnica vigente donde ambos discrepen.
- `docs/Manual_Usuario_Etapa1.md`: manual por rol de las pantallas existentes (T13).
- `docs/Plan_Desarrollo_por_Etapas.md`: este documento; se versiona en Git.
- `README.md`: requisitos, configuración, ejecución, scripts y solución de problemas.
- Manual de usuario: por rol, con capturas y flujos.
- Informe de pruebas: CP, evidencia, defectos y estado.
- Guía de instalación: Windows, SQL Server, `.env` y rollback.

<!-- pagebreak -->
## 12. Riesgos y mitigación

| ID | Riesgo | Prob. | Impacto | Mitigación | Dueño | Señal de alerta |
|---|---|---|---|---|---|---|
| RS-01 | Puntos de negocio abiertos: nombre de monedas, reglas de recompensa, medios de pago | Alta | Medio | Parametrizar en catálogo y constantes; decidir antes de S04 | LP | Cambios de precio en plena demo |
| RS-02 | Traducción MySQL → T-SQL incompleta | Media | Medio | Script 01 probado y revisado por BD | BD | Error de sintaxis al ejecutar |
| RS-03 | SQL Server no disponible o mal configurado | Alta | Alto | Docker en desarrollo; guía y diálogo de conexión; backup | BE + BD | `Connection refused` o timeout |
| RS-04 | Acceso indebido a datos de menores | Baja | Muy alto | Permisos en servicio, vínculo padre–hijo, logs sin PII, revisión | BE | CP-03 o CP-10 falla |
| RS-05 | Descuadre entre saldo y registros | Media | Muy alto | Transacciones, condiciones de saldo, vista de cuadre y CP-20 | BE + BD | Diferencia en `vw_cuadre_billeteras` |
| RS-06 | Concurrencia sin bloqueo | Media | Alto | `UPDLOCK`, `rowcount`, reintento de deadlock y CP-19 | BE | Stock o saldo negativo |
| RS-07 | Choque entre GUI Builder y código a mano | Alta | Bajo | Reglas de edición, `.form` versionados, componentes base a mano | UI | PR con `initComponents` modificado |
| RS-08 | Dependencia JDBC inexistente o incompatible | Alta | Alto | Usar `12.10.2.jre11`; verificar build en CI/local | BE | Maven no resuelve el artefacto |
| RS-09 | Retraso por carga desbalanceada y BE+QA en una persona | Muy alta | Alto | Rebalanceo, plan de recuperación y línea de corte | LP | Tareas vencidas dos días |
| RS-10 | M10a/M10b sin credenciales o sin red | Alta | Medio | Banderas, modo simulado, guía y entrega condicional | BE | Sin token o SMTP |
| RS-11 | WhatsApp rechaza plantillas o cambia API | Media | Medio | Plantillas aprobadas, adaptador aislado y simulación | BE | Error 400/401 del proveedor |
| RS-12 | Instalador no compila en Linux o falta WiX | Media | Medio | Compilar en VM Windows; app-image como respaldo | BE | `jpackage` falla |
| RS-13 | Conflictos por renombrado de paquete | Media | Medio | Hacerlo en una rama corta y avisar antes de mezclar | BE | PR con cientos de archivos |
| RS-14 | Pérdida de datos de desarrollo | Media | Alto | Backups antes de demo, scripts idempotentes, no usar `DROP` | BD | Base vacía o inconsistente |
| RS-15 | Dependencia de una sola persona | Alta | Alto | Pair programming, revisión, documentación y issues | LP | Una persona bloqueada |

**Riesgos que no se aceptan:** saldo negativo, acceso a datos de otro estudiante, contraseña en claro y pérdida de movimientos. Cualquiera de estos se trata como S1 y detiene la entrega.

## 13. Entregables

| Etapa | Entregable | Formato | Responsable | Fecha | Aceptación |
|---|---|---|---|---|---|
| 0 | Plan de desarrollo | Markdown y DOCX | LP | 26/10 | Revisado y aprobado |
| 1 | Arquitectura actualizada | DOCX | LP + BE | 04/11 | ADR y hallazgos incorporados |
| 1 | Scripts 00–03 | SQL | BD | 27/10 | Ejecutables e idempotentes |
| 1 | Código T01–T14 | Maven | Equipo | 04/11 | `mvn verify` y demo |
| 1 | Manual de usuario | DOCX/PDF | LP | 04/11 | Revisado por el equipo |
| 1 | Informe de pruebas Etapa 1 | DOCX/PDF | QA | 04/11 | CP de etapa verdes |
| 1 | JAR ejecutable | JAR | BE | 04/11 | Arranca en otra máquina |
| 2 | Código S01–S10 | Maven | Equipo | 02/12 | CP-01 a CP-20 |
| 2 | Informe de pruebas final | DOCX/PDF | QA | 02/12 | Sin S1/S2 |
| 2 | Manual final y guía de instalación | DOCX/PDF | LP | 02/12 | Instalación reproducible |
| 2 | Instalador | MSI o app-image | BE | 02/12 | Windows limpio |
| 2 | Release | Tag `v0.2-etapa2` | LP | 02/12 | Nota de versión |

## 14. Próximos pasos

### 14.1 Hoy, 26/10/2026

- [ ] Aprobar D-01 a D-07 y registrar las ADR del capítulo 4.
- [ ] Renombrar paquete y clase principal; actualizar `pom.xml` y configuración de NetBeans.
- [ ] Añadir driver, JUnit, plugins y Maven Wrapper.
- [ ] Implementar `DatabaseConnection` y probar la conexión con Docker o SQL Server local.
- [ ] Crear el tablero, las etiquetas y los issues T01–T14.
- [ ] Versionar `docs/` y este plan.

### 14.2 Mañana, 27/10/2026

- [ ] Cerrar T02 con el DDL y la vista de cuadre.
- [ ] Cerrar T03 con datos y hashes.
- [ ] Cerrar T04 y T05 con modelo, sesión y esqueleto.
- [ ] Revisar el DDL contra ADR-05 y ejecutarlo dos veces.
- [ ] Preparar el guion CP-01 a CP-03.

### 14.3 Semana del 28/10 al 04/11/2026

- [ ] T06 y T07: prototipo y componentes base.
- [ ] T08: login real con bloqueo.
- [ ] T09 y T10: navegación y usuarios.
- [x] T11 y T12: guion y pruebas de integración (03/11/2026; `reports/T12_Informe_Pruebas_Etapa1.md`).
- [x] T13: documentación técnica y manual de usuario (03/11/2026; `README.md`, `docs/Manual_Usuario_Etapa1.md` y `docs/Arquitectura_Real_Etapa1.md`).
- [ ] T14: JAR y cierre de Etapa 1.

### 14.4 Regla de seguimiento

Cada día a las 18:00 se actualiza el tablero y se reporta: tarea cerrada, tarea en curso, bloqueo y riesgo nuevo. Si una tarea lleva dos días vencida, el líder aplica la línea de corte o reasigna; no se acumula deuda invisible.

<!-- pagebreak -->

## Anexo A · Configuración del entorno

### A.1 Windows con SQL Server local

1. Instalar JDK 17 LTS y NetBeans 17 o superior.
2. Instalar SQL Server 2019 Express; habilitar TCP/IP en el puerto 1433 y autenticación mixta.
3. Instalar SQL Server Management Studio o Azure Data Studio.
4. Crear la base con `scripts/01_creacion_base_datos.sql` y los datos con `02_datos_iniciales.sql`.
5. Copiar `.env.example` a `.env` y completar `DB_URL`, `DB_USER` y `DB_PASSWORD`.
6. Abrir el proyecto Maven en NetBeans, seleccionar JDK 17 y ejecutar `com.escuela.virtual.Main`.
7. Verificar el login con `admin@colegio.edu` y la clave de prueba.

### A.2 Linux con Docker (desarrollo)

```bash
docker run --name escuela-sql \
  -e "ACCEPT_EULA=Y" \
  -e "MSSQL_SA_PASSWORD=TU_CLAVE_FUERTE_AQUI" \
  -p 1433:1433 \
  -v "$PWD/scripts:/scripts" \
  -d mcr.microsoft.com/mssql/server:2022-latest

docker exec -it escuela-sql /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'TU_CLAVE_FUERTE_AQUI' -C \
  -i /scripts/01_creacion_base_datos.sql
```

Si la imagen no incluye `mssql-tools18`, usar `/opt/mssql-tools/bin/sqlcmd`. Crear el usuario de aplicación con `00_usuario_bd.sql` y usar ese usuario en `.env`, no `sa`. La contraseña del ejemplo es solo para el contenedor local y no se versiona.

En este equipo ya existe un contenedor llamado `sqlserver` con SQL Server 2025 y la base `EscuelaVirtual`; se puede reutilizar con `docker exec` en lugar de crear `escuela-sql`. El comando anterior sirve para un entorno nuevo o para reproducir la base desde cero.

### A.3 Verificación mínima

```bash
./mvnw clean verify
./mvnw -Pintegration verify -DDB_URL_TEST="jdbc:sqlserver://localhost:1433;databaseName=EscuelaVirtualTest;encrypt=true;trustServerCertificate=true"
java -jar target/escuelavirtual-*.jar
```

<!-- pagebreak -->

## Anexo B · Matriz de trazabilidad

| Paso de la propuesta | Módulo | Pantalla | Controlador | Servicio | Tablas | Tarea |
|---|---|---|---|---|---|---|
| 1. Registro de usuarios | M01, M02 | Login, Usuarios | Login, Usuario | Auth, Usuario | usuarios, billeteras | T08, T10 |
| 2. Asignación de dinero | M04 | Recargar, Aprobar | Recarga | Recarga | recargas, billeteras | S02 |
| 3. Compras en la tienda | M05 | Tienda, Carrito | Compra | Compra, Catálogo | productos, billeteras, movimientos | S03 |
| 4. Reconocimiento de logros | M06 | RegistrarLogro | Logro | Logro | logros, usuarios, billeteras | S04 |
| 5. Acumulación de monedas | M03, M06 | MiBilletera, MisLogros | Billetera, Logro | Billetera, Logro | billeteras, logros, movimientos | S01, S04 |
| 6. Canje de recompensas | M07 | Catálogo, Canjear | Canje, Catálogo | Canje, Catálogo | productos, billeteras, movimientos | S04 |
| 7. Seguimiento y supervisión | M08, M09 | Historial, Reportes | Billetera, Reporte | Billetera, Reporte | movimientos, recargas, logros, productos | S05, S06 |
| 8. Avisos a padres | M10a, M10b | Notificaciones | Notificación | Notificación | notificaciones, usuarios | S07, S08 |

## Anexo C · Matriz final de permisos y módulos

| Acción | ESTUDIANTE | PADRE | DOCENTE | ADMIN |
|---|---|---|---|---|
| Iniciar sesión | Sí | Sí | Sí | Sí |
| Ver billetera propia | Sí | No | No | No aplica |
| Ver billeteras de estudiantes | No | Sí, solo hijos | No | Todas |
| Registrar recarga | No | Sí, a sus hijos | No | Sí |
| Aprobar o rechazar recarga | No | No | No | Sí |
| Comprar con dinero | Sí | No | No | No |
| Canjear con monedas | Sí | No | No | No |
| Consultar logros propios | Sí | No | No | No aplica |
| Consultar logros de hijos | No | Sí | No | Sí |
| Registrar logro | No | No | Sí | Sí |
| Administrar usuarios | No | No | No | Sí |
| Administrar catálogo | No | No | No | Sí |
| Ver historial de hijos | No | Sí | No | Sí |
| Generar reportes | No | Parcial | No | Sí |
| Recibir notificaciones | No | Sí | No | No |

| Rol | Módulos visibles |
|---|---|
| ESTUDIANTE | M01, M03, M05, M06 (consulta), M07 |
| PADRE | M01, M03 (hijos), M04, M06 (hijos, consulta), M08 |
| DOCENTE | M01, M06, M08 |
| ADMIN | M01, M02, M03, M04, M05, M06, M07, M08, M09 |

Esta matriz es la única fuente para el `JTree`, para `Permisos` y para las pruebas CP-03, CP-10 y CP-18. Cualquier cambio se hace aquí y se propaga al código y a la arquitectura.

<!-- pagebreak -->
## Anexo D · Convenciones de código y commits

**Java.** Paquetes en minúsculas; clases en `PascalCase`; métodos y variables en `camelCase`; constantes en `MAYÚSCULAS`; nombres en español para el dominio (`Billetera`, `Recarga`, `Logro`) y en inglés para infraestructura (`Connection`, `DAO`). Un archivo por clase pública. Comentarios Javadoc en clases y métodos públicos. No se usa `System.out` en producción; se usa el logger.

**SQL.** Palabras reservadas en mayúsculas; nombres en `snake_case`; una cláusula por línea; `PreparedStatement` con parámetros; sin `SELECT *` en código de producción; siempre listar columnas.

**Commits.** `tipo(scope): descripción` con tipo `feat`, `fix`, `docs`, `test`, `refactor`, `chore`; scope con el módulo o tarea (`auth`, `compra`, `T08`). Ejemplo: `feat(auth): bloquear cuenta tras tres intentos`.

**Ramas.** `feat/T08-login`, `fix/CP-06-saldo`, `docs/plan-desarrollo`, `chore/build-java17`.

**PR.** Título con ID; descripción con objetivo, cambios, pruebas ejecutadas y capturas; checklist de DoD; un revisor; sin mezclar cambios de alcance.

## Anexo E · Checklist de tarea y de PR

**Antes de empezar.**
- [ ] El issue tiene criterio de aceptación, dueño, estimación y dependencia.
- [ ] Existe el wireframe, el DDL o el contrato del servicio.
- [ ] Los datos de prueba están disponibles.

**Antes del PR.**
- [ ] `./mvnw clean verify` verde.
- [ ] Pruebas unitarias nuevas o actualizadas.
- [ ] Pruebas de integración si toca base de datos.
- [ ] Sin secretos, credenciales ni datos personales.
- [ ] Sin SQL en la vista ni lógica de negocio en el controlador.
- [ ] Documentación y `.env.example` actualizados.
- [ ] Captura o evidencia de la funcionalidad.

**Antes de cerrar.**
- [ ] PR revisado y mezclado.
- [ ] CP verificado y registrado.
- [ ] Tablero actualizado.
- [ ] Riesgos nuevos registrados.

## Anexo F · Glosario

| Término | Definición |
|---|---|
| Billetera | Conjunto de dos saldos de un estudiante: dinero para gastos y monedas de recompensa |
| Moneda especial | Punto virtual por logros; se canjea por productos o beneficios, nunca por dinero |
| Logro | Reconocimiento académico o de convivencia con monedas otorgadas |
| Recarga | Dinero que un padre o tutor asigna al saldo de un estudiante y que el admin aprueba |
| Canje | Uso de monedas para obtener un producto o beneficio del catálogo |
| Compra | Adquisición de un producto con el saldo de dinero |
| JPanel | Contenedor Swing que representa una pantalla o sección |
| CardLayout | Gestor que muestra un solo panel activo dentro de un contenedor |
| DAO | Objeto de acceso a datos que encapsula el SQL de una tabla |
| T-SQL | Dialecto SQL de Microsoft SQL Server |
| ADR | Registro de decisión de arquitectura |
| CP | Caso de prueba |
| DoD | Definición de terminado |
| Outbox | Cola persistente de notificaciones pendientes de envío |

## Anexo G · Referencias

1. Arquitectura_Sistema_Gestion_Escolar.docx. Documento de arquitectura, versión 1.0, 2026.
2. Cronograma Actividades.xlsx. Cronograma de actividades del proyecto, 2026.
3. Documentación de Java SE 17 y Swing. Oracle.
4. Documentación de Microsoft JDBC Driver for SQL Server. Microsoft.
5. Documentación de SQL Server 2022 y T-SQL. Microsoft.
6. Documentación de JUnit 5. JUnit Team.
7. Documentación de Jakarta Mail y WhatsApp Cloud API. Eclipse Foundation y Meta.
8. Documentación de `jpackage`. Oracle.

## Historial de versiones

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 26/10/2026 | Equipo del proyecto | Versión inicial basada en la arquitectura y el cronograma; incorpora diagnóstico, ADR, plan de recuperación y matrices |

---

**Fin del plan.** Este documento se mantiene en `docs/Plan_Desarrollo_por_Etapas.md` y se exporta a `docs/Plan_Desarrollo_por_Etapas.docx` para su entrega. Cualquier cambio de alcance, fecha o arquitectura se registra en el historial de versiones y se comunica al equipo y al asesor.
