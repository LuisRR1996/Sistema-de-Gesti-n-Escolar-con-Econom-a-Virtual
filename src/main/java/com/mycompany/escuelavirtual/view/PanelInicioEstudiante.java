/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.dao.LogroDAO;
import com.mycompany.escuelavirtual.dao.LogroDAO.DetalleLogro;
import com.mycompany.escuelavirtual.dao.MovimientoDAO;
import com.mycompany.escuelavirtual.dao.MovimientoDAO.DetalleMovimiento;
import com.mycompany.escuelavirtual.dao.ProductoDAO;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
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
import java.sql.SQLException;
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
        String nombre;
        BigDecimal precioDinero;
        Integer precioMonedas;

        ItemTienda(int id, String nombre, BigDecimal precioDinero, Integer precioMonedas, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.precioDinero = precioDinero;
            this.precioMonedas = precioMonedas;
            this.stock = stock;
        }
    }

    private static class ItemLogro {

        LocalDateTime fecha;
        String tipo, descripcion, docente;
        int monedas;

        ItemLogro(LocalDateTime fecha, String tipo, String descripcion, String docente, int monedas) {
            this.fecha = fecha;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.docente = docente;
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

    private final BilleteraDAO billeteraDAO = new BilleteraDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final LogroDAO logroDAO = new LogroDAO();
    private final Usuario estudianteActual;

    private BigDecimal saldoDinero = BigDecimal.ZERO;
    private int saldoMonedas = 0;

    private final JLabel lblSaldoDinero = Estilos.titulo("");
    private final JLabel lblSaldoMonedas = Estilos.titulo("");

    private final List<ItemTienda> catalogo = new ArrayList<>();
    private final List<ItemLogro> misLogros = new ArrayList<>();
    private final List<ItemMovimiento> misMovimientos = new ArrayList<>();

    private PanelTablaBase<ItemTienda> tablaTienda;
    private PanelTablaBase<ItemLogro> tablaLogros;
    private PanelTablaBase<ItemMovimiento> tablaMovimientos;

    public PanelInicioEstudiante(Usuario estudianteActual) {
        this.estudianteActual = estudianteActual;

        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        JPanel norte = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        norte.setOpaque(false);
        norte.add(Estilos.titulo("Mi Billetera, Tienda y Logros"), BorderLayout.NORTH);

        JPanel pnlSaldos = new JPanel(new GridLayout(1, 2, Estilos.ESPACIO_GRANDE, 0));
        pnlSaldos.setOpaque(false);
        pnlSaldos.add(crearTarjetaSaldoCompacta("Saldo para Gastos Escolares (Soles)", lblSaldoDinero));
        pnlSaldos.add(crearTarjetaSaldoCompacta("Monedas Especiales de Recompensa", lblSaldoMonedas));
        norte.add(pnlSaldos, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, Estilos.ESPACIO_GRANDE, 0));
        centro.setOpaque(false);
        centro.add(crearCuadroTienda());
        centro.add(crearCuadroHistoriales());

        add(centro, BorderLayout.CENTER);
        recargarDesdeBD();
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
        cuadro.add(Estilos.subtitulo("Tienda Escolar y Catálogo de Recompensas (Stock en vivo BD)"), BorderLayout.NORTH);

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
            try {
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
                    if (DialogoConfirmacion.confirmar(this, "Confirmar Compra", "¿Comprar " + cant + "x " + prod.nombre + " por " + Formato.dinero(total) + "?")) {
                        movimientoDAO.registrarCompraOCanje(estudianteActual.getIdUsuario(), prod.id, metodo, cant, total);
                        recargarDesdeBD();
                        Toast.exito(this, "¡Compra realizada en BD con éxito!");
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
                    if (DialogoConfirmacion.confirmar(this, "Confirmar Canje", "¿Canjear " + cant + "x " + prod.nombre + " por " + Formato.monedas(totalMonedas) + "?")) {
                        movimientoDAO.registrarCompraOCanje(estudianteActual.getIdUsuario(), prod.id, metodo, cant, new BigDecimal(totalMonedas));
                        recargarDesdeBD();
                        Toast.exito(this, "¡Recompensa canjeada en BD con éxito!");
                    }
                }
            } catch (SQLException ex) {
                DialogoConfirmacion.error(this, "Error en la base de datos: " + ex.getMessage());
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

        JPanel cuadroLogros = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroLogros.setBackground(Estilos.FONDO_PANEL);
        cuadroLogros.setBorder(Estilos.tarjeta());
        cuadroLogros.add(Estilos.subtitulo("¿En qué he ganado Monedas de Recompensa? (BD)"), BorderLayout.NORTH);

        tablaLogros = new PanelTablaBase<>(
                new String[]{"Fecha", "Mérito / Logro", "Docente", "Monedas"},
                l -> new Object[]{Formato.fechaHora(l.fecha), l.tipo, l.docente, "+" + Formato.monedas(l.monedas)}
        );
        tablaLogros.ocultarFiltroFecha();
        cuadroLogros.add(tablaLogros, BorderLayout.CENTER);

        JPanel cuadroMovs = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroMovs.setBackground(Estilos.FONDO_PANEL);
        cuadroMovs.setBorder(Estilos.tarjeta());
        cuadroMovs.add(Estilos.subtitulo("Mi Historial de Compras y Canjes en la Tienda (BD)"), BorderLayout.NORTH);

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

    private void recargarDesdeBD() {
        if (estudianteActual == null) {
            return;
        }
        try {
            Billetera b = billeteraDAO.obtenerOCrearPorEstudiante(estudianteActual.getIdUsuario());
            saldoDinero = b.getSaldoDinero();
            saldoMonedas = b.getSaldoMonedas();
            lblSaldoDinero.setText(Formato.dinero(saldoDinero));
            lblSaldoMonedas.setText(Formato.monedas(saldoMonedas));

            catalogo.clear();
            for (Producto p : productoDAO.listarActivos()) {
                catalogo.add(new ItemTienda(p.getIdProducto(), p.getNombre(), p.getPrecioDinero(), p.getPrecioMonedas(), p.getStock()));
            }

            misLogros.clear();
            for (DetalleLogro dl : logroDAO.listarPorEstudiante(estudianteActual.getIdUsuario())) {
                misLogros.add(new ItemLogro(dl.fecha(), dl.tipo(), dl.descripcion(), dl.nombreDocente(), dl.monedas()));
            }

            misMovimientos.clear();
            for (DetalleMovimiento dm : movimientoDAO.listarPorEstudiante(estudianteActual.getIdUsuario())) {
                String descTotal = dm.metodoPago() == TipoMoneda.DINERO ? Formato.dinero(dm.montoTotal()) : Formato.monedas(dm.montoTotal().intValue());
                misMovimientos.add(new ItemMovimiento(dm.fecha(), dm.nombreProducto(), dm.metodoPago(), dm.cantidad(), descTotal));
            }

            tablaTienda.setDatos(catalogo);
            tablaLogros.setDatos(misLogros);
            tablaMovimientos.setDatos(misMovimientos);

        } catch (SQLException e) {
            DialogoConfirmacion.error(this, "Error cargando billetera del estudiante: " + e.getMessage());
        }
    }
}
