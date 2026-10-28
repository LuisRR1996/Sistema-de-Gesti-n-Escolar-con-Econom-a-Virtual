/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.model.enums.MetodoPago;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.Validador;
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
        String producto, metodo;
        int cantidad;
        BigDecimal monto;

        CompraHijo(LocalDateTime fecha, String producto, String metodo, int cantidad, BigDecimal monto) {
            this.fecha = fecha;
            this.producto = producto;
            this.metodo = metodo;
            this.cantidad = cantidad;
            this.monto = monto;
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

    private final List<HijoFamiliar> hijos = new ArrayList<>();
    private HijoFamiliar hijoActual;

    private PanelTablaBase<HijoFamiliar> tablaHijos;
    private PanelTablaBase<CompraHijo> tablaComprasHijo;

    private final JLabel lblHijoSeleccionado = Estilos.titulo("-");
    private final JLabel lblSaldoDineroHijo = Estilos.titulo("S/ 0.00");
    private final JLabel lblSaldoMonedasHijo = Estilos.titulo("0 monedas");
    private final JComboBox<String> cboHijoRecarga = new JComboBox<>();

    public PanelInicioPadre() {
        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        cargarGrupoFamiliar();

        add(Estilos.titulo("Supervisión Familiar y Recargas (Grupo Familiar)"), BorderLayout.NORTH);

        // Cuadrícula de 4 cuadros (2 arriba, 2 abajo)
        JPanel cuadricula = new JPanel(new GridLayout(2, 2, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO_GRANDE));
        cuadricula.setOpaque(false);

        cuadricula.add(crearCuadroListaHijos());
        cuadricula.add(crearCuadroSaldoHijo());
        cuadricula.add(crearCuadroRecargaHijo());
        cuadricula.add(crearCuadroHistorialCompras());

        add(cuadricula, BorderLayout.CENTER);

        seleccionarHijo(hijos.get(0));
    }

    private JPanel crearCuadroListaHijos() {
        JPanel cuadro = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadro.setBackground(Estilos.FONDO_PANEL);
        cuadro.setBorder(Estilos.tarjeta());
        cuadro.add(Estilos.subtitulo("1. Mis Hijos Registrados (Seleccione uno en la tabla)"), BorderLayout.NORTH);

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

        for (HijoFamiliar h : hijos) {
            cboHijoRecarga.addItem(h.nombreCompleto);
        }
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
            if (!Validador.esMontoDineroValido(monto)) {
                DialogoConfirmacion.error(this, "El monto debe ser positivo y con máximo 2 decimales.");
                return;
            }
            MetodoPago metodo = (MetodoPago) cboMetodo.getSelectedItem();
            if (DialogoConfirmacion.confirmar(this, "Confirmar Recarga Familiar",
                    "¿Registrar recarga de " + Formato.dinero(monto) + " por " + metodo + " para " + destino.nombreCompleto + "?")) {
                destino.saldoDinero = destino.saldoDinero.add(monto);
                destino.compras.add(0, new CompraHijo(LocalDateTime.now(), "Recarga Familiar (" + metodo + ")", "RECARGA", 1, monto));
                seleccionarHijo(destino);
                Toast.exito(this, "Recarga enviada para " + destino.nombreCompleto);
            }
        });

        int f = 0;
        agregarFila(cuadro, f++, "3. Recargar Saldo a Hijo del Grupo Familiar", null, true);
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
        cuadro.add(Estilos.subtitulo("4. Historial de Compras y Movimientos del Hijo Seleccionado"), BorderLayout.NORTH);

        tablaComprasHijo = new PanelTablaBase<>(
                new String[]{"Fecha", "Concepto / Producto", "Tipo", "Cant.", "Importe"},
                c -> new Object[]{Formato.fechaHora(c.fecha), c.producto, c.metodo, c.cantidad, Formato.dinero(c.monto)}
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

    private void cargarGrupoFamiliar() {
        LocalDateTime ahora = LocalDateTime.now();

        HijoFamiliar h1 = new HijoFamiliar(5, "Luis Fernando Torres Ramos", "5.º A", new BigDecimal("50.00"), 120);
        h1.compras.add(new CompraHijo(ahora.minusDays(5), "Menú del día", "DINERO", 1, new BigDecimal("8.50")));
        h1.compras.add(new CompraHijo(ahora.minusDays(1), "Exoneración de tarea (50 monedas)", "MONEDAS", 1, new BigDecimal("0.00")));

        HijoFamiliar h2 = new HijoFamiliar(6, "Carla Sofía Torres Ramos", "5.º A", new BigDecimal("25.00"), 80);
        h2.compras.add(new CompraHijo(ahora.minusDays(2), "Jugo natural", "DINERO", 2, new BigDecimal("7.00")));
        h2.compras.add(new CompraHijo(ahora.minusDays(4), "Cuaderno A4", "DINERO", 1, new BigDecimal("7.50")));

        HijoFamiliar h3 = new HijoFamiliar(9, " Valeria Torres Ramos", "2.º B", new BigDecimal("18.00"), 60);
        h3.compras.add(new CompraHijo(ahora.minusDays(3), "Sándwich de pollo", "DINERO", 1, new BigDecimal("6.00")));

        hijos.add(h1);
        hijos.add(h2);
        hijos.add(h3);
    }

    private void seleccionarHijo(HijoFamiliar h) {
        this.hijoActual = h;
        lblHijoSeleccionado.setText(h.nombreCompleto + " (" + h.gradoSeccion + ")");
        lblSaldoDineroHijo.setText(Formato.dinero(h.saldoDinero));
        lblSaldoMonedasHijo.setText(Formato.monedas(h.saldoMonedas));
        cboHijoRecarga.setSelectedItem(h.nombreCompleto);
        tablaHijos.setDatos(hijos);
        tablaComprasHijo.setDatos(h.compras);
    }

}
