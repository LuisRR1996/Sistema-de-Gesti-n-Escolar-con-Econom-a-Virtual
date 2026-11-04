# Sistema de Gestión Escolar con Economía Virtual

Aplicación de escritorio en Java + Swing que gestiona una economía escolar sobre SQL Server:
usuarios por rol, billeteras de dos monedas (soles y monedas de recompensa), recargas con
aprobación del administrador, tienda con stock, logros otorgados por docentes y una vista de
conciliación de saldos.

Este README describe **lo que el repositorio hace hoy**, no lo que el plan de desarrollo
proyecta. Las diferencias entre ambos están listadas en
[docs/Arquitectura_Real_Etapa1.md](docs/Arquitectura_Real_Etapa1.md) §9.

## 1. Estado actual (cierre de la Etapa 1)

| Parte | Estado |
|---|---|
| Inicio de sesión contra SQL Server, con bloqueo por intentos fallidos | Operativo |
| Árbol de navegación por rol (M01–M09) | Operativo |
| Gestión de usuarios: alta, edición, baja lógica, reactivación y vínculo padre–hijo | Operativo |
| Billetera, recargas, tienda, logros y catálogo | Operativos **desde el panel de inicio (M01)** de cada rol |
| Pantallas propias de M02 a M09 | Prototipo: cada nodo abre un panel que nombra el módulo y la tarea que lo construirá |
| Reportes y exportación | Solo el botón «Exportar CSV» de cada tabla; no hay módulo de reportes |
| Notificaciones por correo o WhatsApp | No existe |
| Empaquetado JAR e instalador | No existe (T14) |

Las 9 pantallas de módulo que exige el criterio de salida de la Etapa 1 existen, pero 8 de
ellas son prototipos: la funcionalidad real vive en el panel de inicio de cada rol. El manual
de usuario lo explica pantalla por pantalla.

## 2. Requisitos

| Componente | Versión | Nota |
|---|---|---|
| JDK | 17 o superior | El proyecto compila con `--release 17`. Verificado con OpenJDK 25. |
| SQL Server | 2019 o superior | Probado contra SQL Server 2025 en un contenedor Docker llamado `sqlserver`. |
| Base de datos | `EscuelaVirtual` | Con las 6 tablas, la vista `vw_cuadre_billeteras` y los datos iniciales. |
| Maven | 3.9 o superior | **Opcional pero recomendado.** No está instalado en el equipo de desarrollo actual; la sección 4 explica cómo compilar sin él. |

Dependencias que resuelve Maven (`pom.xml`): `dotenv-java` 3.2.0, `mssql-jdbc` 13.2.0.jre11 y
`junit-jupiter` 5.11.4 (solo pruebas). No hay dependencias de interfaz: la UI es Swing del JDK.

## 3. Configuración

La configuración se lee con precedencia: propiedad del sistema (`-DCLAVE=valor`) → variable de
entorno → archivo `.env` del directorio de trabajo. Un valor vacío cuenta como no configurado.

```bash
cp .env.example .env
```

| Clave | Obligatoria | Ejemplo |
|---|---|---|
| `DB_URL` | **Sí.** Sin ella la aplicación no arranca. | `jdbc:sqlserver://localhost:1433;databaseName=EscuelaVirtual;encrypt=optional;trustServerCertificate=false;` |
| `DB_USER` | No (por defecto, cadena vacía) | `escuela_app` |
| `DB_PASSWORD` | No (por defecto, cadena vacía) | — |
| `APP_NAME`, `APP_ENV`, `LOG_LEVEL` | No | Declaradas en la plantilla; el código todavía no las lee. |

`.env` está en `.gitignore`: no se versiona. `.env.example` sí, y nunca lleva valores reales.

## 4. Cómo compilar y ejecutar

### Con Maven

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.mycompany.escuelavirtual.Main
```

La clase principal está declarada en el `pom.xml` como `exec.mainClass`. El `pom.xml` todavía no
configura `maven-jar-plugin` ni `maven-shade-plugin`, de modo que `mvn package` produce un JAR
sin manifiesto ejecutable ni dependencias embebidas: eso es trabajo de T14.

### Sin Maven, con `javac` (procedimiento verificado el 03/11/2026)

```bash
M=~/.m2/repository
CP="$M/io/github/cdimascio/dotenv-java/3.2.0/dotenv-java-3.2.0.jar:$M/com/microsoft/sqlserver/mssql-jdbc/13.2.0.jre11/mssql-jdbc-13.2.0.jre11.jar"

javac --release 17 -encoding UTF-8 -d target/classes -cp "$CP" $(find src/main/java -name '*.java')
java -cp "target/classes:$CP" com.mycompany.escuelavirtual.Main
```

Requiere que los JAR ya estén en el repositorio local `~/.m2`. Ejecute el comando desde la raíz
del proyecto: `Config` busca el `.env` en el directorio de trabajo.

### Qué ocurre al arrancar

`Main` comprueba primero la conexión a `DB_URL`. Si no responde, muestra un diálogo de error de
conexión y termina: no se abre la ventana de inicio de sesión. La aplicación fija el aspecto
multiplataforma de Swing a propósito, porque la paleta propia supone fondo claro y un tema
oscuro del escritorio dejaría los campos ilegibles.

## 5. Pruebas

9 clases de prueba, 48 pruebas (ejecución registrada en
[reports/T12_Informe_Pruebas_Etapa1.md](reports/T12_Informe_Pruebas_Etapa1.md)):

| Clase | Nivel |
|---|---|
| `util.PermisosTest`, `util.ModuloTest`, `util.ui.PanelTablaBaseTest` | Unitario, sin base de datos |
| `service.AuthServiceTest`, `service.UsuarioServiceTest` | Unitario con doble en memoria |
| `dao.UsuarioDAOTest`, `service.GuionT11Test`, `service.PruebasEtapa1Test` | Integración contra SQL Server |

`util.ui.DemoComponentes` no es una prueba: es una ventana manual para revisar los componentes
de `util.ui`.

```bash
mvn test                              # suite completa
mvn -Dtest=PruebasEtapa1Test test     # casos de la Etapa 1
mvn -Dtest=GuionT11Test test          # guion de verificación de T11
```

Las pruebas de integración se omiten solas si no hay conexión a SQL Server, de modo que la suite
sigue siendo ejecutable sin base de datos.

**Sin Maven no hay forma directa de lanzar la suite.** Las clases de prueba compilan con `javac`
(añadiendo los JAR de JUnit 5.11.4 de `~/.m2` al classpath), pero ejecutarlas necesita un
lanzador: el `junit-platform-console-standalone` no está en el repositorio local. Hasta que
cierre T01 (instalación de Maven o del wrapper), el criterio «`mvn verify` verde» del plan no
puede darse por cumplido.

## 6. Estructura del repositorio

```text
EscuelaVirtual/
├── pom.xml                      Maven; sin wrapper (mvnw) todavía
├── .env.example                 plantilla de configuración (se versiona)
├── README.md
├── docs/                        plan, arquitectura y manual (los .md se versionan)
├── reports/                     guion e informe de pruebas, con capturas
├── src/main/java/com/mycompany/escuelavirtual/
│   ├── Main.java                arranque real
│   ├── EscuelaVirtual.java      segundo punto de entrada, de la plantilla de NetBeans
│   ├── config/                  Config
│   ├── model/ + model/enums/
│   ├── dao/                     BaseDAO y 6 DAO
│   ├── service/                 ServicioBase y 9 servicios
│   ├── controller/              9 controladores, uno por módulo
│   ├── view/                    LoginFrame, MainFrame, 4 paneles de inicio, 2 paneles base
│   ├── session/                 SessionManager
│   └── util/ + util/db/ + util/ui/
└── src/test/java/com/mycompany/escuelavirtual/
```

No existe carpeta `scripts/`: los scripts SQL que crean la base de datos no están versionados.
Para levantar un entorno nuevo hay que pedírselos al responsable de base de datos.

## 7. Cuentas de los datos iniciales

| Rol | Correo | Clave |
|---|---|---|
| ADMIN | `admin@colegio.edu` | `Admin123*` |
| DOCENTE | `docente@colegio.edu` | `Docente123*` |
| PADRE | `padre1@correo.com` | `Padre123*` |
| PADRE | `padre2@correo.com` | `Padre123*` |
| ESTUDIANTE | `estudiante1@colegio.edu` | `Estudiante123*` |

Son credenciales de desarrollo y pruebas. Tres fallos consecutivos bloquean la cuenta 15 minutos.

## 8. Limitaciones conocidas

1. **Las consultas corren en el hilo de Swing.** Solo el inicio de sesión usa `SwingWorker`. Con
   una base de datos lenta la ventana se queda sin responder mientras carga. Defecto D-T11-02 (S3).
2. **Las fechas se muestran sin convertir la zona horaria.** El servidor registra con
   `SYSDATETIME()`; si el contenedor corre en UTC, la interfaz muestra la hora UTC. Defecto
   D-T11-03 (S3).
3. **No hay cambio ni restablecimiento de contraseña.** El administrador fija una clave inicial al
   crear el usuario; al editarlo, el campo de contraseña queda deshabilitado. Nadie puede cambiar
   su propia clave desde la aplicación.
4. **`EscuelaVirtual.java` es un segundo punto de entrada** que solo delega en `Main`. Es un
   residuo de la plantilla de NetBeans.
5. **Los scripts de base de datos no están en el repositorio** (punto 6).
6. **Sin empaquetado.** No hay JAR ejecutable ni instalador.

## 9. Solución de problemas

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| Diálogo «No responde el servidor configurado en DB_URL» al arrancar | SQL Server apagado, `DB_URL` mal escrita o credenciales inválidas | Verificar el contenedor (`docker ps`), el puerto 1433 y los valores del `.env`. El detalle del error de JDBC se imprime en la salida de error estándar. |
| `Falta la configuración obligatoria 'DB_URL'` | No hay `.env` en el directorio de trabajo, o `DB_URL` está vacía | Ejecutar desde la raíz del proyecto y rellenar `DB_URL`. |
| «El correo o la contraseña no son correctos» con credenciales correctas | La cuenta puede estar bloqueada o inactiva, o el usuario no existe | El mensaje es deliberadamente igual para correo inexistente y clave incorrecta. Revisar `usuarios.estado`, `intentos_fallidos` y `bloqueado_hasta`. |
| «La cuenta está bloqueada» | 3 intentos fallidos | Esperar 15 minutos o reiniciar `bloqueado_hasta` e `intentos_fallidos` en la base de datos. |
| Los textos exportados a CSV salen con caracteres raros | — | El CSV se exporta con BOM UTF-8 y separador `;`. Abrirlo en Excel, no en un editor que ignore el BOM. |
| Campos o etiquetas ilegibles | Tema oscuro del escritorio | No debería ocurrir: la aplicación fuerza el aspecto multiplataforma. Si ocurre, reportarlo. |

## 10. Documentación

| Documento | Contenido |
|---|---|
| [docs/Manual_Usuario_Etapa1.md](docs/Manual_Usuario_Etapa1.md) | Manual por rol, con las pantallas reales y capturas |
| [docs/Arquitectura_Real_Etapa1.md](docs/Arquitectura_Real_Etapa1.md) | Arquitectura construida: capas, clases, esquema, reglas y desviaciones del plan |
| [docs/Plan_Desarrollo_por_Etapas.md](docs/Plan_Desarrollo_por_Etapas.md) | Plan de desarrollo (línea base de planificación) |
| [reports/T11_Guion_Verificacion_Login_Usuarios.md](reports/T11_Guion_Verificacion_Login_Usuarios.md) | Guion de verificación del flujo login → usuarios |
| [reports/T12_Informe_Pruebas_Etapa1.md](reports/T12_Informe_Pruebas_Etapa1.md) | Informe de pruebas de integración de la Etapa 1 |
