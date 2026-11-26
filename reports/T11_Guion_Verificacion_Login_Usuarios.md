# T11 · Guion de verificación del flujo «login → usuarios»

| Campo | Detalle |
|---|---|
| Tarea | T11 · Guion de verificación login → usuarios (Etapa 1) |
| Casos cubiertos | CP-01, CP-02, CP-03 y CP-18, más los criterios de cierre de T10 |
| Responsable | QA |
| Fecha de ejecución | 03/11/2026 |
| Rama / estado | `develop`, árbol de trabajo con las correcciones de coherencia de T10 sin confirmar |
| Entorno | Linux · OpenJDK 25 compilando con `--release 17` · SQL Server 2025 en contenedor `sqlserver`, base `EscuelaVirtual` |
| Documentos fuente | `Plan_Desarrollo_por_Etapas.md` (§6.2, §10.2, anexo C, ficha T03) y `Arquitectura_Sistema_Gestion_Escolar.docx` (§8.2) |
| Guion automatizado | `src/test/java/com/mycompany/escuelavirtual/service/GuionT11Test.java` |
| Evidencia | `reports/T11/` |
| Veredicto | **CP-02 y CP-03 verdes · CP-01 parcial · CP-18 bloqueado por T09** |

---

## 1. Objetivo y alcance

Verificar el recorrido completo «inicio de sesión → navegación por rol → gestión de usuarios» y dejar evidencia reejecutable de CP-01, CP-02, CP-03 y CP-18, junto con los criterios de cierre de T10 (alta, edición, baja lógica y vínculo padre–hijo).

Queda fuera: módulos de Etapa 2 (billetera, recargas, tienda, logros y reportes), que se verifican en S01–S06 y T12.

## 2. Precondiciones

1. Contenedor `sqlserver` activo y base `EscuelaVirtual` con los datos iniciales de T03 (7 usuarios, 3 billeteras, 12 productos).
2. Archivo `.env` con `DB_URL`, `DB_USER` y `DB_PASSWORD` válidos; sin credenciales en el repositorio.
3. Proyecto compilado sin errores.
4. Ningún usuario de prueba bloqueado por intentos fallidos (`usuarios.intentos_fallidos = 0`).

## 3. Datos de prueba

Credenciales de los datos iniciales, según la ficha de T03 del plan de desarrollo.

| Rol | Correo | Clave | Observaciones |
|---|---|---|---|
| ADMIN | `admin@colegio.edu` | `Admin123*` | Ana María Salazar Ríos |
| DOCENTE | `docente@colegio.edu` | `Docente123*` | Rosa Elena Quispe Huamán |
| PADRE (propietario) | `padre1@correo.com` | `Padre123*` | Miguel Ángel Torres Vega · hijos: estudiantes 5 y 6 |
| PADRE (ajeno) | `padre2@correo.com` | `Padre123*` | Ana Lucía Ramos Cárdenas · hijo: estudiante 7 |
| ESTUDIANTE | `estudiante1@colegio.edu` | `Estudiante123*` | Luis Fernando Torres Ramos · hijo de `padre1` |

Usuario que crea y elimina el propio guion: `qa.t11@colegio.edu` / `Clave123*`.

## 4. Cómo reejecutar el guion

```bash
mvn -Dtest=GuionT11Test test          # pasos automatizados (se omiten solos sin SQL Server)
```

Los pasos manuales 5 a 8 se ejecutan sobre la aplicación en marcha. La ejecución del 03/11/2026 se hizo con `javac` y el lanzador de la plataforma JUnit porque Maven todavía no está en el `PATH` ni existe el wrapper (pendiente de T01).

## 5. Pasos del guion

### Bloque A · Inicio de sesión (CP-01 y CP-02)

| # | Acción | Datos | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|---|
| 1 | Abrir la aplicación e iniciar sesión | `admin@colegio.edu` / `Admin123*` | Se autentica, se cierra el login y se abre el `MainFrame` | Correcto: rol ADMIN, cuenta activa, `ultimo_acceso` actualizado y contador de intentos en 0 | `mainframe-ADMIN.png` |
| 2 | Comprobar que el correo se normaliza | `  Admin@Colegio.EDU  ` | Entra igual: el correo se recorta y pasa a minúsculas | Correcto | `GuionT11Test#cp01LoginAdministrador` |
| 3 | Contar los módulos disponibles en el `MainFrame` | — | **9 módulos (M01–M09) en el árbol de navegación del administrador** | **Incumple: no existe árbol de módulos; el administrador solo ve su panel de inicio con 3 pestañas** | `mainframe-ADMIN.png` · defecto D-T11-01 |
| 4 | Iniciar sesión con clave incorrecta | `admin@colegio.edu` / `EstaNoEsLaClave9*` | Mensaje «El correo o la contraseña no son correctos», no se abre el `MainFrame` y el contador de intentos sube a 1 | Correcto: `CREDENCIALES_INVALIDAS`, `intentos_fallidos` 0 → 1 | `GuionT11Test#cp02ClaveIncorrecta` |
| 5 | Volver a iniciar sesión con la clave correcta | `admin@colegio.edu` / `Admin123*` | Entra y el contador vuelve a 0 | Correcto: `intentos_fallidos` 1 → 0 | ídem |

> El mensaje es el mismo para un correo inexistente y para una clave incorrecta: es deliberado, para no revelar qué correos existen.

### Bloque B · Navegación por rol (CP-18)

| # | Acción | Datos | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|---|
| 6 | Iniciar sesión con cada uno de los 4 roles y capturar la pantalla | Las 4 cuentas de §3 | Cada rol ve en el árbol exactamente los módulos del anexo C: ADMIN M01–M09; PADRE M01, M03, M04, M06, M08; DOCENTE M01, M06, M08; ESTUDIANTE M01, M03, M05, M06, M07 | **Bloqueado: no hay árbol de módulos en ninguno de los 4 roles.** Lo que sí ocurre: cada rol abre su panel de inicio correcto y ningún rol ve el de otro | `mainframe-ADMIN.png`, `mainframe-DOCENTE.png`, `mainframe-PADRE.png`, `mainframe-ESTUDIANTE.png` |
| 7 | Comprobar la matriz que alimentará el árbol | — | Las acciones del anexo C se conceden y se niegan por rol | Correcto: 7 pruebas verdes sobre `Permisos` y 3 comprobaciones contra la base de datos (padre y docente rechazados en «administrar usuarios», admin aceptado) | `PermisosTest`, `GuionT11Test#cp18PermisosPorRol` |

### Bloque C · Aislamiento entre familias (CP-03)

| # | Acción | Datos | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|---|
| 8 | Como `padre2`, consultar la billetera del estudiante 5 (hijo de `padre1`) | — | Se rechaza por permiso (RN-08) | Correcto: `PERMISO_DENEGADO` | `GuionT11Test#cp03PadreConHijoAjeno` |
| 9 | Repetir con movimientos, logros y lista de hijos de otro padre | — | Se rechaza en los cuatro accesos | Correcto: `PERMISO_DENEGADO` en los cuatro | ídem |
| 10 | Como `padre1`, consultar la billetera del estudiante 5 | — | Se permite: el vínculo sale de `usuarios.id_padre` | Correcto | ídem |
| 11 | Verificar en pantalla la lista de hijos de `padre1` | — | Solo aparecen sus hijos | Correcto: muestra a los estudiantes 5 y 6, no al 7 | `mainframe-PADRE.png` |

### Bloque D · Gestión de usuarios (criterios de cierre de T10)

| # | Acción | Datos | Resultado esperado | Resultado obtenido | Evidencia |
|---|---|---|---|---|---|
| 12 | Alta de estudiante con padre asignado | `QA Pruebas T11`, `qa.t11@colegio.edu`, 5.º B, padre = `padre1` | Se crea activo, con el vínculo y con billetera en 0.00 / 0 monedas | Correcto; los espacios del formulario se recortan | `GuionT11Test#altaDeEstudianteConVinculo` |
| 13 | Comprobar el vínculo desde el lado del padre | — | El nuevo hijo aparece en la lista de `padre1` | Correcto | ídem |
| 14 | Intentar un alta con un correo ya existente | `qa.t11@colegio.edu` | Mensaje de negocio «Ya existe un usuario con ese correo» | Correcto: `CONFLICTO`, sin filtrar el error de SQL Server | ídem |
| 15 | Editar datos y cambiar el padre | Apellidos, sección C, padre = `padre2` | Se guardan los cambios y el vínculo anterior se deshace | Correcto | `GuionT11Test#edicionDeUsuario` |
| 16 | Dar de baja lógica e intentar entrar | `qa.t11@colegio.edu` | `estado = 0` y el login se rechaza por cuenta inactiva | Correcto: `CUENTA_INACTIVA` | `GuionT11Test#bajaLogicaYReactivacion` |
| 17 | Reactivar e intentar entrar | — | Vuelve a `estado = 1` y el login funciona | Correcto | ídem |
| 18 | Intentar que el administrador se dé de baja a sí mismo | `admin@colegio.edu` | Se rechaza y la cuenta sigue activa | Correcto: `CONFLICTO` | `GuionT11Test#candadosDeLaBaja` |
| 19 | Intentar dejar el sistema sin administrador activo | — | Se rechaza | Correcto (verificado también sin base de datos en `UsuarioServiceTest`) | ídem |

## 6. Resultado por caso de prueba

| CP | Caso | Veredicto | Sustento |
|---|---|---|---|
| CP-01 | Login de administrador y 9 módulos | **PARCIAL** | El inicio de sesión cumple íntegramente (pasos 1, 2, 5). El requisito «se abre el `MainFrame` con los 9 módulos» no se cumple: no existe el árbol de navegación (paso 3) |
| CP-02 | Login con clave incorrecta | **VERDE** | Pasos 4 y 5: mensaje correcto, no se abre el `MainFrame`, contador sube y se reinicia |
| CP-03 | Padre consulta un hijo ajeno | **VERDE** | Pasos 8 a 11: rechazo por permiso en los cuatro accesos y comprobación del caso positivo |
| CP-18 | Árbol de módulos por rol | **BLOQUEADO** | Paso 6: la funcionalidad que verifica este caso (T09) no está implementada |
| — | Criterios de cierre de T10 | **VERDE** | Pasos 12 a 19 |

Ejecución automatizada: **8 pasos del guion verdes, 0 fallos**. Suite completa del proyecto: **39 pruebas verdes, 0 fallos, 0 omitidas**.

## 7. Defectos abiertos

| ID | Severidad | Descripción | Impacto | Dueño |
|---|---|---|---|---|
| D-T11-01 | S2 | No existe el árbol de módulos por rol: no hay enum `Modulo` ni `JTree`, y el `MainFrame` registra un único panel de inicio por rol en su `CardLayout`. Tampoco existen los 9 paneles por módulo del prototipo | Impide cerrar CP-01 y CP-18, y con ellos los criterios de salida de la Etapa 1 | UI (T09) |
| D-T11-02 | S3 | Las consultas a la base de datos se ejecutan en el hilo de Swing; solo el inicio de sesión usa `SwingWorker`. Con volúmenes altos la ventana se congelará y se incumpliría el requisito de respuesta en menos de 2 s | Riesgo de rendimiento percibido en T12 y en la demo | BE + UI |
| D-T11-03 | S3 | Las fechas se guardan con `SYSDATETIME()` del contenedor, que está en UTC, y la interfaz las muestra sin convertir: la barra de estado marcó «Último acceso: 03/11/2026 17:14» para un acceso hecho a las 12:14 hora local | Afecta a la lectura de historiales y a los reportes de S06 | BE + BD |

Observación adicional (no es defecto): la carpeta `docs/` está en `.gitignore`, de modo que los documentos del proyecto no se versionan. Por eso este guion y su evidencia se guardan en `reports/`. Conviene resolverlo antes de T13.

## 8. Evidencia adjunta

| Archivo | Contenido |
|---|---|
| `reports/T11/mainframe-ADMIN.png` | Sesión de administrador: indicadores, pestañas de usuarios, recargas y catálogo; sin árbol de módulos |
| `reports/T11/mainframe-DOCENTE.png` | Sesión de docente: registro de logros y estudiantes activos |
| `reports/T11/mainframe-PADRE.png` | Sesión de padre: solo sus dos hijos, saldos y solicitud de recarga |
| `reports/T11/mainframe-ESTUDIANTE.png` | Sesión de estudiante: billetera, tienda e historiales propios |
| `src/test/java/.../service/GuionT11Test.java` | Pasos automatizados y reejecutables del guion |

## 9. Estado del entorno al terminar

El guion crea y elimina su propio usuario; de los datos iniciales solo cambia `ultimo_acceso` de las cuentas con las que se inicia sesión, igual que un uso real.

| Comprobación | Valor al cerrar |
|---|---|
| Usuarios | 7 (sin restos de `qa.t11@colegio.edu`) |
| Billeteras | 3, con saldos 50.00/120, 25.00/80 y 0.00/45 |
| `intentos_fallidos` del administrador | 0, sin bloqueo |
| Diferencias en `vw_cuadre_billeteras` | 0 |

## 10. Firmas

| Rol | Nombre | Fecha | Conforme |
|---|---|---|---|
| QA | | | |
| Líder de proyecto | | | |
| Docente asesor | | | |

> El guion no puede firmarse como cerrado mientras D-T11-01 siga abierto: CP-01 queda parcial y CP-18 sin ejecutar.

---

## Addendum del 03/11/2026 · cierre por T12

D-T11-01 quedó corregido en T12: el `MainFrame` ya construye el árbol de módulos a partir del nuevo enum `Modulo`, y CP-01 y CP-18 se reejecutaron en verde. D-T11-02 y D-T11-03 siguen abiertos como S3 diferidos. El detalle, la evidencia nueva y los criterios de salida de la etapa están en `reports/T12_Informe_Pruebas_Etapa1.md`; este guion queda en condiciones de firmarse.
