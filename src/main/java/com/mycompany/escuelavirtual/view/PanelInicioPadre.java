/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.BilleteraController;
import com.mycompany.escuelavirtual.controller.RecargaController;
import com.mycompany.escuelavirtual.controller.UsuarioController;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.DetalleMovimiento;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.MetodoPago;
import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelTablaBase;
import com.mycompany.escuelavirtual.util.ui.Toast;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author user
 */
public class PanelInicioPadre extends JPanel {

    private static final long serialVersionUID = 1L;

    private static class CompraHijo {

        LocalDateTime fecha;
        String producto, metodo, importe;
        int cantidad;

        CompraHijo(LocalDateTime fecha, String producto, String metodo, int cantidad, String importe) {
            this.fecha = fecha;
            this.producto = producto;
            this.metodo = metodo;
            this.cantidad = cantidad;
            this.importe = importe;
        }
    }

    private static class HijoFamiliar {

        int id;
        String nombreCompleto, gradoSeccion;
        BigDecimal saldoDinero;
        int saldoMonedas;
        List<CompraHijo> compras = new ArrayList<>();

        HijoFamiliar(int id, String nombreCompleto, String gradoSeccion, BigDecimal saldoDinero, int saldoMonedas) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.gradoSeccion = gradoSeccion;
            this.saldoDinero = saldoDinero;
            this.saldoMonedas = saldoMonedas;
        }
    }

    private final UsuarioController usuarioController;
    private final BilleteraController billeteraController;
    private final RecargaController recargaController;
    private final Usuario padreActual;

    private final List<HijoFamiliar> hijos = new ArrayList<>();

    private PanelTablaBase<HijoFamiliar> tablaHijos;
    private PanelTablaBase<CompraHijo> tablaComprasHijo;

    private final JLabel lblHijoSeleccionado = Estilos.titulo("-");
    private final JLabel lblSaldoDineroHijo = Estilos.titulo("S/ 0.00");
    private final JLabel lblSaldoMonedasHijo = Estilos.titulo("0 monedas");
    private final JComboBox<String> cboHijoRecarga = new JComboBox<>();

    public PanelInicioPadre(Usuario padreActual) {
        this.padreActual = padreActual;
        this.usuarioController = new UsuarioController(padreActual);
        this.billeteraController = new BilleteraController(padreActual);
        this.recargaController = new RecargaController(padreActual);

        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        JPanel cabeceraTitulo = new JPanel(new BorderLayout());
        cabeceraTitulo.setOpaque(false);
        cabeceraTitulo.add(Estilos.titulo("Supervisión Familiar y Recargas (Grupo Familiar BD)"), BorderLayout.WEST);
        JButton btnRefrescar = Estilos.botonSecundario("↻ Actualizar Datos");
        btnRefrescar.addActionListener(e -> {
            recargarDesdeBD();
            Toast.info(this, "Datos actualizados desde SQL Server.");
        });
        cabeceraTitulo.add(btnRefrescar, BorderLayout.EAST);
        add(cabeceraTitulo, BorderLayout.NORTH);

        JPanel cuadricula = new JPanel(new GridLayout(2, 2, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO_GRANDE));
        cuadricula.setOpaque(false);
        cuadricula.add(crearCuadroListaHijos());
        cuadricula.add(crearCuadroSaldoHijo());
        cuadricula.add(crearCuadroRecargaHijo());
        cuadricula.add(crearCuadroHistorialCompras());
        add(cuadricula, BorderLayout.CENTER);

        recargarDesdeBD();
    }

    private JPanel crearCuadroListaHijos() {
        JPanel cuadro = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadro.setBackground(Estilos.FONDO_PANEL);
        cuadro.setBorder(Estilos.tarjeta());
        cuadro.add(Estilos.subtitulo("1. Mis Hijos Registrados (BD)"), BorderLayout.NORTH);

        tablaHijos = new PanelTablaBase<>(
                new String[]{"ID", "Estudiante", "Grado y Sección", "Saldo S/", "Monedas"},
                h -> new Object[]{h.id, h.nombreCompleto, h.gradoSeccion, Formato.dinero(h.saldoDinero), Formato.monedas(h.saldoMonedas)}
        );
        tablaHijos.ocultarFiltroFecha();
        tablaHijos.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                HijoFamiliar sel = tablaHijos.getSeleccionado();
                if (sel != null) {
                    seleccionarHijo(sel);
                }
            }
        });
        cuadro.add(tablaHijos, BorderLayout.CENTER);
        return cuadro;
    }

    private JPanel crearCuadroSaldoHijo() {
        JPanel cuadro = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        cuadro.setBackground(Estilos.FONDO_PANEL);
        cuadro.setBorder(Estilos.tarjeta());
        cuadro.add(Estilos.subtitulo("2. Detalle de Saldos del Hijo Seleccionado"), BorderLayout.NORTH);

        JPanel datos = new JPanel(new GridLayout(3, 2, Estilos.ESPACIO, Estilos.ESPACIO));
        datos.setOpaque(false);
        datos.add(Estilos.etiqueta("Alumno supervisado:"));
        datos.add(lblHijoSeleccionado);
        datos.add(Estilos.etiqueta("Saldo para Gastos (S/):"));
        datos.add(lblSaldoDineroHijo);
        datos.add(Estilos.etiqueta("Monedas por Logros:"));
        datos.add(lblSaldoMonedasHijo);
        cuadro.add(datos, BorderLayout.CENTER);
        return cuadro;
    }

    private JPanel crearCuadroRecargaHijo() {
        JPanel cuadro = new JPanel(new GridBagLayout());
        cuadro.setBackground(Estilos.FONDO_PANEL);
        cuadro.setBorder(Estilos.tarjeta());

        cboHijoRecarga.addActionListener(e -> {
            int idx = cboHijoRecarga.getSelectedIndex();
            if (idx >= 0 && idx < hijos.size()) {
                seleccionarHijo(hijos.get(idx));
            }
        });

        JTextField txtMonto = new JTextField("20.00", 12);
        JComboBox<MetodoPago> cboMetodo = new JComboBox<>(MetodoPago.values());
        JButton btnRecargar = Estilos.botonPrimario("Enviar Recarga al Hijo");

        btnRecargar.addActionListener(e -> {
            int idx = cboHijoRecarga.getSelectedIndex();
            if (idx < 0) {
                return;
            }
            HijoFamiliar destino = hijos.get(idx);
            BigDecimal monto;
            try {
                monto = new BigDecimal(txtMonto.getText().trim());
            } catch (NumberFormatException ex) {
                DialogoConfirmacion.error(this, "Ingrese un monto numérico válido.");
                return;
            }

            MetodoPago metodo = (MetodoPago) cboMetodo.getSelectedItem();
            if (DialogoConfirmacion.confirmar(this, "Confirmar Recarga Familiar", "¿Registrar recarga de " + Formato.dinero(monto) + " por " + metodo + " para " + destino.nombreCompleto + "?")) {
                try {
                    // El monto, el método y el vínculo padre-hijo los valida el servicio.
                    recargaController.registrar(destino.id, monto, metodo);
                    recargarDesdeBD();
                    Toast.exito(this, "Recarga registrada en estado PENDIENTE: no acredita hasta que el administrador la apruebe.");
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
            }
        });

        int f = 0;
        agregarFila(cuadro, f++, "3. Solicitar Recarga a Administración", null, true);
        agregarFila(cuadro, f++, "Seleccionar Hijo:", cboHijoRecarga, false);
        agregarFila(cuadro, f++, "Monto a Recargar (S/):", txtMonto, false);
        agregarFila(cuadro, f++, "Método de Pago:", cboMetodo, false);
        agregarFila(cuadro, f, "", btnRecargar, false);
        return cuadro;
    }

    private JPanel crearCuadroHistorialCompras() {
        JPanel cuadro = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadro.setBackground(Estilos.FONDO_PANEL);
        cuadro.setBorder(Estilos.tarjeta());
        cuadro.add(Estilos.subtitulo("4. Historial de Compras (BD)"), BorderLayout.NORTH);

        tablaComprasHijo = new PanelTablaBase<>(
                new String[]{"Fecha", "Concepto / Producto", "Tipo", "Cant.", "Importe"},
                c -> new Object[]{Formato.fechaHora(c.fecha), c.producto, c.metodo, c.cantidad, c.importe}
        );
        tablaComprasHijo.ocultarFiltroFecha();
        cuadro.add(tablaComprasHijo, BorderLayout.CENTER);
        return cuadro;
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, JComponent campo, boolean titulo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO);
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (titulo) {
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
        if (padreActual == null) {
            return;
        }
        try {
            hijos.clear();
            cboHijoRecarga.removeAllItems();

            for (Usuario h : usuarioController.listarHijos()) {
                Billetera b = billeteraController.consultarSaldo(h.getIdUsuario());
                HijoFamiliar hf = new HijoFamiliar(
                        h.getIdUsuario(),
                        h.getNombreCompleto(),
                        (h.getGrado() == null ? "-" : h.getGrado()) + " " + (h.getSeccion() == null ? "-" : h.getSeccion()),
                        b.getSaldoDinero(),
                        b.getSaldoMonedas());

                for (DetalleMovimiento dm : billeteraController.listarMovimientos(h.getIdUsuario())) {
                    String descTotal = dm.metodoPago() == TipoMoneda.DINERO
                            ? Formato.dinero(dm.montoTotal())
                            : Formato.monedas(dm.montoTotal().intValue());
                    hf.compras.add(new CompraHijo(dm.fecha(), dm.nombreProducto(), dm.metodoPago().name(),
                            dm.cantidad(), descTotal));
                }
                hijos.add(hf);
                cboHijoRecarga.addItem(hf.nombreCompleto);
            }
            tablaHijos.setDatos(hijos);
            if (!hijos.isEmpty()) {
                seleccionarHijo(hijos.get(0));
            }

        } catch (NegocioException e) {
            DialogoConfirmacion.error(this, e.getMessage());
        }
    }

    private void seleccionarHijo(HijoFamiliar h) {
        lblHijoSeleccionado.setText(h.nombreCompleto + " (" + h.gradoSeccion + ")");
        lblSaldoDineroHijo.setText(Formato.dinero(h.saldoDinero));
        lblSaldoMonedasHijo.setText(Formato.monedas(h.saldoMonedas));
        cboHijoRecarga.setSelectedItem(h.nombreCompleto);
        tablaComprasHijo.setDatos(h.compras);
    }
}
