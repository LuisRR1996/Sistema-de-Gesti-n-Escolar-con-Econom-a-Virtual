package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.CuadreBilletera;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.ReporteService;
import java.util.List;

/** Indicadores y conciliación de saldos (M09). */
public class ReporteController {

    private final ReporteService reporteService;
    private final Usuario actor;

    public ReporteController(Usuario actor) {
        this(new ReporteService(), actor);
    }

    public ReporteController(ReporteService reporteService, Usuario actor) {
        this.reporteService = reporteService;
        this.actor = actor;
    }

    public List<CuadreBilletera> descuadres() {
        return reporteService.descuadres(actor);
    }
}
