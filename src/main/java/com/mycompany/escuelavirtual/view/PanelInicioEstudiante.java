/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelTablaBase;
import com.mycompany.escuelavirtual.util.ui.Toast;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author user
 */
public class PanelInicioEstudiante extends JPanel {

    private static final long serialVersionUID = 1L;

    private static class ItemTienda {

        int id, stock;
        String nombre, descripcion;
        BigDecimal precioDinero;
        Integer precioMonedas;

        ItemTienda(int id, String nombre, String descripcion, BigDecimal precioDinero, Integer precioMonedas, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precioDinero = precioDinero;
            this.precioMonedas = precioMonedas;
            this.stock = stock;
        }
    }

    private static class ItemLogro {

        LocalDateTime fecha;
        String tipo, descripcion;
        int monedas;

        ItemLogro(LocalDateTime fecha, String tipo, String descripcion, int monedas) {
            this.fecha = fecha;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.monedas = monedas;
        }
    }

    private static class ItemMovimiento {

        LocalDateTime fecha;
        String producto;
        TipoMoneda metodo;
        int cantidad;
        String total;

        ItemMovimiento(LocalDateTime fecha, String producto, TipoMoneda metodo, int cantidad, String total) {
            this.fecha = fecha;
            this.producto = producto;
            this.metodo = metodo;
            this.cantidad = cantidad;
            this.total = total;
        }
    }

    private BigDecimal saldoDinero = new BigDecimal("50.00");
    private int saldoMonedas = 120;

    private final JLabel lblSaldoDinero = Estilos.titulo("");
    private final JLabel lblSaldoMonedas = Estilos.titulo("");

    private final List<ItemTienda> catalogo = new ArrayList<>();
    private final List<ItemLogro> misLogros = new ArrayList<>();
    private final List<ItemMovimiento> misMovimientos = new ArrayList<>();

    private PanelTablaBase<ItemTienda> tablaTienda;
    private PanelTablaBase<ItemLogro> tablaLogros;
    private PanelTablaBase<ItemMovimiento> tablaMovimientos;

    public PanelInicioEstudiante() {
        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        cargarDatosIniciales();

        // Cabecera compacta con los 2 saldos
        JPanel norte = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Mi Billetera, Tienda y Logros"), BorderLayout.NORTH);

        JPanel pnlSaldos = new JPanel(new GridLayout(1, 2, Estilos.ESPACIO_GRANDE, 0));
        pnlSaldos.setOpaque(false);
        pnlSaldos.add(crearTarjetaSaldoCompacta("Saldo para Gastos Escolares", lblSaldoDinero));
        pnlSaldos.add(crearTarjetaSaldoCompacta("Monedas Especiales de Recompensa", lblSaldoMonedas));
        norte.add(pnlSaldos, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // Cuerpo principal: Izquierda Tienda, Derecha Historial de Logros y Compras
        JPanel centro = new JPanel(new GridLayout(1, 2, Estilos.ESPACIO_GRANDE, 0));
        centro.setOpaque(false);
        centro.add(crearCuadroTienda());
        centro.add(crearCuadroHistoriales());

        add(centro, BorderLayout.CENTER);
        actualizarVista();
    }

    private JPanel crearTarjetaSaldoCompacta(String titulo, JLabel lblValor) {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Estilos.FONDO_PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(Estilos.ESPACIO, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO, Estilos.ESPACIO_GRANDE)));
        tarjeta.add(Estilos.subtitulo(titulo), BorderLayout.WEST);
        tarjeta.add(lblValor, BorderLayout.EAST);
        return tarjeta;
    }

    private JPanel crearCuadroTienda() {
        JPanel cuadro = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        cuadro.setBackground(Estilos.FONDO_PANEL);
        cuadro.setBorder(Estilos.tarjeta());
        cuadro.add(Estilos.subtitulo("Tienda Escolar y Catálogo de Recompensas (Stock en vivo)"), BorderLayout.NORTH);

        tablaTienda = new PanelTablaBase<>(
                new String[]{"ID", "Producto / Recompensa", "Precio S/", "Costo Monedas", "Stock"},
                p -> new Object[]{
                    p.id, p.nombre,
                    p.precioDinero == null ? "No aplica" : Formato.dinero(p.precioDinero),
                    p.precioMonedas == null ? "No aplica" : Formato.monedas(p.precioMonedas),
                    p.stock
                }
        );
        tablaTienda.ocultarFiltroFecha();
        cuadro.add(tablaTienda, BorderLayout.CENTER);

        JPanel pieCompra = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
        pieCompra.setOpaque(false);

        JSpinner spnCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JComboBox<TipoMoneda> cboMetodo = new JComboBox<>(TipoMoneda.values());
        JButton btnComprar = Estilos.botonPrimario("Comprar / Canjear");

        btnComprar.addActionListener(e -> {
            ItemTienda prod = tablaTienda.getSeleccionado();
            if (prod == null) {
                DialogoConfirmacion.error(this, "Seleccione un producto o recompensa de la tienda.");
                return;
            }
            int cant = (Integer) spnCantidad.getValue();
            if (prod.stock < cant) {
                DialogoConfirmacion.error(this, "No hay stock suficiente para '" + prod.nombre + "'.");
                return;
            }
            TipoMoneda metodo = (TipoMoneda) cboMetodo.getSelectedItem();
            if (metodo == TipoMoneda.DINERO) {
                if (prod.precioDinero == null) {
                    DialogoConfirmacion.error(this, "Este artículo solo se puede canjear con MONEDAS.");
                    return;
                }
                BigDecimal total = prod.precioDinero.multiply(BigDecimal.valueOf(cant));
                if (saldoDinero.compareTo(total) < 0) {
                    DialogoConfirmacion.error(this, "Saldo insuficiente en Soles para realizar la compra.");
                    return;
                }
                if (DialogoConfirmacion.confirmar(this, "Confirmar Compra",
                        "¿Comprar " + cant + "x " + prod.nombre + " por " + Formato.dinero(total) + "?")) {
                    saldoDinero = saldoDinero.subtract(total);
                    prod.stock -= cant;
                    misMovimientos.add(0, new ItemMovimiento(LocalDateTime.now(), prod.nombre, TipoMoneda.DINERO, cant, Formato.dinero(total)));
                    actualizarVista();
                    Toast.exito(this, "¡Compra realizada con éxito!");
                }
            } else {
                if (prod.precioMonedas == null) {
                    DialogoConfirmacion.error(this, "Este producto solo se compra con DINERO (S/).");
                    return;
                }
                int totalMonedas = prod.precioMonedas * cant;
                if (saldoMonedas < totalMonedas) {
                    DialogoConfirmacion.error(this, "No tienes suficientes monedas especiales para este canje.");
                    return;
                }
                if (DialogoConfirmacion.confirmar(this, "Confirmar Canje",
                        "¿Canjear " + cant + "x " + prod.nombre + " por " + Formato.monedas(totalMonedas) + "?")) {
                    saldoMonedas -= totalMonedas;
                    prod.stock -= cant;
                    misMovimientos.add(0, new ItemMovimiento(LocalDateTime.now(), prod.nombre, TipoMoneda.MONEDAS, cant, Formato.monedas(totalMonedas)));
                    actualizarVista();
                    Toast.exito(this, "¡Recompensa canjeada con éxito!");
                }
            }
        });

        pieCompra.add(Estilos.etiqueta("Cant.:"));
        pieCompra.add(spnCantidad);
        pieCompra.add(Estilos.etiqueta("Pagar con:"));
        pieCompra.add(cboMetodo);
        pieCompra.add(btnComprar);
        cuadro.add(pieCompra, BorderLayout.SOUTH);

        return cuadro;
    }

    private JPanel crearCuadroHistoriales() {
        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, Estilos.ESPACIO));
        derecha.setOpaque(false);

        // Cuadro Superior: En qué ha ganado monedas de recompensa
        JPanel cuadroLogros = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroLogros.setBackground(Estilos.FONDO_PANEL);
        cuadroLogros.setBorder(Estilos.tarjeta());
        cuadroLogros.add(Estilos.subtitulo("¿En qué he ganado Monedas de Recompensa? (Mis Logros)"), BorderLayout.NORTH);

        tablaLogros = new PanelTablaBase<>(
                new String[]{"Fecha", "Mérito / Logro", "Detalle del Docente", "Monedas Ganadas"},
                l -> new Object[]{Formato.fechaHora(l.fecha), l.tipo, l.descripcion, "+" + Formato.monedas(l.monedas)}
        );
        tablaLogros.ocultarFiltroFecha();
        cuadroLogros.add(tablaLogros, BorderLayout.CENTER);

        // Cuadro Inferior: Historial de compras y canjes
        JPanel cuadroMovs = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroMovs.setBackground(Estilos.FONDO_PANEL);
        cuadroMovs.setBorder(Estilos.tarjeta());
        cuadroMovs.add(Estilos.subtitulo("Mi Historial de Compras y Canjes en la Tienda"), BorderLayout.NORTH);

        tablaMovimientos = new PanelTablaBase<>(
                new String[]{"Fecha", "Producto / Recompensa", "Método", "Cant.", "Total"},
                m -> new Object[]{Formato.fechaHora(m.fecha), m.producto, m.metodo, m.cantidad, m.total}
        );
        tablaMovimientos.ocultarFiltroFecha();
        cuadroMovs.add(tablaMovimientos, BorderLayout.CENTER);

        derecha.add(cuadroLogros);
        derecha.add(cuadroMovs);
        return derecha;
    }

    private void cargarDatosIniciales() {
        catalogo.add(new ItemTienda(1, "Menú del día", "Almuerzo completo escolar", new BigDecimal("8.50"), null, 40));
        catalogo.add(new ItemTienda(2, "Sándwich de pollo", "Sándwich fresco", new BigDecimal("6.00"), null, 30));
        catalogo.add(new ItemTienda(3, "Jugo natural", "Vaso de 300 ml", new BigDecimal("3.50"), null, 25));
        catalogo.add(new ItemTienda(4, "Galletas integrales", "Paquete individual", new BigDecimal("2.00"), null, 60));
        catalogo.add(new ItemTienda(5, "Cuaderno A4", "Anillado 100 hojas", new BigDecimal("7.50"), null, 15));
        catalogo.add(new ItemTienda(6, "Pase libre de uniforme", "Un día sin uniforme", null, 80, 10));
        catalogo.add(new ItemTienda(7, "Exoneración de tarea", "Un día sin tarea asignada", null, 50, 20));
        catalogo.add(new ItemTienda(8, "Gorra del colegio", "Gorra oficial institucional", null, 100, 5));
        catalogo.add(new ItemTienda(9, "Salida anticipada al recreo", "Cinco minutos antes", null, 30, 25));
        catalogo.add(new ItemTienda(10, "Set de lapiceros", "Tres lapiceros de colores", new BigDecimal("4.50"), 35, 20));

        LocalDateTime ahora = LocalDateTime.now();
        misLogros.add(new ItemLogro(ahora.minusDays(6), "Participación en clase", "Aportes voluntarios en semana de repaso", 10));
        misLogros.add(new ItemLogro(ahora.minusDays(4), "Puntualidad", "Asistió a tiempo todos los días del mes", 5));
        misLogros.add(new ItemLogro(ahora.minusDays(1), "Trabajo en equipo", "Apoyo destacado en laboratorio", 25));

        misMovimientos.add(new ItemMovimiento(ahora.minusDays(5), "Menú del día", TipoMoneda.DINERO, 1, Formato.dinero(new BigDecimal("8.50"))));
        misMovimientos.add(new ItemMovimiento(ahora.minusHours(6), "Exoneración de tarea", TipoMoneda.MONEDAS, 1, Formato.monedas(50)));
    }

    private void actualizarVista() {
        lblSaldoDinero.setText(Formato.dinero(saldoDinero));
        lblSaldoMonedas.setText(Formato.monedas(saldoMonedas));
        tablaTienda.setDatos(catalogo);
        tablaLogros.setDatos(misLogros);
        tablaMovimientos.setDatos(misMovimientos);
    }

}
