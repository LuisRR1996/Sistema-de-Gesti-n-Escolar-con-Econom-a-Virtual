package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.CanjeService;

/** Canje de recompensas con monedas especiales (M07). */
public class CanjeController {

    private final CanjeService canjeService;
    private final Usuario actor;

    public CanjeController(Usuario actor) {
        this(new CanjeService(), actor);
    }

    public CanjeController(CanjeService canjeService, Usuario actor) {
        this.canjeService = canjeService;
        this.actor = actor;
    }

    public void canjear(int idProducto, int cantidad) {
        canjeService.canjear(actor, idProducto, cantidad);
    }
}
