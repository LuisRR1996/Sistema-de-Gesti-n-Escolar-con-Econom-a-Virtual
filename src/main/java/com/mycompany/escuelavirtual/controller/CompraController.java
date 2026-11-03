package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.CompraService;

/** Compra con saldo de dinero (M05). */
public class CompraController {

    private final CompraService compraService;
    private final Usuario actor;

    public CompraController(Usuario actor) {
        this(new CompraService(), actor);
    }

    public CompraController(CompraService compraService, Usuario actor) {
        this.compraService = compraService;
        this.actor = actor;
    }

    public void comprar(int idProducto, int cantidad) {
        compraService.comprar(actor, idProducto, cantidad);
    }
}
