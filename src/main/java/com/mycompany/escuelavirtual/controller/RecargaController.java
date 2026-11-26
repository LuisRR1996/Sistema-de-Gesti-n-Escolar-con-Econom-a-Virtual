package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.DetalleRecarga;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.MetodoPago;
import com.mycompany.escuelavirtual.service.RecargaService;
import java.math.BigDecimal;
import java.util.List;

/** Registro y resolución de recargas (M04). */
public class RecargaController {

    private final RecargaService recargaService;
    private final Usuario actor;

    public RecargaController(Usuario actor) {
        this(new RecargaService(), actor);
    }

    public RecargaController(RecargaService recargaService, Usuario actor) {
        this.recargaService = recargaService;
        this.actor = actor;
    }

    public void registrar(int idEstudiante, BigDecimal monto, MetodoPago metodo) {
        recargaService.registrar(actor, idEstudiante, monto, metodo);
    }

    public void aprobar(int idRecarga) {
        recargaService.aprobar(actor, idRecarga);
    }

    public void rechazar(int idRecarga, String motivo) {
        recargaService.rechazar(actor, idRecarga, motivo);
    }

    public List<DetalleRecarga> listarTodas() {
        return recargaService.listarTodas(actor);
    }
}
