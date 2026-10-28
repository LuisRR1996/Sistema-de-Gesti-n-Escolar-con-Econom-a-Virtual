/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.model.enums.EstadoRecarga;
import com.mycompany.escuelavirtual.model.enums.MetodoPago;
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
import java.time.LocalDateTime;
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

/**
 *
 * @author user
 */
public class PanelInicioAdmin extends JPanel {

    private static final long serialVersionUID = 1L;

    private static class FilaUsuario {
        int id;
        String nombres, apellidos, correo, grado, seccion;
        Rol rol;
        boolean activo;

        FilaUsuario(int id, String nombres, String apellidos, String correo, Rol rol, String grado, String seccion, boolean activo) {
            this.id = id;
            this.nombres = nombres;
            this.apellidos = apellidos;
            this.correo = correo;
            this.rol = rol;
            this.grado = grado;
            this.seccion = seccion;
            this.activo = activo;
        }
    }

    private static class FilaRecarga {
        int id;
        String padre, estudiante;
        BigDecimal monto;
        MetodoPago metodo;
        EstadoRecarga estado;
        LocalDateTime fecha;

        FilaRecarga(int id, String padre, String estudiante, BigDecimal monto, MetodoPago metodo, EstadoRecarga estado, LocalDateTime fecha) {
            this.id = id;
            this.padre = padre;
            this.estudiante = estudiante;
            this.monto = monto;
            this.metodo = metodo;
            this.estado = estado;
            this.fecha = fecha;
        }
    }

    private static class FilaProducto {
        int id, stock;
        String nombre, descripcion;
        BigDecimal precioDinero;
        Integer precioMonedas;
        boolean activo;

        FilaProducto(int id, String nombre, String descripcion, BigDecimal precioDinero, Integer precioMonedas, int stock, boolean activo) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precioDinero = precioDinero;
            this.precioMonedas = precioMonedas;
            this.stock = stock;
            this.activo = activo;
        }

        String alertaStock() {
            if (!activo) return "INACTIVO";
            if (stock == 0) return "AGOTADO - REPONER";
            if (stock <= 10) return "⚠ BAJO STOCK (Reponer)";
            return "OK";
        }
    }

    private final List<FilaUsuario> usuarios = new ArrayList<>();
    private final List<FilaRecarga> recargas = new ArrayList<>();
    private final List<FilaProducto> productos = new ArrayList<>();

    private final JLabel lblKpiActivos = Estilos.titulo("0");
    private final JLabel lblKpiInactivos = Estilos.titulo("0");
    private final JLabel lblKpiRecargas = Estilos.titulo("0");
    private final JLabel lblKpiStockBajo = Estilos.titulo("0");

    private PanelTablaBase<FilaUsuario> tablaActivos;
    private PanelTablaBase<FilaUsuario> tablaInactivos;
    private PanelTablaBase<FilaRecarga> tablaRecargas;
    private PanelTablaBase<FilaProducto> tablaProductos;
    private JComboBox<String> cboAlumnoMonedas;

    // Variables de estado para edición
    private FilaUsuario usuarioEnEdicion = null;
    private FilaProducto productoEnEdicion = null;

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
        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        cargarDatosIniciales();

        JPanel norte = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Panel de Administración General"), BorderLayout.NORTH);
        norte.add(crearBarraIndicadores(), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilos.FUENTE_NEGRITA);
        pestanas.addTab("1. Usuarios y Alumnos (Activos / Inactivos)", crearSeccionUsuarios());
        pestanas.addTab("2. Autorizar Recargas y Dar Monedas", crearSeccionRecargasYMonedas());
        pestanas.addTab("3. Catálogo de Productos y Control de Stock", crearSeccionProductos());

        add(pestanas, BorderLayout.CENTER);
        refrescarTodo();
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

        // Cuadro izquierdo: Formulario de Usuario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilos.FONDO_PANEL);
        form.setBorder(Estilos.tarjeta());

        btnCancelarUsuario.setVisible(false);
        btnCancelarUsuario.addActionListener(e -> limpiarFormularioUsuario());

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
            
            if (usuarioEnEdicion == null) {
                // Agregar nuevo
                int nuevoId = usuarios.size() + 1;
                usuarios.add(new FilaUsuario(
                        nuevoId, txtNombresU.getText().trim(), txtApellidosU.getText().trim(),
                        txtCorreoU.getText().trim().toLowerCase(), rol,
                        rol == Rol.ESTUDIANTE ? txtGradoU.getText().trim() : "-",
                        rol == Rol.ESTUDIANTE ? txtSeccionU.getText().trim() : "-", true
                ));
                Toast.exito(this, "Usuario registrado con éxito.");
            } else {
                // Actualizar existente
                usuarioEnEdicion.nombres = txtNombresU.getText().trim();
                usuarioEnEdicion.apellidos = txtApellidosU.getText().trim();
                usuarioEnEdicion.correo = txtCorreoU.getText().trim().toLowerCase();
                usuarioEnEdicion.rol = rol;
                usuarioEnEdicion.grado = rol == Rol.ESTUDIANTE ? txtGradoU.getText().trim() : "-";
                usuarioEnEdicion.seccion = rol == Rol.ESTUDIANTE ? txtSeccionU.getText().trim() : "-";
                Toast.exito(this, "Datos del usuario actualizados.");
            }
            limpiarFormularioUsuario();
            refrescarTodo();
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
        agregarFilaForm(form, f, "", panelBotones, false);

        // Cuadros derechos: Activos e Inactivos
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
            FilaUsuario sel = tablaActivos.getSeleccionado();
            if (sel != null) {
                sel.activo = false;
                refrescarTodo();
                Toast.info(this, sel.nombres + " pasó a estado INACTIVO.");
            }
        });

        tablaActivos = new PanelTablaBase<>(
                new String[]{"ID", "Nombres y Apellidos", "Correo", "Rol", "Grado/Sec.", "Estado"},
                u -> new Object[]{u.id, u.nombres + " " + u.apellidos, u.correo, u.rol, u.grado + " " + u.seccion, "ACTIVO"}
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
            FilaUsuario sel = tablaInactivos.getSeleccionado();
            if (sel != null) {
                sel.activo = true;
                refrescarTodo();
                Toast.exito(this, sel.nombres + " fue REACTIVADO.");
            }
        });

        tablaInactivos = new PanelTablaBase<>(
                new String[]{"ID", "Nombres y Apellidos", "Correo", "Rol", "Grado/Sec.", "Estado"},
                u -> new Object[]{u.id, u.nombres + " " + u.apellidos, u.correo, u.rol, u.grado + " " + u.seccion, "INACTIVO"}
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

    private void cargarUsuarioEnFormulario(FilaUsuario sel) {
        if (sel == null) {
            DialogoConfirmacion.error(this, "Seleccione un usuario de la tabla para editar.");
            return;
        }
        usuarioEnEdicion = sel;
        txtNombresU.setText(sel.nombres);
        txtApellidosU.setText(sel.apellidos);
        txtCorreoU.setText(sel.correo);
        cboRolU.setSelectedItem(sel.rol);
        txtGradoU.setText(sel.grado.equals("-") ? "" : sel.grado);
        txtSeccionU.setText(sel.seccion.equals("-") ? "" : sel.seccion);
        
        btnGuardarUsuario.setText("Guardar Cambios");
        btnCancelarUsuario.setVisible(true);
        txtClaveU.setEnabled(false); // No se edita la clave por aquí
    }

    private void limpiarFormularioUsuario() {
        usuarioEnEdicion = null;
        txtNombresU.setText("");
        txtApellidosU.setText("");
        txtCorreoU.setText("");
        txtGradoU.setText("");
        txtSeccionU.setText("");
        txtClaveU.setEnabled(true);
        btnGuardarUsuario.setText("Agregar Usuario");
        btnCancelarUsuario.setVisible(false);
    }

    private JPanel crearSeccionRecargasYMonedas() {
        JPanel contenedor = new JPanel(new GridLayout(1, 2, Estilos.ESPACIO_GRANDE, 0));
        contenedor.setBackground(Estilos.FONDO);
        contenedor.setBorder(Estilos.margen());

        // Cuadro Izquierdo: Autorizar recargas de los padres
        JPanel cuadroRecargas = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        cuadroRecargas.setBackground(Estilos.FONDO_PANEL);
        cuadroRecargas.setBorder(Estilos.tarjeta());
        cuadroRecargas.add(Estilos.subtitulo("Solicitudes de Recarga de Padres"), BorderLayout.NORTH);

        tablaRecargas = new PanelTablaBase<>(
                new String[]{"ID", "Fecha", "Padre", "Alumno", "Monto", "Método", "Estado"},
                r -> new Object[]{r.id, Formato.fechaHora(r.fecha), r.padre, r.estudiante, Formato.dinero(r.monto), r.metodo, r.estado}
        );
        tablaRecargas.ocultarFiltroFecha();
        cuadroRecargas.add(tablaRecargas, BorderLayout.CENTER);

        JPanel botonesRecarga = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        botonesRecarga.setOpaque(false);
        JButton btnAprobar = Estilos.botonPrimario("Autorizar Recarga");
        JButton btnRechazar = Estilos.botonSecundario("Rechazar");

        btnAprobar.addActionListener(e -> {
            FilaRecarga sel = tablaRecargas.getSeleccionado();
            if (sel != null && sel.estado == EstadoRecarga.PENDIENTE) {
                if (DialogoConfirmacion.confirmar(this, "Autorizar Recarga", "¿Aprobar recarga de " + Formato.dinero(sel.monto) + " para " + sel.estudiante + "?")) {
                    sel.estado = EstadoRecarga.APROBADA;
                    refrescarTodo();
                    Toast.exito(this, "Recarga aprobada y acreditada al alumno.");
                }
            }
        });

        btnRechazar.addActionListener(e -> {
            FilaRecarga sel = tablaRecargas.getSeleccionado();
            if (sel != null && sel.estado == EstadoRecarga.PENDIENTE) {
                sel.estado = EstadoRecarga.RECHAZADA;
                refrescarTodo();
                Toast.info(this, "Recarga rechazada.");
            }
        });

        botonesRecarga.add(btnRechazar);
        botonesRecarga.add(btnAprobar);
        cuadroRecargas.add(botonesRecarga, BorderLayout.SOUTH);

        // Cuadro Derecho: Recargar monedas especiales a un alumno
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
            String alumno = (String) cboAlumnoMonedas.getSelectedItem();
            int cant = (Integer) spnMonedas.getValue();
            if (alumno != null && DialogoConfirmacion.confirmar(this, "Confirmar Monedas Especiales", "¿Acreditar " + cant + " monedas a " + alumno + "?")) {
                Toast.exito(this, "Se acreditaron +" + cant + " monedas a " + alumno);
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

        // Cuadro Izquierdo: Formulario de Producto
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
                if (!txtPrecioSolesP.getText().isBlank()) pDinero = new BigDecimal(txtPrecioSolesP.getText().trim());
                if (!txtPrecioMonedasP.getText().isBlank()) pMonedas = Integer.valueOf(txtPrecioMonedasP.getText().trim());
            } catch (NumberFormatException ex) {
                DialogoConfirmacion.error(this, "Verifique los importes en Soles o Monedas.");
                return;
            }
            if (pDinero == null && pMonedas == null) {
                DialogoConfirmacion.error(this, "Defina al menos un precio (Soles o Monedas).");
                return;
            }
            
            if (productoEnEdicion == null) {
                // Nuevo
                productos.add(new FilaProducto(productos.size() + 1, txtNombreP.getText().trim(), txtDescP.getText().trim(), pDinero, pMonedas, (Integer) spnStockP.getValue(), true));
                Toast.exito(this, "Producto agregado al catálogo.");
            } else {
                // Actualizar
                productoEnEdicion.nombre = txtNombreP.getText().trim();
                productoEnEdicion.descripcion = txtDescP.getText().trim();
                productoEnEdicion.precioDinero = pDinero;
                productoEnEdicion.precioMonedas = pMonedas;
                productoEnEdicion.stock = (Integer) spnStockP.getValue();
                Toast.exito(this, "Producto actualizado con éxito.");
            }
            limpiarFormularioProducto();
            refrescarTodo();
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

        // Cuadro Derecho: Tabla
        JPanel cuadroTabla = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        cuadroTabla.setBackground(Estilos.FONDO_PANEL);
        cuadroTabla.setBorder(Estilos.tarjeta());

        tablaProductos = new PanelTablaBase<>(
                new String[]{"ID", "Producto", "Precio S/", "Precio Monedas", "Stock", "Estado / Aviso"},
                p -> new Object[]{p.id, p.nombre, p.precioDinero == null ? "-" : Formato.dinero(p.precioDinero), p.precioMonedas == null ? "-" : Formato.monedas(p.precioMonedas), p.stock, p.alertaStock()}
        );
        tablaProductos.ocultarFiltroFecha();
        cuadroTabla.add(tablaProductos, BorderLayout.CENTER);

        JPanel accionesProd = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        accionesProd.setOpaque(false);

        JButton btnEditar = Estilos.botonSecundario("Editar Producto");
        JButton btnSimularCompra = Estilos.botonSecundario("Simular Compra (-1)");
        JButton btnReponer = Estilos.botonPrimario("Reponer Stock (+10)");
        JButton btnEliminar = Estilos.botonSecundario("Dar de Baja");

        btnEditar.addActionListener(e -> cargarProductoEnFormulario(tablaProductos.getSeleccionado()));
        
        btnSimularCompra.addActionListener(e -> {
            FilaProducto sel = tablaProductos.getSeleccionado();
            if (sel != null && sel.stock > 0) {
                sel.stock--;
                refrescarTodo();
                if (sel.stock <= 10) Toast.error(this, "⚠ Aviso: Stock bajo de '" + sel.nombre + "'.");
            }
        });

        btnReponer.addActionListener(e -> {
            FilaProducto sel = tablaProductos.getSeleccionado();
            if (sel != null) {
                sel.stock += 10;
                refrescarTodo();
                Toast.exito(this, "Repuestos +10 de " + sel.nombre);
            }
        });

        btnEliminar.addActionListener(e -> {
            FilaProducto sel = tablaProductos.getSeleccionado();
            if (sel != null && DialogoConfirmacion.confirmar(this, "Eliminar", "¿Dar de baja '" + sel.nombre + "'?")) {
                sel.activo = false;
                refrescarTodo();
                Toast.info(this, "Producto dado de baja.");
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

    private void cargarProductoEnFormulario(FilaProducto sel) {
        if (sel == null) {
            DialogoConfirmacion.error(this, "Seleccione un producto para editar.");
            return;
        }
        productoEnEdicion = sel;
        txtNombreP.setText(sel.nombre);
        txtDescP.setText(sel.descripcion);
        txtPrecioSolesP.setText(sel.precioDinero != null ? sel.precioDinero.toString() : "");
        txtPrecioMonedasP.setText(sel.precioMonedas != null ? sel.precioMonedas.toString() : "");
        spnStockP.setValue(sel.stock);
        
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
            c.gridx = 0; c.gridy = fila; c.gridwidth = 2;
            panel.add(Estilos.subtitulo(etiqueta), c);
            return;
        }
        c.gridx = 0; c.gridy = fila; panel.add(Estilos.etiqueta(etiqueta), c);
        c.gridx = 1; c.weightx = 1.0; panel.add(campo, c);
    }

    private void cargarDatosIniciales() {
        usuarios.add(new FilaUsuario(1, "Ana María", "Salazar Ríos", "admin@colegio.edu", Rol.ADMIN, "-", "-", true));
        usuarios.add(new FilaUsuario(2, "Rosa Elena", "Quispe Huamán", "docente@colegio.edu", Rol.DOCENTE, "-", "-", true));
        usuarios.add(new FilaUsuario(3, "Miguel Ángel", "Torres Vega", "padre1@correo.com", Rol.PADRE, "-", "-", true));
        usuarios.add(new FilaUsuario(5, "Luis Fernando", "Torres Ramos", "estudiante1@colegio.edu", Rol.ESTUDIANTE, "5.º", "A", true));
        usuarios.add(new FilaUsuario(6, "Carla Sofía", "Torres Ramos", "estudiante2@colegio.edu", Rol.ESTUDIANTE, "5.º", "A", true));
        usuarios.add(new FilaUsuario(8, "Mateo Sebastián", "Gómez Díaz", "estudiante4@colegio.edu", Rol.ESTUDIANTE, "3.º", "C", false));

        LocalDateTime ahora = LocalDateTime.now();
        recargas.add(new FilaRecarga(1, "Miguel Ángel Torres", "Luis Fernando Torres", new BigDecimal("58.50"), MetodoPago.YAPE, EstadoRecarga.APROBADA, ahora.minusDays(10)));
        recargas.add(new FilaRecarga(3, "Ana Lucía Ramos", "Carla Sofía Torres", new BigDecimal("30.00"), MetodoPago.EFECTIVO, EstadoRecarga.PENDIENTE, ahora.minusDays(1)));

        productos.add(new FilaProducto(1, "Menú del día", "Almuerzo completo del comedor", new BigDecimal("8.50"), null, 40, true));
        productos.add(new FilaProducto(5, "Cuaderno A4", "Cuaderno anillado 100 hojas", new BigDecimal("7.50"), null, 15, true));
        productos.add(new FilaProducto(6, "Pase libre de uniforme", "Un día sin uniforme", null, 80, 10, true));
        productos.add(new FilaProducto(8, "Gorra del colegio", "Gorra oficial institucional", null, 100, 5, true));
    }

    private void refrescarTodo() {
        List<FilaUsuario> listaActivos = usuarios.stream().filter(u -> u.activo).toList();
        List<FilaUsuario> listaInactivos = usuarios.stream().filter(u -> !u.activo).toList();
        tablaActivos.setDatos(listaActivos);
        tablaInactivos.setDatos(listaInactivos);
        tablaRecargas.setDatos(recargas);
        tablaProductos.setDatos(productos.stream().filter(p -> p.activo).toList());

        cboAlumnoMonedas.removeAllItems();
        for (FilaUsuario u : listaActivos) {
            if (u.rol == Rol.ESTUDIANTE) cboAlumnoMonedas.addItem(u.nombres + " " + u.apellidos + " (" + u.grado + " " + u.seccion + ")");
        }

        lblKpiActivos.setText(String.valueOf(listaActivos.size()));
        lblKpiInactivos.setText(String.valueOf(listaInactivos.size()));
        lblKpiRecargas.setText(String.valueOf(recargas.stream().filter(r -> r.estado == EstadoRecarga.PENDIENTE).count()));
        lblKpiStockBajo.setText(String.valueOf(productos.stream().filter(p -> p.activo && p.stock <= 10).count()));
    }

}
