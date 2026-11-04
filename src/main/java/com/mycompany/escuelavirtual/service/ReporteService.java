package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.model.CuadreBilletera;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import java.util.List;

/**
 * Indicadores y conciliación de saldos (M09).
 *
 * <p>El cuadre se lee de la vista {@code vw_cuadre_billeteras}, que recalcula
 * cada saldo como recargas aprobadas + logros − movimientos (ADR-04). Es la
 * evidencia de CP-20: ninguna billetera debe aparecer descuadrada.</p>
 */
public class ReporteService extends ServicioBase {

    private final BilleteraDAO billeteraDAO;

    public ReporteService() {
        this(new BilleteraDAO());
    }

    public ReporteService(BilleteraDAO billeteraDAO) {
        this.billeteraDAO = billeteraDAO;
    }

    public List<CuadreBilletera> cuadreBilleteras(Usuario actor) {
        Permisos.exigir(actor, Accion.VER_CUADRE_BILLETERAS);
        return consultar(() -> billeteraDAO.listarCuadre());
    }

    /** Billeteras cuyo saldo almacenado no coincide con el recalculado. */
    public List<CuadreBilletera> descuadres(Usuario actor) {
        return cuadreBilleteras(actor).stream().filter(c -> !c.cuadra()).toList();
    }
}
