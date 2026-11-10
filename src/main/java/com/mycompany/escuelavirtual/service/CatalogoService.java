package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.ProductoDAO;
import com.mycompany.escuelavirtual.model.Producto;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import com.mycompany.escuelavirtual.util.Validador;
import java.util.List;

/**
 * Catálogo de productos y recompensas (M05 y M07).
 *
 * <p>Solo el administrador lo mantiene; cualquier usuario autenticado puede
 * ver los productos activos para comprar o canjear.</p>
 */
public class CatalogoService extends ServicioBase {

    private final ProductoDAO productoDAO;

    public CatalogoService() {
        this(new ProductoDAO());
    }

    public CatalogoService(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
    }

    /** Productos disponibles para la tienda. */
    public List<Producto> listarActivos(Usuario actor) {
        exigirSesion(actor);
        return consultar(() -> productoDAO.listarActivos());
    }

    public List<Producto> listarTodos(Usuario actor) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_CATALOGO);
        return consultar(() -> productoDAO.listarTodos());
    }

    public Producto registrar(Usuario actor, Producto producto) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_CATALOGO);
        validar(producto);
        return consultar(() -> productoDAO.insertar(producto));
    }

    public void actualizar(Usuario actor, Producto producto) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_CATALOGO);
        if (!Validador.esIdValido(producto == null ? null : producto.getIdProducto())) {
            throw new NegocioException(CodigoError.DATO_INVALIDO);
        }
        validar(producto);
        ejecutar(() -> productoDAO.actualizar(producto));
    }

    /** Ajuste manual de inventario: reposición o merma registrada por el administrador. */
    public void ajustarStock(Usuario actor, int idProducto, int nuevoStock) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_CATALOGO);
        if (!Validador.esIdValido(idProducto) || nuevoStock < 0) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El stock no puede ser negativo.");
        }
        ejecutar(() -> productoDAO.actualizarStock(idProducto, nuevoStock));
    }

    public void darDeBaja(Usuario actor, int idProducto) {
        Permisos.exigir(actor, Accion.ADMINISTRAR_CATALOGO);
        ejecutar(() -> productoDAO.cambiarEstado(idProducto, false));
    }

    private void validar(Producto producto) {
        if (producto == null || !Validador.esTextoValido(producto.getNombre(), 150)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El nombre del producto es obligatorio.");
        }
        if (producto.getPrecioDinero() == null && producto.getPrecioMonedas() == null) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "Defina al menos un precio: soles o monedas.");
        }
        if (producto.getPrecioDinero() != null
                && !Validador.esMontoDineroValido(producto.getPrecioDinero())) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El precio en soles debe ser positivo y con máximo 2 decimales.");
        }
        if (producto.getPrecioMonedas() != null
                && !Validador.esMonedasValidas(producto.getPrecioMonedas())) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El precio en monedas debe ser un entero positivo.");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El stock no puede ser negativo.");
        }
    }

    private static void exigirSesion(Usuario actor) {
        if (actor == null || actor.getRol() == null) {
            throw new NegocioException(CodigoError.PERMISO_DENEGADO);
        }
    }
}
