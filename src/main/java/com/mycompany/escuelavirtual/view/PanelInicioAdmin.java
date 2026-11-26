/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.CatalogoController;
import com.mycompany.escuelavirtual.controller.LogroController;
import com.mycompany.escuelavirtual.controller.RecargaController;
import com.mycompany.escuelavirtual.controller.ReporteController;
import com.mycompany.escuelavirtual.controller.UsuarioController;
import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.DetalleRecarga;
import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.EstadoRecarga;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelTablaBase;
import com.mycompany.escuelavirtual.util.ui.Toast;
import com.mycompany.escuelavirtual.view.BuscadorCombo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class PanelInicioAdmin extends JPanel {

    private static final long serialVersionUID = 1L;

    private record ItemComboUsuario(Integer id, String etiqueta) {

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final UsuarioController usuarioController;
    private final CatalogoController catalogoController;
    private final RecargaController recargaController;
    private final LogroController logroController;
    private final ReporteController reporteController;

    private List<Usuario> usuarios = new ArrayList<>();
    private List<DetalleRecarga> recargas = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();

    private final JLabel lblKpiActivos = Estilos.titulo("0");
    private final JLabel lblKpiInactivos = Estilos.titulo("0");
    private final JLabel lblKpiRecargas = Estilos.titulo("0");
    private final JLabel lblKpiStockBajo = Estilos.titulo("0");
    private final JLabel lblKpiDescuadres = Estilos.titulo("0");

    private PanelTablaBase<Usuario> tablaActivos;
    private PanelTablaBase<Usuario> tablaInactivos;
    private PanelTablaBase<DetalleRecarga> tablaRecargas;
    private PanelTablaBase<Producto> tablaProductos;
    private PanelTablaBase<DetalleLogro> tablaLogros;

    private JComboBox<ItemComboUsuario> cboAlumnoMonedas;
    private final JComboBox<ItemComboUsuario> cboPadreU = new JComboBox<>();

    private final BuscadorCombo<ItemComboUsuario> buscadorPadreU;
    private BuscadorCombo<ItemComboUsuario> buscadorAlumnoMonedas;

    private Usuario usuarioEnEdicion = null;
    private Producto productoEnEdicion = null;

    // Controles de formulario de Usuario
    private final JTextField txtNombresU = new JTextField(14);
    private final JTextField txtApellidosU = new JTextField(14);
    private final JTextField txtCorreoU = new JTextField(14);
    private final JPasswordField txtClaveU = new JPasswordField("Temporal123*", 14);
    private final JComboBox<Rol> cboRolU = new JComboBox<>(Rol.values());
    private final JTextField txtGradoU = new JTextField("5.º", 6);
    private final JTextField txtSeccionU = new JTextField("A", 4);
    private final JButton btnGuardarUsuario = Estilos.botonPrimario("Agregar Usuario");
    private final JButton btnCancelarUsuario = Estilos.botonSecundario("Cancelar");

    // Controles de formulario de Producto
    private final JComboBox<String> cboCategoriaP = new JComboBox<>(new String[]{
        "Alimentos y Bebidas", "Útiles Escolares", "Uniformes y Accesorios", "Recompensas", "Otros"
    });
    private final JTextField txtNombreP = new JTextField(14);
    private final JTextField txtDescP = new JTextField(14);
    private final JTextField txtPrecioSolesP = new JTextField("5.00", 8);
    private final JTextField txtPrecioMonedasP = new JTextField("40", 8);
    private final JSpinner spnStockP = new JSpinner(new SpinnerNumberModel(25, 0, 500, 5));
    private final JButton btnGuardarProducto = Estilos.botonPrimario("Agregar al Catálogo");
    private final JButton btnCancelarProducto = Estilos.botonSecundario("Cancelar");

    public PanelInicioAdmin(Usuario adminActual) {
        this.usuarioController = new UsuarioController(adminActual);
        this.catalogoController = new CatalogoController(adminActual);
        this.recargaController = new RecargaController(adminActual);
        this.logroController = new LogroController(adminActual);
        this.reporteController = new ReporteController(adminActual);

        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        JPanel norte = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        norte.setOpaque(false);

        JPanel cabeceraTitulo = new JPanel(new BorderLayout());
        cabeceraTitulo.setOpaque(false);
        cabeceraTitulo.add(Estilos.titulo("Panel de Administración General (Conectado a SQL Server)"), BorderLayout.WEST);
        JButton btnRefrescar = Estilos.botonSecundario("↻ Actualizar Datos");
        btnRefrescar.addActionListener(e -> {
            recargarDesdeBD();
            Toast.info(this, "Datos actualizados desde SQL Server.");
        });
        cabeceraTitulo.add(btnRefrescar, BorderLayout.EAST);
        norte.add(cabeceraTitulo, BorderLayout.NORTH);

        norte.add(crearBarraIndicadores(), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilos.FUENTE_NEGRITA);
        pestanas.addTab("1. Usuarios y Alumnos (Activos / Inactivos)", crearSeccionUsuarios());
        pestanas.addTab("2. Autorizar Recargas y Dar Monedas", crearSeccionRecargasYMonedas());
        pestanas.addTab("3. Catálogo de Productos y Control de Stock", crearSeccionProductos());

        add(pestanas, BorderLayout.CENTER);

        buscadorPadreU = new BuscadorCombo<>(cboPadreU);
        recargarDesdeBD();
    }

    private JPanel crearBarraIndicadores() {
        JPanel barra = new JPanel(new GridLayout(1, 5, Estilos.ESPACIO, 0));
        barra.setOpaque(false);
        barra.add(crearMiniTarjeta("Usuarios Activos", lblKpiActivos));
        barra.add(crearMiniTarjeta("Usuarios Inactivos", lblKpiInactivos));
        barra.add(crearMiniTarjeta("Recargas Pendientes", lblKpiRecargas));
        barra.add(crearMiniTarjeta("Productos por Reponer", lblKpiStockBajo));
        barra.add(crearMiniTarjeta("Billeteras Descuadradas", lblKpiDescuadres));
        return barra;
    }

    private JPanel crearMiniTarjeta(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Estilos.FONDO_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(Estilos.ESPACIO, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO, Estilos.ESPACIO_GRANDE)));
        p.add(Estilos.subtitulo(titulo), BorderLayout.WEST);
        p.add(valor, BorderLayout.EAST);
        return p;
    }

    private JPanel crearSeccionUsuarios() {
        JPanel contenedor = new JPanel(new BorderLayout(Estilos.ESPACIO_GRANDE, 0));
        contenedor.setBackground(Estilos.FONDO);
        contenedor.setBorder(Estilos.margen());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilos.FONDO_PANEL);
        form.setBorder(Estilos.tarjeta());

        btnCancelarUsuario.setVisible(false);
        btnCancelarUsuario.addActionListener(e -> limpiarFormularioUsuario());

        cboRolU.addActionListener(e -> {
            boolean esAlumno = cboRolU.getSelectedItem() == Rol.ESTUDIANTE;
            txtGradoU.setEnabled(esAlumno);
            txtSeccionU.setEnabled(esAlumno);
            cboPadreU.setEnabled(esAlumno);
        });

        btnGuardarUsuario.addActionListener(e -> {
            Rol rol = (Rol) cboRolU.getSelectedItem();
            ItemComboUsuario padreSel = buscadorPadreU.getSeleccionado();
            Integer idPadre = (rol == Rol.ESTUDIANTE && padreSel != null) ? padreSel.id() : null;

            try {
                if (usuarioEnEdicion == null) {
                    String clave = new String(txtClaveU.getPassword());
                    usuarioController.registrar(
                            txtNombresU.getText(),
                            txtApellidosU.getText(),
                            txtCorreoU.getText(),
                            clave,
                            rol,
                            txtGradoU.getText(),
                            txtSeccionU.getText(),
                            idPadre
                    );
                    Toast.exito(this, "Usuario guardado en SQL Server.");
                } else {
                    usuarioEnEdicion.setNombres(txtNombresU.getText().trim());
                    usuarioEnEdicion.setApellidos(txtApellidosU.getText().trim());
                    usuarioEnEdicion.setCorreo(txtCorreoU.getText().trim().toLowerCase());
                    usuarioEnEdicion.setRol(rol);
                    usuarioEnEdicion.setGrado(rol == Rol.ESTUDIANTE ? txtGradoU.getText().trim() : null);
                    usuarioEnEdicion.setSeccion(rol == Rol.ESTUDIANTE ? txtSeccionU.getText().trim() : null);
                    usuarioEnEdicion.setIdPadre(idPadre);
                    usuarioController.actualizar(usuarioEnEdicion);
                    Toast.exito(this, "Usuario actualizado en SQL Server.");
                }
                limpiarFormularioUsuario();
                recargarDesdeBD();
            } catch (NegocioException ex) {
                DialogoConfirmacion.error(this, ex.getMessage());
            }
        });

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, Estilos.ESPACIO_CHICO, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnGuardarUsuario);
        panelBotones.add(btnCancelarUsuario);

        int f = 0;
        agregarFilaForm(form, f++, "Datos de Usuario", null, true);
        agregarFilaForm(form, f++, "Nombres:", txtNombresU, false);
        agregarFilaForm(form, f++, "Apellidos:", txtApellidosU, false);
        agregarFilaForm(form, f++, "Correo:", txtCorreoU, false);
        agregarFilaForm(form, f++, "Contraseña:", txtClaveU, false);
        agregarFilaForm(form, f++, "Rol:", cboRolU, false);
        agregarFilaForm(form, f++, "Grado (Alumno):", txtGradoU, false);
        agregarFilaForm(form, f++, "Sección (Alumno):", txtSeccionU, false);
        agregarFilaForm(form, f++, "Padre (Alumno):", cboPadreU, false);
        agregarFilaForm(form, f, "", panelBotones, false);

        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, Estilos.ESPACIO));
        derecha.setOpaque(false);

        JPanel cuadroActivos = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroActivos.setBackground(Estilos.FONDO_PANEL);
        cuadroActivos.setBorder(Estilos.tarjeta());

        JPanel cabActivos = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        cabActivos.setOpaque(false);
        JButton btnEditarActivo = Estilos.botonSecundario("Editar Seleccionado");
        JButton btnDesactivar = Estilos.botonSecundario("Pasar a Inactivo");
        cabActivos.add(btnEditarActivo);
        cabActivos.add(btnDesactivar);

        JPanel topActivos = new JPanel(new BorderLayout());
        topActivos.setOpaque(false);
        topActivos.add(Estilos.subtitulo("USUARIOS Y ALUMNOS ACTIVOS"), BorderLayout.WEST);
        topActivos.add(cabActivos, BorderLayout.EAST);

        btnEditarActivo.addActionListener(e -> cargarUsuarioEnFormulario(tablaActivos.getSeleccionado()));
        btnDesactivar.addActionListener(e -> {
            Usuario sel = tablaActivos.getSeleccionado();
            if (sel == null) {
                DialogoConfirmacion.error(this, "Seleccione un usuario activo de la tabla superior.");
                return;
            }
            try {
                usuarioController.cambiarEstado(sel.getIdUsuario(), false);
                recargarDesdeBD();
                Toast.info(this, sel.getNombres() + " pasó a estado INACTIVO en BD.");
            } catch (NegocioException ex) {
                DialogoConfirmacion.error(this, ex.getMessage());
            }
        });

        tablaActivos = new PanelTablaBase<>(
                new String[]{"ID", "Nombres y Apellidos", "Correo", "Rol", "Grado/Sec.", "Estado"},
                u -> new Object[]{
                    u.getIdUsuario(),
                    u.getNombreCompleto(),
                    u.getCorreo(),
                    u.getRol(),
                    (u.getGrado() == null ? "-" : u.getGrado()) + " " + (u.getSeccion() == null ? "-" : u.getSeccion()),
                    "ACTIVO"
                }
        );
        tablaActivos.ocultarFiltroFecha();
        cuadroActivos.add(topActivos, BorderLayout.NORTH);
        cuadroActivos.add(tablaActivos, BorderLayout.CENTER);

        JPanel cuadroInactivos = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroInactivos.setBackground(Estilos.FONDO_PANEL);
        cuadroInactivos.setBorder(Estilos.tarjeta());

        JPanel cabInactivos = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        cabInactivos.setOpaque(false);
        JButton btnEditarInactivo = Estilos.botonSecundario("Editar Seleccionado");
        JButton btnReactivar = Estilos.botonPrimario("Reactivar Seleccionado");
        cabInactivos.add(btnEditarInactivo);
        cabInactivos.add(btnReactivar);

        JPanel topInactivos = new JPanel(new BorderLayout());
        topInactivos.setOpaque(false);
        topInactivos.add(Estilos.subtitulo("USUARIOS Y ALUMNOS INACTIVOS"), BorderLayout.WEST);
        topInactivos.add(cabInactivos, BorderLayout.EAST);

        btnEditarInactivo.addActionListener(e -> cargarUsuarioEnFormulario(tablaInactivos.getSeleccionado()));
        btnReactivar.addActionListener(e -> {
            Usuario sel = tablaInactivos.getSeleccionado();
            if (sel == null) {
                DialogoConfirmacion.error(this, "Seleccione un usuario inactivo de la tabla inferior.");
                return;
            }
            try {
                usuarioController.cambiarEstado(sel.getIdUsuario(), true);
                recargarDesdeBD();
                Toast.exito(this, sel.getNombres() + " fue REACTIVADO en BD.");
            } catch (NegocioException ex) {
                DialogoConfirmacion.error(this, ex.getMessage());
            }
        });

        tablaInactivos = new PanelTablaBase<>(
                new String[]{"ID", "Nombres y Apellidos", "Correo", "Rol", "Grado/Sec.", "Estado"},
                u -> new Object[]{
                    u.getIdUsuario(),
                    u.getNombreCompleto(),
                    u.getCorreo(),
                    u.getRol(),
                    (u.getGrado() == null ? "-" : u.getGrado()) + " " + (u.getSeccion() == null ? "-" : u.getSeccion()),
                    "INACTIVO"
                }
        );
        tablaInactivos.ocultarFiltroFecha();
        cuadroInactivos.add(topInactivos, BorderLayout.NORTH);
        cuadroInactivos.add(tablaInactivos, BorderLayout.CENTER);

        derecha.add(cuadroActivos);
        derecha.add(cuadroInactivos);
        contenedor.add(form, BorderLayout.WEST);
        contenedor.add(derecha, BorderLayout.CENTER);
        return contenedor;
    }

    private void cargarUsuarioEnFormulario(Usuario sel) {
        if (sel == null) {
            DialogoConfirmacion.error(this, "Seleccione un usuario de la tabla para editar.");
            return;
        }
        usuarioEnEdicion = sel;
        txtNombresU.setText(sel.getNombres());
        txtApellidosU.setText(sel.getApellidos());
        txtCorreoU.setText(sel.getCorreo());
        cboRolU.setSelectedItem(sel.getRol());
        txtGradoU.setText(sel.getGrado() == null ? "" : sel.getGrado());
        txtSeccionU.setText(sel.getSeccion() == null ? "" : sel.getSeccion());

        for (int i = 0; i < cboPadreU.getItemCount(); i++) {
            ItemComboUsuario item = cboPadreU.getItemAt(i);
            if ((sel.getIdPadre() == null && item.id() == null)
                    || (sel.getIdPadre() != null && sel.getIdPadre().equals(item.id()))) {
                cboPadreU.setSelectedIndex(i);
                break;
            }
        }

        btnGuardarUsuario.setText("Guardar Cambios");
        btnCancelarUsuario.setVisible(true);
        txtClaveU.setEnabled(false);
    }

    private void limpiarFormularioUsuario() {
        usuarioEnEdicion = null;
        txtNombresU.setText("");
        txtApellidosU.setText("");
        txtCorreoU.setText("");
        txtGradoU.setText("5.º");
        txtSeccionU.setText("A");
        txtClaveU.setEnabled(true);
        btnGuardarUsuario.setText("Agregar Usuario");
        btnCancelarUsuario.setVisible(false);
    }

    private JPanel crearSeccionRecargasYMonedas() {
        JPanel contenedor = new JPanel(new GridLayout(1, 2, Estilos.ESPACIO_GRANDE, 0));
        contenedor.setBackground(Estilos.FONDO);
        contenedor.setBorder(Estilos.margen());

        JPanel cuadroRecargas = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        cuadroRecargas.setBackground(Estilos.FONDO_PANEL);
        cuadroRecargas.setBorder(Estilos.tarjeta());
        cuadroRecargas.add(Estilos.subtitulo("Solicitudes de Recarga de Padres (En vivo desde BD)"), BorderLayout.NORTH);

        tablaRecargas = new PanelTablaBase<>(
                new String[]{"ID", "Fecha", "Padre", "Alumno", "Monto", "Método", "Estado"},
                r -> new Object[]{
                    r.idRecarga(),
                    Formato.fechaHora(r.fecha()),
                    r.nombrePadre(),
                    r.nombreEstudiante(),
                    Formato.dinero(r.monto()),
                    r.metodo(),
                    r.estado()
                }
        );
        tablaRecargas.ocultarFiltroFecha();
        cuadroRecargas.add(tablaRecargas, BorderLayout.CENTER);

        JPanel botonesRecarga = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        botonesRecarga.setOpaque(false);
        JButton btnAprobar = Estilos.botonPrimario("Autorizar Recarga");
        JButton btnRechazar = Estilos.botonSecundario("Rechazar");

        btnAprobar.addActionListener(e -> {
            DetalleRecarga sel = tablaRecargas.getSeleccionado();
            if (sel == null || sel.estado() != EstadoRecarga.PENDIENTE) {
                DialogoConfirmacion.error(this, "Seleccione una recarga en estado PENDIENTE.");
                return;
            }
            if (DialogoConfirmacion.confirmar(this, "Autorizar Recarga",
                    "¿Aprobar recarga de " + Formato.dinero(sel.monto()) + " para " + sel.nombreEstudiante() + "?")) {
                try {
                    recargaController.aprobar(sel.idRecarga());
                    recargarDesdeBD();
                    Toast.exito(this, "Recarga aprobada y acreditada en la billetera del alumno.");
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
            }
        });

        btnRechazar.addActionListener(e -> {
            DetalleRecarga sel = tablaRecargas.getSeleccionado();
            if (sel == null || sel.estado() != EstadoRecarga.PENDIENTE) {
                DialogoConfirmacion.error(this, "Seleccione una recarga en estado PENDIENTE.");
                return;
            }
            try {
                recargaController.rechazar(sel.idRecarga(), "Rechazada por el administrador");
                recargarDesdeBD();
                Toast.info(this, "Recarga rechazada: el saldo no cambia.");
            } catch (NegocioException ex) {
                DialogoConfirmacion.error(this, ex.getMessage());
            }
        });

        botonesRecarga.add(btnRechazar);
        botonesRecarga.add(btnAprobar);
        cuadroRecargas.add(botonesRecarga, BorderLayout.SOUTH);

        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, Estilos.ESPACIO_GRANDE));
        derecha.setOpaque(false);

        JPanel cuadroMonedas = new JPanel(new GridBagLayout());
        cuadroMonedas.setBackground(Estilos.FONDO_PANEL);
        cuadroMonedas.setBorder(Estilos.tarjeta());

        cboAlumnoMonedas = new JComboBox<>();
        buscadorAlumnoMonedas = new BuscadorCombo<>(cboAlumnoMonedas);

        JComboBox<String> cboMotivo = new JComboBox<>(new String[]{
            "Participación destacada", "Puntualidad mensual", "Mejor promedio", "Convivencia ejemplar"
        });
        JTextField txtDetalle = new JTextField("Otorgado por administración", 16);
        JSpinner spnMonedas = new JSpinner(new SpinnerNumberModel(20, 5, 500, 5));

        int f = 0;
        agregarFilaForm(cuadroMonedas, f++, "Recargar Monedas Especiales a Alumno", null, true);
        agregarFilaForm(cuadroMonedas, f++, "Seleccionar Alumno:", cboAlumnoMonedas, false);
        agregarFilaForm(cuadroMonedas, f++, "Concepto / Logro:", cboMotivo, false);
        agregarFilaForm(cuadroMonedas, f++, "Detalle:", txtDetalle, false);
        agregarFilaForm(cuadroMonedas, f++, "Cantidad Monedas:", spnMonedas, false);

        JButton btnDarMonedas = Estilos.botonPrimario("Acreditar Monedas Especiales");
        btnDarMonedas.addActionListener(e -> {
            ItemComboUsuario alumno = buscadorAlumnoMonedas.getSeleccionado();
            int cant = (Integer) spnMonedas.getValue();
            if (alumno == null || alumno.id() == null) {
                return;
            }
            if (DialogoConfirmacion.confirmar(this, "Confirmar Monedas Especiales",
                    "¿Acreditar " + cant + " monedas a " + alumno.etiqueta() + "?")) {
                try {
                    logroController.registrar(
                            alumno.id(),
                            (String) cboMotivo.getSelectedItem(),
                            txtDetalle.getText(),
                            cant
                    );
                    recargarDesdeBD();
                    Toast.exito(this, "Se acreditaron +" + cant + " monedas a " + alumno.etiqueta());
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
            }
        });
        agregarFilaForm(cuadroMonedas, f, "", btnDarMonedas, false);

        JPanel cuadroHistorialLogros = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroHistorialLogros.setBackground(Estilos.FONDO_PANEL);
        cuadroHistorialLogros.setBorder(Estilos.tarjeta());
        cuadroHistorialLogros.add(Estilos.subtitulo("Historial General de Logros (BD)"), BorderLayout.NORTH);

        tablaLogros = new PanelTablaBase<>(
                new String[]{"Fecha", "Alumno", "Autor", "Logro", "Monedas"},
                l -> new Object[]{
                    Formato.fechaHora(l.fecha()),
                    l.nombreEstudiante(),
                    l.nombreDocente(),
                    l.tipo(),
                    "+" + Formato.monedas(l.monedas())
                }
        );
        tablaLogros.ocultarFiltroFecha();
        cuadroHistorialLogros.add(tablaLogros, BorderLayout.CENTER);

        derecha.add(cuadroMonedas);
        derecha.add(cuadroHistorialLogros);

        contenedor.add(cuadroRecargas);
        contenedor.add(derecha);
        return contenedor;
    }

    private JPanel crearSeccionProductos() {
        JPanel contenedor = new JPanel(new BorderLayout(Estilos.ESPACIO_GRANDE, 0));
        contenedor.setBackground(Estilos.FONDO);
        contenedor.setBorder(Estilos.margen());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilos.FONDO_PANEL);
        form.setBorder(Estilos.tarjeta());

        btnCancelarProducto.setVisible(false);
        btnCancelarProducto.addActionListener(e -> limpiarFormularioProducto());

        btnGuardarProducto.addActionListener(e -> {
            BigDecimal pDinero = null;
            Integer pMonedas = null;
            try {
                if (!txtPrecioSolesP.getText().isBlank()) {
                    pDinero = new BigDecimal(txtPrecioSolesP.getText().trim());
                }
                if (!txtPrecioMonedasP.getText().isBlank()) {
                    pMonedas = Integer.valueOf(txtPrecioMonedasP.getText().trim());
                }
            } catch (NumberFormatException ex) {
                DialogoConfirmacion.error(this, "Verifique los importes en Soles o Monedas.");
                return;
            }
            try {
                if (productoEnEdicion == null) {
                    Producto nuevo = new Producto();
                    nuevo.setCategoria((String) cboCategoriaP.getSelectedItem());
                    nuevo.setNombre(txtNombreP.getText().trim());
                    nuevo.setDescripcion(txtDescP.getText().trim());
                    nuevo.setPrecioDinero(pDinero);
                    nuevo.setPrecioMonedas(pMonedas);
                    nuevo.setStock((Integer) spnStockP.getValue());
                    nuevo.setActivo(true);
                    catalogoController.registrar(nuevo);
                    Toast.exito(this, "Producto agregado a SQL Server.");
                } else {
                    productoEnEdicion.setCategoria((String) cboCategoriaP.getSelectedItem());
                    productoEnEdicion.setNombre(txtNombreP.getText().trim());
                    productoEnEdicion.setDescripcion(txtDescP.getText().trim());
                    productoEnEdicion.setPrecioDinero(pDinero);
                    productoEnEdicion.setPrecioMonedas(pMonedas);
                    productoEnEdicion.setStock((Integer) spnStockP.getValue());
                    catalogoController.actualizar(productoEnEdicion);
                    Toast.exito(this, "Producto actualizado en SQL Server.");
                }
                limpiarFormularioProducto();
                recargarDesdeBD();
            } catch (NegocioException ex) {
                DialogoConfirmacion.error(this, ex.getMessage());
            }
        });

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, Estilos.ESPACIO_CHICO, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnGuardarProducto);
        panelBotones.add(btnCancelarProducto);

        int f = 0;
        agregarFilaForm(form, f++, "Datos de Producto", null, true);
        agregarFilaForm(form, f++, "Categoría:", cboCategoriaP, false);
        agregarFilaForm(form, f++, "Nombre:", txtNombreP, false);
        agregarFilaForm(form, f++, "Descripción:", txtDescP, false);
        agregarFilaForm(form, f++, "Precio S/ (opcional):", txtPrecioSolesP, false);
        agregarFilaForm(form, f++, "Precio Monedas (opc.):", txtPrecioMonedasP, false);
        agregarFilaForm(form, f++, "Stock Actual:", spnStockP, false);
        agregarFilaForm(form, f, "", panelBotones, false);

        JPanel cuadroTabla = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        cuadroTabla.setBackground(Estilos.FONDO_PANEL);
        cuadroTabla.setBorder(Estilos.tarjeta());

        tablaProductos = new PanelTablaBase<>(
                new String[]{"ID", "Categoría", "Producto", "Descripción", "Precio S/", "Precio Monedas", "Stock", "Estado / Aviso"},
                p -> new Object[]{
                    p.getIdProducto(),
                    p.getCategoria(),
                    p.getNombre(),
                    p.getDescripcion() == null ? "" : p.getDescripcion(),
                    p.getPrecioDinero() == null ? "-" : Formato.dinero(p.getPrecioDinero()),
                    p.getPrecioMonedas() == null ? "-" : Formato.monedas(p.getPrecioMonedas()),
                    p.getStock(),
                    alertaStock(p)
                }
        );
        tablaProductos.ocultarFiltroFecha();
        cuadroTabla.add(tablaProductos, BorderLayout.CENTER);

        JPanel accionesProd = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        accionesProd.setOpaque(false);

        JButton btnEditar = Estilos.botonSecundario("Editar Producto");
        JButton btnSimularCompra = Estilos.botonSecundario("Descontar Stock (-1)");
        JButton btnReponer = Estilos.botonPrimario("Reponer Stock (+10)");
        JButton btnEliminar = Estilos.botonSecundario("Dar de Baja");

        btnEditar.addActionListener(e -> cargarProductoEnFormulario(tablaProductos.getSeleccionado()));

        btnSimularCompra.addActionListener(e -> {
            Producto sel = tablaProductos.getSeleccionado();
            if (sel != null && sel.getStock() > 0) {
                try {
                    int nuevoStock = sel.getStock() - 1;
                    catalogoController.ajustarStock(sel.getIdProducto(), nuevoStock);
                    recargarDesdeBD();
                    if (nuevoStock <= 10) {
                        Toast.error(this, "⚠ Aviso: Stock bajo de '" + sel.getNombre() + "'.");
                    }
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
            }
        });

        btnReponer.addActionListener(e -> {
            Producto sel = tablaProductos.getSeleccionado();
            if (sel != null) {
                try {
                    catalogoController.ajustarStock(sel.getIdProducto(), sel.getStock() + 10);
                    recargarDesdeBD();
                    Toast.exito(this, "Repuestos +10 de " + sel.getNombre() + ".");
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            Producto sel = tablaProductos.getSeleccionado();
            if (sel != null && DialogoConfirmacion.confirmar(this, "Dar de Baja", "¿Dar de baja '" + sel.getNombre() + "'?")) {
                try {
                    catalogoController.darDeBaja(sel.getIdProducto());
                    recargarDesdeBD();
                    Toast.info(this, "Producto dado de baja.");
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
            }
        });

        accionesProd.add(btnEditar);
        accionesProd.add(btnSimularCompra);
        accionesProd.add(btnReponer);
        accionesProd.add(btnEliminar);
        cuadroTabla.add(accionesProd, BorderLayout.SOUTH);

        contenedor.add(form, BorderLayout.WEST);
        contenedor.add(cuadroTabla, BorderLayout.CENTER);
        return contenedor;
    }

    private String alertaStock(Producto p) {
        if (!p.isActivo()) {
            return "INACTIVO";
        }
        if (p.getStock() == 0) {
            return "AGOTADO - REPONER";
        }
        if (p.getStock() <= 10) {
            return "⚠ BAJO STOCK (Reponer)";
        }
        return "OK";
    }

    private void cargarProductoEnFormulario(Producto sel) {
        if (sel == null) {
            DialogoConfirmacion.error(this, "Seleccione un producto para editar.");
            return;
        }
        productoEnEdicion = sel;
        cboCategoriaP.setSelectedItem(sel.getCategoria());
        txtNombreP.setText(sel.getNombre());
        txtDescP.setText(sel.getDescripcion() == null ? "" : sel.getDescripcion());
        txtPrecioSolesP.setText(sel.getPrecioDinero() != null ? sel.getPrecioDinero().toString() : "");
        txtPrecioMonedasP.setText(sel.getPrecioMonedas() != null ? sel.getPrecioMonedas().toString() : "");
        spnStockP.setValue(sel.getStock());

        btnGuardarProducto.setText("Actualizar Producto");
        btnCancelarProducto.setVisible(true);
    }

    private void limpiarFormularioProducto() {
        productoEnEdicion = null;
        cboCategoriaP.setSelectedIndex(0);
        txtNombreP.setText("");
        txtDescP.setText("");
        txtPrecioSolesP.setText("");
        txtPrecioMonedasP.setText("");
        spnStockP.setValue(25);
        btnGuardarProducto.setText("Agregar al Catálogo");
        btnCancelarProducto.setVisible(false);
    }

    private void agregarFilaForm(JPanel panel, int fila, String etiqueta, JComponent campo, boolean esTitulo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO);
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (esTitulo) {
            c.gridx = 0;
            c.gridy = fila;
            c.gridwidth = 2;
            panel.add(Estilos.subtitulo(etiqueta), c);
            return;
        }
        c.gridx = 0;
        c.gridy = fila;
        panel.add(Estilos.etiqueta(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(campo, c);
    }

    private void recargarDesdeBD() {
        int descuadres = 0;
        try {
            usuarios = usuarioController.listarTodos();
            recargas = recargaController.listarTodas();
            productos = catalogoController.listarTodos();
            descuadres = reporteController.descuadres().size();
        } catch (NegocioException e) {
            DialogoConfirmacion.error(this, e.getMessage());
        }

        List<Usuario> listaActivos = usuarios.stream().filter(Usuario::isEstado).toList();
        List<Usuario> listaInactivos = usuarios.stream().filter(u -> !u.isEstado()).toList();
        tablaActivos.setDatos(listaActivos);
        tablaInactivos.setDatos(listaInactivos);
        tablaRecargas.setDatos(recargas);
        tablaProductos.setDatos(productos.stream().filter(Producto::isActivo).toList());
        tablaLogros.setDatos(logroController.listarTodos());

        List<ItemComboUsuario> listaAlumnosCombo = new ArrayList<>();
        List<ItemComboUsuario> listaPadresCombo = new ArrayList<>();
        listaPadresCombo.add(new ItemComboUsuario(null, "Sin padre asignado"));

        for (Usuario u : listaActivos) {
            if (u.getRol() == Rol.ESTUDIANTE) {
                String gradoSec = (u.getGrado() == null ? "-" : u.getGrado()) + " " + (u.getSeccion() == null ? "-" : u.getSeccion());
                listaAlumnosCombo.add(new ItemComboUsuario(u.getIdUsuario(), u.getNombreCompleto() + " (" + gradoSec + ")"));
            } else if (u.getRol() == Rol.PADRE) {
                listaPadresCombo.add(new ItemComboUsuario(u.getIdUsuario(), u.getNombreCompleto()));
            }
        }

        buscadorAlumnoMonedas.setElementos(listaAlumnosCombo);
        buscadorPadreU.setElementos(listaPadresCombo);

        lblKpiActivos.setText(String.valueOf(listaActivos.size()));
        lblKpiInactivos.setText(String.valueOf(listaInactivos.size()));
        lblKpiRecargas.setText(String.valueOf(recargas.stream().filter(r -> r.estado() == EstadoRecarga.PENDIENTE).count()));
        lblKpiStockBajo.setText(String.valueOf(productos.stream().filter(p -> p.isActivo() && p.getStock() <= 10).count()));
        lblKpiDescuadres.setText(String.valueOf(descuadres));
    }
}
