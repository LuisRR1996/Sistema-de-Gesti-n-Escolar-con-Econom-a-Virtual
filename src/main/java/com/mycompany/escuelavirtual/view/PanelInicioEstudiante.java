/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.BilleteraController;
import com.mycompany.escuelavirtual.controller.CanjeController;
import com.mycompany.escuelavirtual.controller.CatalogoController;
import com.mycompany.escuelavirtual.controller.CompraController;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.DetalleMovimiento;
import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.NegocioException;
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

    private final BilleteraController billeteraController;
    private final CatalogoController catalogoController;
    private final CompraController compraController;
    private final CanjeController canjeController;
    private final Usuario estudianteActual;

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
        this.billeteraController = new BilleteraController(estudianteActual);
        this.catalogoController = new CatalogoController(estudianteActual);
        this.compraController = new CompraController(estudianteActual);
        this.canjeController = new CanjeController(estudianteActual);

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
            TipoMoneda metodo = (TipoMoneda) cboMetodo.getSelectedItem();
            boolean conDinero = metodo == TipoMoneda.DINERO;
            if (!DialogoConfirmacion.confirmar(this,
                    conDinero ? "Confirmar Compra" : "Confirmar Canje",
                    "¿" + (conDinero ? "Comprar " : "Canjear ") + cant + "x " + prod.nombre
                    + totalLegible(prod, metodo, cant) + "?")) {
                return;
            }
            try {
                // El saldo, el stock y la validez del método se comprueban en el
                // servicio, dentro de la transacción; aquí solo se informa.
                if (conDinero) {
                    compraController.comprar(prod.id, cant);
                } else {
                    canjeController.canjear(prod.id, cant);
                }
                recargarDesdeBD();
                Toast.exito(this, conDinero ? "¡Compra realizada!" : "¡Recompensa canjeada!");
            } catch (NegocioException ex) {
                DialogoConfirmacion.error(this, ex.getMessage());
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

    private static String totalLegible(ItemTienda prod, TipoMoneda metodo, int cantidad) {
        if (metodo == TipoMoneda.DINERO) {
            return prod.precioDinero == null ? ""
                    : " por " + Formato.dinero(prod.precioDinero.multiply(BigDecimal.valueOf(cantidad)));
        }
        return prod.precioMonedas == null ? ""
                : " por " + Formato.monedas(prod.precioMonedas * cantidad);
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
            Billetera b = billeteraController.consultarSaldoPropio();
            lblSaldoDinero.setText(Formato.dinero(b.getSaldoDinero()));
            lblSaldoMonedas.setText(Formato.monedas(b.getSaldoMonedas()));

            catalogo.clear();
            for (Producto p : catalogoController.listarActivos()) {
                catalogo.add(new ItemTienda(p.getIdProducto(), p.getNombre(), p.getPrecioDinero(),
                        p.getPrecioMonedas(), p.getStock()));
            }

            misLogros.clear();
            for (DetalleLogro dl : billeteraController.listarLogros(estudianteActual.getIdUsuario())) {
                misLogros.add(new ItemLogro(dl.fecha(), dl.tipo(), dl.descripcion(), dl.nombreDocente(), dl.monedas()));
            }

            misMovimientos.clear();
            for (DetalleMovimiento dm : billeteraController.listarMovimientos(estudianteActual.getIdUsuario())) {
                String descTotal = dm.metodoPago() == TipoMoneda.DINERO
                        ? Formato.dinero(dm.montoTotal())
                        : Formato.monedas(dm.montoTotal().intValue());
                misMovimientos.add(new ItemMovimiento(dm.fecha(), dm.nombreProducto(), dm.metodoPago(),
                        dm.cantidad(), descTotal));
            }

            tablaTienda.setDatos(catalogo);
            tablaLogros.setDatos(misLogros);
            tablaMovimientos.setDatos(misMovimientos);

        } catch (NegocioException e) {
            DialogoConfirmacion.error(this, e.getMessage());
        }
    }
}
