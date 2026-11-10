package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.TipoMoneda;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;

/**
 * Canje de recompensas con monedas especiales (M07).
 *
 * <p>Reutiliza la transacción de {@link CompraService} —misma validación de
 * stock, de saldo y mismo registro del movimiento— cambiando únicamente el
 * saldo que se descuenta, porque las monedas y el dinero nunca se convierten
 * entre sí (RN-01).</p>
 */
public class CanjeService extends ServicioBase {

    private final CompraService compraService;

    public CanjeService() {
        this(new CompraService());
    }

    public CanjeService(CompraService compraService) {
        this.compraService = compraService;
    }

    public void canjear(Usuario actor, int idProducto, int cantidad) {
        Permisos.exigir(actor, Accion.CANJEAR_CON_MONEDAS);
        compraService.registrar(actor, idProducto, cantidad, TipoMoneda.MONEDAS);
    }
}
