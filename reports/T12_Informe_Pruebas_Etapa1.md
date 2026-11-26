# T12 · Informe de pruebas de integración de la Etapa 1

| Campo | Detalle |
|---|---|
| Tarea | T12 · Pruebas de integración de Etapa 1 y corrección de defectos |
| Responsable | QA |
| Fecha de ejecución | 03/11/2026 |
| Rama | `feature/t11-guion-de-verificacion-del-flujo-login-usuarios` |
| Entorno | Linux · OpenJDK 25 compilando con `--release 17` · SQL Server 2025 en contenedor `sqlserver`, base `EscuelaVirtual` |
| Casos de la etapa | CP-01, CP-02, CP-03, CP-16, CP-17 y CP-18 (plan de desarrollo §10.6) |
| Documentos fuente | `Plan_Desarrollo_por_Etapas.md` (§10.2, §10.5, §10.6, anexo C) y `reports/T11_Guion_Verificacion_Login_Usuarios.md` |
| Evidencia | `reports/T12/` |
| Veredicto | **Los 6 casos de prueba de la Etapa 1 en verde · sin defectos S1 ni S2 abiertos · 2 defectos S3 diferidos** |

---

## 1. Objetivo y alcance

Ejecutar la matriz de casos de prueba de la Etapa 1 contra la base de datos real, corregir los defectos que bloqueaban el cierre y dejar la evidencia reejecutable.

Entran en el alcance los criterios de salida de la Etapa 1 del §10.6 del plan: CP-01, CP-02, CP-03, CP-16, CP-17 y CP-18, más el criterio transversal (ningún saldo negativo, ninguna diferencia en `vw_cuadre_billeteras`, ningún acceso a datos de otro estudiante y ningún secreto en el repositorio).

Quedan fuera los casos de la Etapa 2 (CP-04 a CP-15, CP-19 y CP-20), que se ejecutan en S01–S06 y se cierran en S10. CP-20 se comprueba aquí solo como criterio transversal de cuadre, no como caso formal.

## 2. Resumen de la ejecución

| Indicador | Valor |
|---|---|
| Casos de prueba de la etapa ejecutados | 6 de 6 |
| Casos en verde | 6 |
| Casos en ámbar o rojo | 0 |
| Pruebas automatizadas del proyecto | 48 correctas, 0 fallidas, 0 omitidas |
| Defectos S1/S2 abiertos | 0 |
| Defectos S3 abiertos | 2 (D-T11-02 y D-T11-03) |
| Diferencias en `vw_cuadre_billeteras` | 0 |

Desglose de la suite automatizada:

| Clase de prueba | Nivel | Pruebas |
|---|---|---|
| `util.PermisosTest` | Unitario | 7 |
| `util.ModuloTest` | Unitario | 3 |
| `util.ui.PanelTablaBaseTest` | Unitario | 5 |
| `service.AuthServiceTest` | Unitario con doble en memoria | 7 |
| `service.UsuarioServiceTest` | Unitario con doble en memoria | 9 |
| `dao.UsuarioDAOTest` | Integración con SQL Server | 3 |
| `service.GuionT11Test` | Integración con SQL Server | 8 |
| `service.PruebasEtapa1Test` | Integración con SQL Server | 6 |
| **Total** | | **48** |

## 3. Resultado por caso de prueba

| CP | Caso | Veredicto | Sustento | Evidencia |
|---|---|---|---|---|
| CP-01 | Login de administrador y 9 módulos | **VERDE** | El administrador se autentica contra SQL Server, se actualiza `ultimo_acceso` y su árbol de navegación contiene M01 a M09 | `PruebasEtapa1Test#cp01LoginYNueveModulos`, `ModuloTest#administradorVeNueveModulos`, `mainframe-ADMIN.png` |
| CP-02 | Login con clave incorrecta | **VERDE** | Mensaje genérico, no se abre el `MainFrame`, `intentos_fallidos` sube a 1 y el acceso correcto lo reinicia | `GuionT11Test#cp02ClaveIncorrecta` |
| CP-03 | Padre consulta un hijo ajeno | **VERDE** | `PERMISO_DENEGADO` en billetera, movimientos, logros y lista de hijos; el padre propietario sí accede | `GuionT11Test#cp03PadreConHijoAjeno` |
| CP-16 | Bloqueo tras 3 intentos | **VERDE** | Al tercer fallo la cuenta queda bloqueada, `bloqueado_hasta` cae a 15 minutos y ni la clave correcta entra; al expirar entra y el contador vuelve a 0 | `PruebasEtapa1Test#cp16BloqueoTrasTresIntentos` y `#cp16ExpiracionDelBloqueo` |
| CP-17 | Usuario inactivo | **VERDE** | La baja lógica impide el login con `CUENTA_INACTIVA` sin acumular intentos; la reactivación lo devuelve | `PruebasEtapa1Test#cp17CuentaInactiva`, `GuionT11Test#bajaLogicaYReactivacion` |
| CP-18 | Árbol de módulos por rol | **VERDE** | Cada rol ve exactamente los módulos del anexo C: ADMIN 9, PADRE 5, ESTUDIANTE 5, DOCENTE 3 | `ModuloTest#modulosPorRol`, `PruebasEtapa1Test#cp18ArbolPorRolConUsuariosReales`, las 4 capturas `mainframe-*.png` |

### Criterio transversal

| Comprobación | Resultado | Sustento |
|---|---|---|
| Ninguna billetera descuadrada | 0 diferencias en `vw_cuadre_billeteras` | `PruebasEtapa1Test#criterioTransversalDeSaldos` |
| Ningún saldo negativo | 4 billeteras, todas con dinero y monedas ≥ 0 | ídem |
| Ningún acceso a datos de otro estudiante | CP-03 verde en los cuatro accesos | `GuionT11Test#cp03PadreConHijoAjeno` |
| Ningún secreto en el repositorio | `.env` y `docs/` ignorados por Git; sin contraseñas en el código versionado | `git check-ignore`, `git grep` sobre el árbol versionado |

### Contraste con el árbol por rol observado

Inventario tomado del `JTree` real de cada sesión, no de la matriz:

| Rol | Módulos en el árbol | Esperado por el anexo C |
|---|---|---|
| ADMIN | M01, M02, M03, M04, M05, M06, M07, M08, M09 | Coincide (9) |
| PADRE | M01, M03, M04, M06, M08 | Coincide (5) |
| ESTUDIANTE | M01, M03, M05, M06, M07 | Coincide (5) |
| DOCENTE | M01, M06, M08 | Coincide (3) |

## 4. Defectos corregidos en esta tarea

### D-T11-01 · S2 · No existía el árbol de módulos por rol

**Síntoma.** El `MainFrame` registraba un único panel de inicio por rol en su `CardLayout`; no había enum `Modulo`, ni `JTree`, ni pantalla por módulo. CP-01 quedaba parcial y CP-18 no se podía ejecutar.

**Corrección.**

| Archivo | Cambio |
|---|---|
| `util/Modulo.java` | Nuevo. Enum con código, título y roles de cada módulo; es la traducción literal de la tabla «Rol / Módulos visibles» del anexo C y la única fuente del árbol y de las pruebas |
| `view/MainFrame.java` | Añade el `JTree` lateral construido con `Modulo.visiblesPara(rol)` y una tarjeta por módulo en el `CardLayout`; la selección de un nodo muestra su tarjeta |
| `view/PanelModuloPrototipo.java` | Nuevo. Pantalla prototipo de los módulos cuya pantalla propia llega en tareas posteriores; nombra el módulo y la tarea del plan que lo implementa |

**Alcance de la corrección.** El árbol es real y completo: cada rol ve exactamente sus módulos y cada nodo abre su pantalla. El nodo M01 abre el panel de inicio del rol, que es donde hoy viven las funciones ya construidas (usuarios, recargas y catálogo para el administrador; billetera, tienda e historiales para el estudiante; hijos y saldos para el padre; logros y aula para el docente). Los nodos M02 a M09 abren el panel prototipo, que es el entregable «9 paneles prototipo» del criterio de salida de la Etapa 1; su contenido definitivo corresponde a T10 y S01–S06 según el cronograma.

**Verificación.** CP-01 y CP-18 reejecutados y en verde, con inventario del árbol y captura por rol.

**Estado.** Cerrado.

### Defectos no reproducidos

CP-02, CP-03 y los criterios de cierre de T10 se reejecutaron íntegros tras el cambio y siguen en verde: el árbol no alteró la autenticación, los permisos ni la gestión de usuarios.

## 5. Defectos abiertos

| ID | Severidad | Descripción | Decisión | Dueño |
|---|---|---|---|---|
| D-T11-02 | S3 | Las consultas a la base de datos se ejecutan en el hilo de Swing; solo el inicio de sesión usa `SwingWorker` | Diferido. No bloquea la entrega (§10.5). Se corrige junto con los paneles de S01–S06, cuando cada panel pase a tener su propia carga de datos; corregirlo ahora obligaría a reescribir los cuatro paneles de inicio que la Etapa 2 va a dividir | BE + UI |
| D-T11-03 | S3 | El contenedor de SQL Server corre en UTC (`SYSDATETIME()` = `GETUTCDATE()`, verificado el 03/11) y la interfaz muestra la hora sin convertir: la barra de estado marcó «19:37» para un acceso de las 14:37 hora local | Diferido. No bloquea la entrega. Debe resolverse antes de S06, porque afecta a los reportes y al CSV. La decisión de fondo —fijar la zona del contenedor o almacenar con desfase y convertir al mostrar— corresponde a BD con el líder | BE + BD |

Ningún defecto S1 o S2 queda abierto, que es la condición de aceptación de T12.

## 6. Observaciones del entorno

1. **La carpeta `docs/` está en `.gitignore`.** Los documentos del proyecto no se versionan; por eso este informe y su evidencia viven en `reports/`. Pendiente de resolver en T13, como ya señalaba el guion de T11.
2. **Maven no está en el `PATH` y no existe el wrapper.** La compilación y la ejecución de las pruebas se hicieron con `javac` y el lanzador de la plataforma JUnit contra los artefactos de `~/.m2`. El criterio «`mvn verify`» del §10.6 no se puede dar por cumplido hasta que T01 cierre.
3. **Dato de más en la base de desarrollo.** Hay 8 usuarios donde los datos iniciales de T03 definen 7: existe `estudiante4@colegio.edu` con `id_usuario = 1009`, creado desde la interfaz en una prueba manual anterior. No afecta a ningún caso de prueba —el árbol y los permisos dependen del rol, no del número de filas— pero conviene restaurar la base antes de la demo, como pide el §10.3.
4. **Las pruebas no ensucian los datos.** `GuionT11Test` y `PruebasEtapa1Test` crean y eliminan su propio usuario (`qa.t11@colegio.edu` y `qa.t12@colegio.edu`) y se omiten solas si no hay SQL Server. Ninguna cuenta de los datos iniciales queda bloqueada: el bloqueo de CP-16 se ensaya sobre el usuario de QA.

## 7. Cómo reejecutar

```bash
mvn test                                   # suite completa: 48 pruebas
mvn -Dtest=PruebasEtapa1Test test          # solo los casos de la Etapa 1 que cierra T12
mvn -Dtest=GuionT11Test test               # guion de T11: CP-02, CP-03 y cierre de T10
```

Precondiciones: contenedor `sqlserver` activo con la base `EscuelaVirtual` y los datos iniciales de T03, y un `.env` con `DB_URL`, `DB_USER` y `DB_PASSWORD` válidos. Las pruebas de integración se omiten solas si no hay conexión, de modo que la suite sigue siendo ejecutable sin base de datos.

Mientras T01 no cierre, la ejecución sin Maven es: compilar `src/main/java` y `src/test/java` con `javac --release 17` contra los JAR de `~/.m2` y lanzar el `Launcher` de la plataforma JUnit sobre el paquete `com.mycompany.escuelavirtual`.

## 8. Evidencia adjunta

| Archivo | Contenido |
|---|---|
| `reports/T12/mainframe-ADMIN.png` | Árbol con los 9 módulos y panel de inicio del administrador (CP-01 y CP-18) |
| `reports/T12/mainframe-PADRE.png` | Árbol con M01, M03, M04, M06 y M08 (CP-18) |
| `reports/T12/mainframe-ESTUDIANTE.png` | Árbol con M01, M03, M05, M06 y M07 (CP-18) |
| `reports/T12/mainframe-DOCENTE.png` | Árbol con M01, M06 y M08 (CP-18) |
| `reports/T12/modulo-prototipo-*.png` | Un nodo de módulo seleccionado por rol: el árbol abre su tarjeta en el `CardLayout` |
| `src/test/java/.../util/ModuloTest.java` | Matriz de módulos por rol (CP-01 y CP-18) |
| `src/test/java/.../service/PruebasEtapa1Test.java` | CP-01, CP-16, CP-17, CP-18 y criterio transversal contra SQL Server |

## 9. Criterios de salida de la Etapa 1

| Criterio del §10.6 | Estado |
|---|---|
| CP-01, CP-02, CP-03, CP-16, CP-17 y CP-18 verdes | Cumplido |
| Login y ABU de usuarios | Cumplido (T08 y T10, reverificados aquí) |
| 9 paneles prototipo | Cumplido con el árbol y las pantallas prototipo de M02–M09 |
| Scripts reproducibles | Fuera del alcance de T12; corresponde a T02, T03 y T13 |
| `mvn verify` | **Pendiente**: Maven no está disponible en el entorno (T01) |
| JAR | **Pendiente**: T14 |
| Manual | **Pendiente**: T13 |

La parte de pruebas de la Etapa 1 queda cerrada. El cierre completo de la etapa depende de T01, T13 y T14.

## 10. Firmas

| Rol | Nombre | Fecha | Conforme |
|---|---|---|---|
| QA | | | |
| Líder de proyecto | | | |
| Docente asesor | | | |

> Segregación de funciones (§7.3 del plan): CP-03 y CP-16, casos críticos, deben ser reejecutados por un integrante distinto de quien los automatizó antes de firmar.
