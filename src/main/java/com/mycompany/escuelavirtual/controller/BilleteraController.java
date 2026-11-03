package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.DetalleMovimiento;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.BilleteraService;
import java.util.List;

/**
 * Consulta de saldos, movimientos y logros (M03). El servicio verifica que el
 * actor pueda ver al estudiante pedido (RN-08).
 */
public class BilleteraController {

    private final BilleteraService billeteraService;
    private final Usuario actor;

    public BilleteraController(Usuario actor) {
        this(new BilleteraService(), actor);
    }

    public BilleteraController(BilleteraService billeteraService, Usuario actor) {
        this.billeteraService = billeteraService;
        this.actor = actor;
    }

    public Billetera consultarSaldo(int idEstudiante) {
        return billeteraService.consultarSaldo(actor, idEstudiante);
    }

    /** Saldo del propio estudiante conectado. */
    public Billetera consultarSaldoPropio() {
        return billeteraService.consultarSaldo(actor, actor.getIdUsuario());
    }

    public List<DetalleMovimiento> listarMovimientos(int idEstudiante) {
        return billeteraService.listarMovimientos(actor, idEstudiante);
    }

    public List<DetalleLogro> listarLogros(int idEstudiante) {
        return billeteraService.listarLogros(actor, idEstudiante);
    }
}
