package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.CatalogoService;
import java.util.List;

/** Catálogo de productos y recompensas (M05 y M07). */
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final Usuario actor;

    public CatalogoController(Usuario actor) {
        this(new CatalogoService(), actor);
    }

    public CatalogoController(CatalogoService catalogoService, Usuario actor) {
        this.catalogoService = catalogoService;
        this.actor = actor;
    }

    public List<Producto> listarActivos() {
        return catalogoService.listarActivos(actor);
    }

    public List<Producto> listarTodos() {
        return catalogoService.listarTodos(actor);
    }

    public Producto registrar(Producto producto) {
        return catalogoService.registrar(actor, producto);
    }

    public void actualizar(Producto producto) {
        catalogoService.actualizar(actor, producto);
    }

    public void ajustarStock(int idProducto, int nuevoStock) {
        catalogoService.ajustarStock(actor, idProducto, nuevoStock);
    }

    public void darDeBaja(int idProducto) {
        catalogoService.darDeBaja(actor, idProducto);
    }
}
