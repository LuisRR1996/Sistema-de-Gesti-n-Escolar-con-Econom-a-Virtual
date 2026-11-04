package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.dao.MovimientoDAO;
import com.mycompany.escuelavirtual.dao.ProductoDAO;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import com.mycompany.escuelavirtual.util.Validador;
import com.mycompany.escuelavirtual.util.db.Transaccion;
import java.math.BigDecimal;

/**
 * Compra en la tienda escolar (M05) y, reutilizando la misma transacción, el
 * canje con monedas que expone {@link CanjeService}.
 *
 * <p>El descuento de saldo, el descuento de stock y el registro del movimiento
 * ocurren dentro de una única transacción JDBC, y las comprobaciones de saldo y
 * de stock se hacen con las filas ya bloqueadas: si algo falla se revierte todo
 * (RN-03, RN-04, RN-05, RN-07, RN-10 y RN-11).</p>
 */
public class CompraService extends ServicioBase {

    private final ProductoDAO productoDAO;
    private final BilleteraDAO billeteraDAO;
    private final MovimientoDAO movimientoDAO;

    public CompraService() {
        this(new ProductoDAO(), new BilleteraDAO(), new MovimientoDAO());
    }

    public CompraService(ProductoDAO productoDAO, BilleteraDAO billeteraDAO, MovimientoDAO movimientoDAO) {
        this.productoDAO = productoDAO;
        this.billeteraDAO = billeteraDAO;
        this.movimientoDAO = movimientoDAO;
    }

    /** Compra con saldo de dinero. Solo el estudiante dueño de la billetera. */
    public void comprar(Usuario actor, int idProducto, int cantidad) {
        Permisos.exigir(actor, Accion.COMPRAR_CON_DINERO);
        registrar(actor, idProducto, cantidad, TipoMoneda.DINERO);
    }

    /**
     * Operación común de compra y canje: el permiso ya fue verificado por el
     * servicio que la invoca.
     */
    void registrar(Usuario actor, int idProducto, int cantidad, TipoMoneda metodo) {
        if (!Validador.esIdValido(idProducto) || !Validador.esCantidadValida(cantidad)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "Seleccione un producto y una cantidad válida.");
        }
        ejecutar(() -> Transaccion.ejecutar(cn -> {
            Producto producto = productoDAO.bloquearPorId(cn, idProducto);
            if (producto == null || !producto.isActivo()) {
                throw new NegocioException(CodigoError.NO_ENCONTRADO,
                        "El producto ya no está disponible.");
            }
            if (producto.getStock() < cantidad) { // RN-04
                throw new NegocioException(CodigoError.STOCK_INSUFICIENTE,
                        "Solo quedan " + producto.getStock() + " unidades de '" + producto.getNombre() + "'.");
            }

            Billetera billetera = billeteraDAO.bloquearPorEstudiante(cn, actor.getIdUsuario());
            int idBilletera = billetera != null
                    ? billetera.getIdBilletera()
                    : billeteraDAO.crear(cn, actor.getIdUsuario()).getIdBilletera();
            BigDecimal saldoDinero = billetera != null ? billetera.getSaldoDinero() : BigDecimal.ZERO;
            int saldoMonedas = billetera != null ? billetera.getSaldoMonedas() : 0;

            BigDecimal montoTotal;
            if (metodo == TipoMoneda.DINERO) {
                if (producto.getPrecioDinero() == null) { // RN-10
                    throw new NegocioException(CodigoError.CONFLICTO,
                            "'" + producto.getNombre() + "' solo se puede canjear con monedas.");
                }
                montoTotal = producto.getPrecioDinero().multiply(BigDecimal.valueOf(cantidad)); // RN-11
                if (saldoDinero.compareTo(montoTotal) < 0) { // RN-03
                    throw new NegocioException(CodigoError.SALDO_INSUFICIENTE,
                            "Saldo insuficiente: la compra cuesta " + montoTotal + " y hay " + saldoDinero + ".");
                }
                billeteraDAO.descontarDinero(cn, idBilletera, montoTotal);
            } else {
                if (producto.getPrecioMonedas() == null) { // RN-10
                    throw new NegocioException(CodigoError.CONFLICTO,
                            "'" + producto.getNombre() + "' solo se puede comprar con dinero.");
                }
                int totalMonedas = producto.getPrecioMonedas() * cantidad; // RN-11
                if (saldoMonedas < totalMonedas) { // RN-03
                    throw new NegocioException(CodigoError.SALDO_INSUFICIENTE,
                            "Monedas insuficientes: el canje cuesta " + totalMonedas + " y hay " + saldoMonedas + ".");
                }
                montoTotal = BigDecimal.valueOf(totalMonedas);
                billeteraDAO.descontarMonedas(cn, idBilletera, totalMonedas);
            }

            productoDAO.descontarStock(cn, idProducto, cantidad);
            // RN-05 y RN-14: movimiento auditable con el usuario que lo ejecutó.
            movimientoDAO.insertar(cn, idBilletera, idProducto, metodo, cantidad, montoTotal,
                    actor.getIdUsuario());
        }));
    }
}
