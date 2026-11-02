/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.UsuarioController;
import com.mycompany.escuelavirtual.dao.LogroDAO;
import com.mycompany.escuelavirtual.dao.ProductoDAO;
import com.mycompany.escuelavirtual.dao.RecargaDAO;
import com.mycompany.escuelavirtual.dao.RecargaDAO.DetalleRecarga;
import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.EstadoRecarga;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.Validador;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelTablaBase;
import com.mycompany.escuelavirtual.util.ui.Toast;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
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

    private final UsuarioController usuarioController = new UsuarioController();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final RecargaDAO recargaDAO = new RecargaDAO();
    private final LogroDAO logroDAO = new LogroDAO();

    private final int idAdminActual;

    private List<Usuario> usuarios = new ArrayList<>();
    private List<DetalleRecarga> recargas = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();

    private final JLabel lblKpiActivos = Estilos.titulo("0");
    private final JLabel lblKpiInactivos = Estilos.titulo("0");
    private final JLabel lblKpiRecargas = Estilos.titulo("0");
    private final JLabel lblKpiStockBajo = Estilos.titulo("0");

    private PanelTablaBase<Usuario> tablaActivos;
    private PanelTablaBase<Usuario> tablaInactivos;
    private PanelTablaBase<DetalleRecarga> tablaRecargas;
    private PanelTablaBase<Producto> tablaProductos;
    private JComboBox<ItemComboUsuario> cboAlumnoMonedas;
    private final JComboBox<ItemComboUsuario> cboPadreU = new JComboBox<>();

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
    private final JTextField txtNombreP = new JTextField(14);
    private final JTextField txtDescP = new JTextField(14);
    private final JTextField txtPrecioSolesP = new JTextField("5.00", 8);
    private final JTextField txtPrecioMonedasP = new JTextField("40", 8);
    private final JSpinner spnStockP = new JSpinner(new SpinnerNumberModel(25, 0, 500, 5));
    private final JButton btnGuardarProducto = Estilos.botonPrimario("Agregar al Catálogo");
    private final JButton btnCancelarProducto = Estilos.botonSecundario("Cancelar");

    public PanelInicioAdmin() {
        this(null);
    }

    public PanelInicioAdmin(Usuario adminActual) {
        this.idAdminActual = (adminActual != null && adminActual.getIdUsuario() != null) ? adminActual.getIdUsuario() : 1;

        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        JPanel norte = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Panel de Administración General (Conectado a SQL Server)"), BorderLayout.NORTH);
        norte.add(crearBarraIndicadores(), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilos.FUENTE_NEGRITA);
        pestanas.addTab("1. Usuarios y Alumnos (Activos / Inactivos)", crearSeccionUsuarios());
        pestanas.addTab("2. Autorizar Recargas y Dar Monedas", crearSeccionRecargasYMonedas());
        pestanas.addTab("3. Catálogo de Productos y Control de Stock", crearSeccionProductos());

        add(pestanas, BorderLayout.CENTER);
        recargarDesdeBD();
    }

    private JPanel crearBarraIndicadores() {
        JPanel barra = new JPanel(new GridLayout(1, 4, Estilos.ESPACIO, 0));
        barra.setOpaque(false);
        barra.add(crearMiniTarjeta("Usuarios Activos", lblKpiActivos));
        barra.add(crearMiniTarjeta("Usuarios Inactivos", lblKpiInactivos));
        barra.add(crearMiniTarjeta("Recargas Pendientes", lblKpiRecargas));
        barra.add(crearMiniTarjeta("Productos por Reponer", lblKpiStockBajo));
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
            if (!Validador.esTextoValido(txtNombresU.getText()) || !Validador.esTextoValido(txtApellidosU.getText())) {
                DialogoConfirmacion.error(this, "Complete los nombres y apellidos del usuario.");
                return;
            }
            if (!Validador.esCorreoValido(txtCorreoU.getText())) {
                DialogoConfirmacion.error(this, "Ingrese un correo institucional válido.");
                return;
            }
            Rol rol = (Rol) cboRolU.getSelectedItem();
            ItemComboUsuario padreSel = (ItemComboUsuario) cboPadreU.getSelectedItem();
            Integer idPadre = (rol == Rol.ESTUDIANTE && padreSel != null) ? padreSel.id() : null;

            try {
                if (usuarioEnEdicion == null) {
                    String clave = new String(txtClaveU.getPassword());
                    if (clave.isBlank()) {
                        DialogoConfirmacion.error(this, "Ingrese una contraseña inicial.");
                        return;
                    }
                    usuarioController.registrarUsuario(
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
                    usuarioController.actualizarUsuario(usuarioEnEdicion);
                    Toast.exito(this, "Usuario actualizado en SQL Server.");
                }
                limpiarFormularioUsuario();
                recargarDesdeBD();
            } catch (SQLException ex) {
                DialogoConfirmacion.error(this, "No se pudo guardar en BD: " + ex.getMessage());
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

        // Cuadro Activos
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
            } catch (SQLException ex) {
                DialogoConfirmacion.error(this, "Error al desactivar usuario: " + ex.getMessage());
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

        // Cuadro Inactivos
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
            } catch (SQLException ex) {
                DialogoConfirmacion.error(this, "Error al reactivar usuario: " + ex.getMessage());
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
                    recargaDAO.aprobarRecarga(sel.idRecarga(), idAdminActual);
                    recargarDesdeBD();
                    Toast.exito(this, "Recarga aprobada y acreditada en la billetera del alumno.");
                } catch (SQLException ex) {
                    DialogoConfirmacion.error(this, "Error al aprobar recarga: " + ex.getMessage());
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
                recargaDAO.rechazarRecarga(sel.idRecarga(), idAdminActual, "Rechazada por el administrador");
                recargarDesdeBD();
                Toast.info(this, "Recarga rechazada en BD.");
            } catch (SQLException ex) {
                DialogoConfirmacion.error(this, "Error al rechazar recarga: " + ex.getMessage());
            }
        });

        botonesRecarga.add(btnRechazar);
        botonesRecarga.add(btnAprobar);
        cuadroRecargas.add(botonesRecarga, BorderLayout.SOUTH);

        JPanel cuadroMonedas = new JPanel(new GridBagLayout());
        cuadroMonedas.setBackground(Estilos.FONDO_PANEL);
        cuadroMonedas.setBorder(Estilos.tarjeta());

        cboAlumnoMonedas = new JComboBox<>();
        JComboBox<String> cboMotivo = new JComboBox<>(new String[]{
            "Participación destacada", "Puntualidad mensual", "Mejor promedio", "Convivencia ejemplar", "Bono institucional"
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
            ItemComboUsuario alumno = (ItemComboUsuario) cboAlumnoMonedas.getSelectedItem();
            int cant = (Integer) spnMonedas.getValue();
            if (alumno == null || alumno.id() == null) {
                return;
            }
            if (DialogoConfirmacion.confirmar(this, "Confirmar Monedas Especiales",
                    "¿Acreditar " + cant + " monedas a " + alumno.etiqueta() + "?")) {
                try {
                    logroDAO.otorgarLogro(
                            alumno.id(),
                            idAdminActual,
                            (String) cboMotivo.getSelectedItem(),
                            txtDetalle.getText().trim(),
                            cant
                    );
                    recargarDesdeBD();
                    Toast.exito(this, "Se acreditaron +" + cant + " monedas en BD a " + alumno.etiqueta());
                } catch (SQLException ex) {
                    DialogoConfirmacion.error(this, "Error al acreditar monedas: " + ex.getMessage());
                }
            }
        });
        agregarFilaForm(cuadroMonedas, f, "", btnDarMonedas, false);

        contenedor.add(cuadroRecargas);
        contenedor.add(cuadroMonedas);
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
            if (!Validador.esTextoValido(txtNombreP.getText())) {
                DialogoConfirmacion.error(this, "El nombre del producto es obligatorio.");
                return;
            }
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
            if (pDinero == null && pMonedas == null) {
                DialogoConfirmacion.error(this, "Defina al menos un precio (Soles o Monedas).");
                return;
            }

            try {
                if (productoEnEdicion == null) {
                    Producto nuevo = new Producto();
                    nuevo.setNombre(txtNombreP.getText().trim());
                    nuevo.setDescripcion(txtDescP.getText().trim());
                    nuevo.setPrecioDinero(pDinero);
                    nuevo.setPrecioMonedas(pMonedas);
                    nuevo.setStock((Integer) spnStockP.getValue());
                    nuevo.setActivo(true);
                    productoDAO.insertar(nuevo);
                    Toast.exito(this, "Producto agregado a SQL Server.");
                } else {
                    productoEnEdicion.setNombre(txtNombreP.getText().trim());
                    productoEnEdicion.setDescripcion(txtDescP.getText().trim());
                    productoEnEdicion.setPrecioDinero(pDinero);
                    productoEnEdicion.setPrecioMonedas(pMonedas);
                    productoEnEdicion.setStock((Integer) spnStockP.getValue());
                    productoDAO.actualizar(productoEnEdicion);
                    Toast.exito(this, "Producto actualizado en SQL Server.");
                }
                limpiarFormularioProducto();
                recargarDesdeBD();
            } catch (SQLException ex) {
                DialogoConfirmacion.error(this, "Error al guardar producto: " + ex.getMessage());
            }
        });

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, Estilos.ESPACIO_CHICO, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnGuardarProducto);
        panelBotones.add(btnCancelarProducto);

        int f = 0;
        agregarFilaForm(form, f++, "Datos de Producto", null, true);
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
                new String[]{"ID", "Producto", "Descripción", "Precio S/", "Precio Monedas", "Stock", "Estado / Aviso"},
                p -> new Object[]{
                    p.getIdProducto(),
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
                    productoDAO.actualizarStock(sel.getIdProducto(), nuevoStock);
                    recargarDesdeBD();
                    if (nuevoStock <= 10) {
                        Toast.error(this, "⚠ Aviso: Stock bajo de '" + sel.getNombre() + "'.");
                    }
                } catch (SQLException ex) {
                    DialogoConfirmacion.error(this, "Error al descontar stock: " + ex.getMessage());
                }
            }
        });

        btnReponer.addActionListener(e -> {
            Producto sel = tablaProductos.getSeleccionado();
            if (sel != null) {
                try {
                    productoDAO.actualizarStock(sel.getIdProducto(), sel.getStock() + 10);
                    recargarDesdeBD();
                    Toast.exito(this, "Repuestos +10 de " + sel.getNombre() + " en BD.");
                } catch (SQLException ex) {
                    DialogoConfirmacion.error(this, "Error al reponer stock: " + ex.getMessage());
                }
            }
        });

        btnEliminar.addActionListener(e -> {
            Producto sel = tablaProductos.getSeleccionado();
            if (sel != null && DialogoConfirmacion.confirmar(this, "Dar de Baja", "¿Dar de baja '" + sel.getNombre() + "'?")) {
                try {
                    productoDAO.cambiarEstado(sel.getIdProducto(), false);
                    recargarDesdeBD();
                    Toast.info(this, "Producto dado de baja en BD.");
                } catch (SQLException ex) {
                    DialogoConfirmacion.error(this, "Error al dar de baja: " + ex.getMessage());
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
        usuarios = usuarioController.obtenerTodosLosUsuarios();
        try {
            recargas = recargaDAO.listarTodas();
        } catch (SQLException e) {
            System.err.println("Error al cargar recargas de BD: " + e.getMessage());
            recargas = new ArrayList<>();
        }
        try {
            productos = productoDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al cargar productos de BD: " + e.getMessage());
            productos = new ArrayList<>();
        }

        List<Usuario> listaActivos = usuarios.stream().filter(Usuario::isEstado).toList();
        List<Usuario> listaInactivos = usuarios.stream().filter(u -> !u.isEstado()).toList();
        tablaActivos.setDatos(listaActivos);
        tablaInactivos.setDatos(listaInactivos);
        tablaRecargas.setDatos(recargas);
        tablaProductos.setDatos(productos.stream().filter(Producto::isActivo).toList());

        cboAlumnoMonedas.removeAllItems();
        cboPadreU.removeAllItems();
        cboPadreU.addItem(new ItemComboUsuario(null, "Sin padre asignado"));

        for (Usuario u : listaActivos) {
            if (u.getRol() == Rol.ESTUDIANTE) {
                String gradoSec = (u.getGrado() == null ? "-" : u.getGrado()) + " " + (u.getSeccion() == null ? "-" : u.getSeccion());
                cboAlumnoMonedas.addItem(new ItemComboUsuario(u.getIdUsuario(), u.getNombreCompleto() + " (" + gradoSec + ")"));
            } else if (u.getRol() == Rol.PADRE) {
                cboPadreU.addItem(new ItemComboUsuario(u.getIdUsuario(), u.getNombreCompleto()));
            }
        }

        lblKpiActivos.setText(String.valueOf(listaActivos.size()));
        lblKpiInactivos.setText(String.valueOf(listaInactivos.size()));
        lblKpiRecargas.setText(String.valueOf(recargas.stream().filter(r -> r.estado() == EstadoRecarga.PENDIENTE).count()));
        lblKpiStockBajo.setText(String.valueOf(productos.stream().filter(p -> p.isActivo() && p.getStock() <= 10).count()));
    }
}
