# Manual de usuario · Etapa 1

| Campo | Detalle |
|---|---|
| Tarea | T13 · Documentación técnica y manual de usuario de Etapa 1 |
| Versión | 1.0 · 03/11/2026 |
| Alcance | Las pantallas que la aplicación tiene hoy, tal como se comportan |
| Documento técnico | [Arquitectura_Real_Etapa1.md](Arquitectura_Real_Etapa1.md) |

Este manual describe la aplicación construida, no las pantallas previstas para etapas
posteriores. Donde una pantalla todavía es un prototipo, se dice y se indica dónde está
mientras tanto la función real.

---

## 1. Antes de empezar

La aplicación es un programa de escritorio que necesita conexión con la base de datos del
colegio. Si el servidor no responde, al abrirla aparece un aviso de error de conexión y el
programa se cierra: no es un fallo de sus credenciales. Avise al responsable técnico.

Pida al administrador su correo y su contraseña inicial. **La aplicación todavía no permite
cambiar la contraseña**: si necesita otra, el administrador debe crearle el usuario de nuevo o
cambiarla directamente en la base de datos.

## 2. Iniciar sesión

La ventana de inicio de sesión tiene tres campos y un botón:

| Elemento | Qué hace |
|---|---|
| **Correo** | Su correo institucional. Se valida el formato antes de enviar nada al servidor. |
| **Contraseña** | Obligatoria. |
| **Mostrar contraseña** | Muestra lo escrito, para comprobar que no hay errores de teclado. |
| **Ingresar** | Entra. La tecla **Enter** hace lo mismo desde cualquiera de los dos campos. |

Si los datos son correctos, la ventana de inicio de sesión se cierra y se abre la ventana
principal. Si no:

| Mensaje | Qué significa |
|---|---|
| «El correo o la contraseña no son correctos.» | La clave está mal **o** ese correo no existe. El mensaje es el mismo a propósito, para no revelar qué correos están registrados. |
| «La cuenta está bloqueada. Vuelva a intentarlo en N minuto(s).» | Hubo **3 intentos fallidos**. El bloqueo dura **15 minutos** y durante ese tiempo no entra ni con la clave correcta. Un acceso correcto reinicia el contador. |
| «La cuenta está inactiva. Contacte al administrador.» | El administrador le dio de baja lógica. Sus datos siguen en el sistema; hay que reactivarlo. |
| «No hay conexión con la base de datos.» | Problema de red o de servidor, no de sus credenciales. |

## 3. La ventana principal

Es igual para los cuatro roles y tiene cuatro zonas:

1. **Cabecera azul.** El título «Escuela Virtual», su nombre con su rol y el botón **Cerrar
   sesión**, que pide confirmación y devuelve a la pantalla de inicio de sesión.
2. **Árbol de módulos, a la izquierda.** Solo los módulos que su rol puede ver. Al hacer clic en
   un nodo, la zona central cambia a esa pantalla. Al entrar queda seleccionado el primer nodo.
3. **Zona central.** La pantalla del módulo seleccionado.
4. **Barra de estado inferior.** El correo de la sesión y la fecha y hora de su último acceso.
   Si es su primer ingreso dice «primero».

> **Sobre la hora:** la barra de estado y las fechas de las tablas muestran la hora que registra
> el servidor, sin convertirla a la hora local. Si el servidor está configurado en UTC, verá su
> acceso de las 14:37 como «19:37». Es un defecto conocido y pendiente (D-T11-03).

### Módulos que ve cada rol

| Módulo | Administrador | Padre | Estudiante | Docente |
|---|:---:|:---:|:---:|:---:|
| M01 · Inicio y sesión | Sí | Sí | Sí | Sí |
| M02 · Usuarios | Sí | — | — | — |
| M03 · Billetera | Sí | Sí | Sí | — |
| M04 · Recargas | Sí | Sí | — | — |
| M05 · Tienda y compras | Sí | — | Sí | — |
| M06 · Logros | Sí | Sí | Sí | Sí |
| M07 · Catálogo y canjes | Sí | — | Sí | — |
| M08 · Supervisión | Sí | Sí | — | Sí |
| M09 · Reportes y CSV | Sí | — | — | — |
| **Total de nodos** | **9** | **5** | **5** | **3** |

Capturas: [mainframe-ADMIN.png](../reports/T12/mainframe-ADMIN.png) ·
[mainframe-PADRE.png](../reports/T12/mainframe-PADRE.png) ·
[mainframe-ESTUDIANTE.png](../reports/T12/mainframe-ESTUDIANTE.png) ·
[mainframe-DOCENTE.png](../reports/T12/mainframe-DOCENTE.png)

### Importante: dónde están las funciones

**Solo el módulo M01 tiene su pantalla real.** Los nodos M02 a M09 abren una pantalla prototipo
que nombra el módulo y la tarea que lo construirá
([captura](../reports/T12/modulo-prototipo-ADMIN.png)). Todo lo que hoy se puede hacer —gestionar
usuarios, recargar, comprar, otorgar monedas— se hace **desde M01**, que abre un panel distinto
para cada rol. Las secciones 4 a 7 describen ese panel.

## 4. Cómo usar las tablas

Todas las tablas de la aplicación funcionan igual:

| Elemento | Qué hace |
|---|---|
| **Buscar** | Filtra las filas mientras escribe, sobre cualquier columna. No distingue mayúsculas. |
| **Exportar CSV** | Guarda **las filas visibles** (las que deja el filtro) en un archivo CSV. |
| Cabeceras | Un clic ordena por esa columna; otro clic invierte el orden. No se pueden reordenar. |
| Selección | Una fila a la vez. Los botones de acción trabajan sobre la fila seleccionada. |

Cuando una tabla no tiene filas, muestra «No hay registros para mostrar.».

El CSV se genera con separador `;` y codificación UTF-8 con BOM, de modo que Excel abre las
tildes y las eñes correctamente.

Los avisos de resultado aparecen como un mensaje breve en la esquina de la ventana. Los errores
y las confirmaciones aparecen como diálogo, y hay que cerrarlos.

---

## 5. Administrador

M01 abre el **Panel de Administración General**, con cinco indicadores arriba y tres pestañas.

### Indicadores

Usuarios activos · Usuarios inactivos · Recargas pendientes · Productos por reponer (stock 10 o
menos) · **Billeteras descuadradas**. Se recalculan tras cada operación.

> **El indicador de billeteras descuadradas debe estar siempre en 0.** Cuenta las billeteras cuyo
> saldo guardado no coincide con el que se obtiene al sumar sus recargas aprobadas y sus logros y
> restar sus movimientos. Cualquier valor distinto de cero es un problema de integridad: avise al
> responsable técnico antes de seguir operando.

### Pestaña 1 · Usuarios y Alumnos (Activos / Inactivos)

A la izquierda, el formulario **Datos de Usuario**; a la derecha, dos tablas: activos arriba e
inactivos abajo. Las dos muestran ID, nombre, correo, rol, grado/sección y estado.

**Dar de alta un usuario**

1. Rellene nombres, apellidos y correo. El correo se guarda en minúsculas y debe ser único.
2. Escriba la contraseña inicial. El campo trae `Temporal123*` como valor de partida: cámbielo.
3. Elija el rol. Al elegir **ESTUDIANTE** se habilitan Grado, Sección y Padre; con los demás
   roles esos tres campos se deshabilitan y se guardan vacíos.
4. En **Padre (Alumno)**, elija un padre de la lista o deje «Sin padre asignado». La lista solo
   contiene usuarios con rol PADRE que estén activos.
5. **Agregar Usuario**.

Al crear un estudiante, su billetera se crea con saldo 0,00 y 0 monedas.

Errores que puede ver: «Complete los nombres y apellidos del usuario.», «Ingrese un correo válido
de hasta 150 caracteres.», «Ingrese una contraseña inicial.», «Ya existe un usuario con ese
correo.».

**Editar un usuario**

Seleccione la fila y pulse **Editar Seleccionado** (en la tabla de activos o en la de inactivos).
El formulario se rellena, el botón pasa a **Guardar Cambios** y aparece **Cancelar**. El campo de
contraseña se deshabilita: **la edición no cambia la contraseña**.

Dos candados que la aplicación no deja saltar:

- No puede quitarse a sí mismo el rol de administrador.
- No puede darse de baja a sí mismo, ni dar de baja al último administrador activo. El mensaje es
  «Debe quedar al menos un administrador activo en el sistema.».

**Dar de baja y reactivar**

**Pasar a Inactivo** sobre un activo hace la baja lógica: el usuario desaparece de la tabla de
activos, aparece en la de inactivos y ya no puede iniciar sesión, pero no se borra ningún dato.
**Reactivar Seleccionado** sobre un inactivo lo devuelve. Nada se elimina de la base de datos.

### Pestaña 2 · Autorizar Recargas y Dar Monedas

A la izquierda, **Solicitudes de Recarga de Padres**: ID, fecha, padre, alumno, monto, método y
estado (PENDIENTE, APROBADA o RECHAZADA).

- **Autorizar Recarga** acredita el monto en la billetera del alumno y pasa la recarga a APROBADA.
- **Rechazar** la pasa a RECHAZADA y no toca ningún saldo.

Las dos acciones exigen una recarga **en estado PENDIENTE**; con cualquier otra fila el programa
avisa. Una recarga ya resuelta no se puede volver a aprobar ni rechazar.

A la derecha, **Recargar Monedas Especiales a Alumno**: elija alumno (solo estudiantes activos),
concepto, detalle y cantidad de monedas (de 5 en 5, entre 5 y 500), y pulse **Acreditar Monedas
Especiales**. Las monedas se registran como un logro: aparecen en el historial de logros del
alumno con usted como quien las otorgó.

### Pestaña 3 · Catálogo de Productos y Control de Stock

Formulario a la izquierda, tabla de productos activos a la derecha con una columna
**Estado / Aviso** que marca `OK`, `⚠ BAJO STOCK (Reponer)` con 10 unidades o menos, o
`AGOTADO - REPONER` con 0.

**Precio en soles y precio en monedas son independientes y opcionales**, pero hay que poner al
menos uno: así se decide cómo se adquiere el producto. Solo con precio en soles, se compra; solo
con precio en monedas, se canjea; con los dos, el estudiante elige.

| Botón | Qué hace |
|---|---|
| **Agregar al Catálogo** / **Actualizar Producto** | Crea o guarda el producto del formulario. |
| **Editar Producto** | Carga la fila seleccionada en el formulario. |
| **Descontar Stock (-1)** | Baja una unidad. Avisa si queda en 10 o menos. Es un atajo de prueba, no una venta: no genera movimiento ni cobra nada. |
| **Reponer Stock (+10)** | Sube diez unidades. |
| **Dar de Baja** | Desactiva el producto. Deja de aparecer en la tienda y en esta tabla, pero no se borra y sus movimientos pasados se conservan. |

---

## 6. Padre o madre

M01 abre **Supervisión Familiar y Recargas**, con cuatro cuadros:

1. **Mis Hijos Registrados.** Sus hijos vinculados y activos, con grado, sección, saldo en soles
   y monedas. Al seleccionar uno, los cuadros 2 y 4 se actualizan.
2. **Detalle de Saldos del Hijo Seleccionado.** Nombre, saldo en soles y monedas por logros.
3. **Solicitar Recarga a Administración.** Elija hijo, escriba el monto y elija el método de pago
   (EFECTIVO, TRANSFERENCIA, YAPE o PLIN) y pulse **Enviar Recarga al Hijo**.
4. **Historial de Compras.** Lo que el hijo seleccionado ha comprado o canjeado: fecha, producto,
   tipo (DINERO o MONEDAS), cantidad e importe.

**La recarga no acredita saldo de inmediato.** Queda en estado PENDIENTE hasta que un
administrador la autorice; el mensaje de confirmación lo dice. Si la rechaza, el saldo no cambia.

El monto debe ser un número positivo con dos decimales como máximo. Si escribe texto, el programa
avisa: «Ingrese un monto numérico válido.».

**Solo ve a sus propios hijos.** El vínculo se comprueba en el servidor en cada consulta, no en la
pantalla: intentar consultar la billetera, los movimientos o los logros de un alumno que no es su
hijo devuelve «No tiene permiso para realizar esta operación.». Si la lista sale vacía, es que no
tiene hijos vinculados: pídaselo al administrador.

Captura: [mainframe-PADRE.png](../reports/T12/mainframe-PADRE.png)

---

## 7. Estudiante

M01 abre **Mi Billetera, Tienda y Logros**. Arriba, dos tarjetas: **Saldo para Gastos Escolares
(Soles)** y **Monedas Especiales de Recompensa**.

A la izquierda, **Tienda Escolar y Catálogo de Recompensas**, con el stock en vivo. Las columnas
de precio dicen «No aplica» cuando el producto no admite esa forma de pago.

**Comprar o canjear**

1. Seleccione el producto.
2. Elija la cantidad (1 a 10).
3. En **Pagar con**, elija `DINERO` para comprar con soles o `MONEDAS` para canjear.
4. **Comprar / Canjear** y confirme. El diálogo muestra el total.

Qué puede salir mal:

| Mensaje | Motivo |
|---|---|
| «'X' solo se puede canjear con monedas.» / «'X' solo se puede comprar con dinero.» | Eligió una forma de pago que ese producto no admite. |
| «Saldo insuficiente…» / «Monedas insuficientes…» | No alcanza. El mensaje dice cuánto cuesta y cuánto tiene. |
| «Solo quedan N unidades de 'X'.» | Pidió más de lo que hay en stock. |
| «El producto ya no está disponible.» | Lo dieron de baja mientras tenía la pantalla abierta. |

La operación es atómica: o se descuenta el saldo, se descuenta el stock y se registra el
movimiento, o no ocurre nada. Nunca queda a medias, y el saldo no puede quedar en negativo.

A la derecha, dos historiales: **¿En qué he ganado Monedas de Recompensa?** (fecha, mérito,
docente que lo otorgó y monedas) y **Mi Historial de Compras y Canjes** (fecha, producto, método,
cantidad y total).

Solo ve sus propios datos. No puede consultar la billetera ni el historial de otro estudiante.

Captura: [mainframe-ESTUDIANTE.png](../reports/T12/mainframe-ESTUDIANTE.png)

---

## 8. Docente

M01 abre **Panel Docente - Registro de Logros e Incentivos**, con tres zonas:

1. **Otorgar Monedas por Mérito** (formulario, a la izquierda).
2. **Estudiantes Activos**: todos los estudiantes activos del sistema, con ID, nombre, grado y
   sección.
3. **Logros que he registrado**: solo los suyos, con fecha, estudiante, tipo de mérito,
   descripción y monedas.

**Otorgar monedas**

1. Elija el estudiante.
2. Elija la categoría de mérito: Participación en clase, Puntualidad, Mejor promedio del
   bimestre, Convivencia ejemplar, Buen rendimiento académico o Buen comportamiento en aula.
3. Escriba el motivo.
4. Elija las monedas (de 5 en 5, entre 5 y 200).
5. **Otorgar Monedas por Mérito** y confirme.

Las monedas se acreditan en la billetera del estudiante en el momento, y el logro queda
registrado con su nombre: el estudiante y su padre lo ven en el historial.

**Lo que el rol docente no puede hacer:** consultar saldos, movimientos o historiales de los
estudiantes. Su panel no los muestra y el servidor rechaza la consulta aunque se intente por otra
vía. Solo registra logros y ve los que él mismo ha otorgado.

Captura: [mainframe-DOCENTE.png](../reports/T12/mainframe-DOCENTE.png)

---

## 9. Preguntas frecuentes

**Me quedé bloqueado.** Espere 15 minutos. El bloqueo se levanta solo y el contador vuelve a
cero; no hace falta que nadie lo desbloquee.

**Quiero cambiar mi contraseña.** Todavía no se puede desde la aplicación. Hable con el
administrador.

**La ventana se queda sin responder un momento.** Las consultas se ejecutan en el mismo hilo que
dibuja la pantalla, así que con la base de datos lenta la ventana se congela mientras carga.
Espere: no se ha cerrado. Defecto conocido D-T11-02.

**Hice clic en M03, M05 o cualquier otro módulo y solo veo un texto.** Es correcto: esas
pantallas son prototipos de esta etapa. La función real está en M01.

**La hora que veo no es la mía.** Ver el aviso de la sección 3.

**¿Se borra algo cuando dan de baja a un usuario o a un producto?** No. La baja es lógica: el
registro se marca como inactivo y se puede reactivar. El historial se conserva siempre.
