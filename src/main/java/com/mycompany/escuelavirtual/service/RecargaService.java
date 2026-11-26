package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.dao.RecargaDAO;
import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.DetalleRecarga;
import com.mycompany.escuelavirtual.model.Recarga;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.EstadoRecarga;
import com.mycompany.escuelavirtual.model.enums.MetodoPago;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import com.mycompany.escuelavirtual.util.Validador;
import com.mycompany.escuelavirtual.util.db.Transaccion;
import java.math.BigDecimal;
import java.util.List;

/**
 * Recargas de saldo: registro por el padre y resolución por el administrador (M04).
 *
 * <p>La solicitud nace PENDIENTE y no cambia el saldo; solo la aprobación
 * acredita, y lo hace exactamente una vez porque la fila de la recarga se
 * bloquea y se revisa su estado dentro de la misma transacción (RN-06). El
 * rechazo no toca el saldo. Las recargas de dinero nunca otorgan monedas
 * (RN-02).</p>
 */
public class RecargaService extends ServicioBase {

    private final RecargaDAO recargaDAO;
    private final BilleteraDAO billeteraDAO;
    private final UsuarioDAO usuarioDAO;

    public RecargaService() {
        this(new RecargaDAO(), new BilleteraDAO(), new UsuarioDAO());
    }

    public RecargaService(RecargaDAO recargaDAO, BilleteraDAO billeteraDAO, UsuarioDAO usuarioDAO) {
        this.recargaDAO = recargaDAO;
        this.billeteraDAO = billeteraDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Registra la solicitud. Un padre solo puede recargar a sus hijos (RN-08).
     */
    public void registrar(Usuario actor, int idEstudiante, BigDecimal monto, MetodoPago metodo) {
        Permisos.exigir(actor, Accion.REGISTRAR_RECARGA);
        if (!Validador.esMontoDineroValido(monto)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El monto debe ser positivo y con máximo 2 decimales.");
        }
        if (metodo == null) {
            throw new NegocioException(CodigoError.DATO_INVALIDO, "Seleccione el método de pago.");
        }

        // --- VALIDACIÓN 1: Límites máximos según método de pago ---
        BigDecimal limiteMaximo = switch (metodo) {
            case YAPE, PLIN ->
                new BigDecimal("500.00");
            case EFECTIVO ->
                new BigDecimal("1000.00");
            case TRANSFERENCIA ->
                new BigDecimal("3000.00");
        };

        if (monto.compareTo(limiteMaximo) > 0) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "El monto máximo permitido para " + metodo + " es S/ " + limiteMaximo + ".");
        }
        // ----------------------------------------------------------

        // --- VALIDACIÓN 2: Límite diario de cantidad de recargas ---
        int recargasHoy = consultar(() -> recargaDAO.contarRecargasDelDiaPorPadre(actor.getIdUsuario()));
        if (recargasHoy >= 5) {
            throw new NegocioException(CodigoError.CONFLICTO,
                    "Ha alcanzado el límite máximo de 5 solicitudes de recarga por día.");
        }
        // -----------------------------------------------------------

        boolean esSuHijo = actor.getRol() == Rol.PADRE
                && consultar(() -> usuarioDAO.esHijoDe(actor.getIdUsuario(), idEstudiante));
        Permisos.exigirAccesoAEstudiante(actor, idEstudiante, esSuHijo);

        ejecutar(() -> Transaccion.ejecutar(cn -> {
            Billetera billetera = billeteraDAO.obtenerOCrear(cn, idEstudiante);
            recargaDAO.insertar(cn, billetera.getIdBilletera(), actor.getIdUsuario(), monto, metodo);
        }));
    }

    public void aprobar(Usuario actor, int idRecarga) {
        Permisos.exigir(actor, Accion.RESOLVER_RECARGA);
        ejecutar(() -> Transaccion.ejecutar(cn -> {
            Recarga recarga = exigirPendiente(cn, idRecarga);
            recargaDAO.aprobar(cn, idRecarga, actor.getIdUsuario());
            billeteraDAO.acreditarDinero(cn, recarga.getIdBilletera(), recarga.getMonto());
        }));
    }

    public void rechazar(Usuario actor, int idRecarga, String motivo) {
        Permisos.exigir(actor, Accion.RESOLVER_RECARGA);
        ejecutar(() -> Transaccion.ejecutar(cn -> {
            exigirPendiente(cn, idRecarga);
            recargaDAO.rechazar(cn, idRecarga, actor.getIdUsuario(),
                    Validador.esTextoValido(motivo) ? motivo.trim() : "Rechazada por administración");
        }));
    }

    public List<DetalleRecarga> listarTodas(Usuario actor) {
        Permisos.exigir(actor, Accion.RESOLVER_RECARGA);
        return consultar(() -> recargaDAO.listarTodas());
    }

    /**
     * Bloquea la recarga y comprueba que siga PENDIENTE: es lo que impide la
     * doble acreditación cuando dos administradores aprueban a la vez.
     */
    private Recarga exigirPendiente(java.sql.Connection cn, int idRecarga)
            throws java.sql.SQLException {
        Recarga recarga = recargaDAO.bloquearPorId(cn, idRecarga);
        if (recarga == null) {
            throw new NegocioException(CodigoError.NO_ENCONTRADO, "La recarga no existe.");
        }
        if (recarga.getEstado() != EstadoRecarga.PENDIENTE) {
            throw new NegocioException(CodigoError.ESTADO_INVALIDO,
                    "La recarga ya fue " + recarga.getEstado() + ": no se puede volver a resolver.");
        }
        return recarga;
    }
}
