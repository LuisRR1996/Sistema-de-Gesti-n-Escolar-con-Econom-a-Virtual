package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.dao.LogroDAO;
import com.mycompany.escuelavirtual.dao.MovimientoDAO;
import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.DetalleMovimiento;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.Permisos;
import java.util.List;

/**
 * Consulta de los dos saldos, del historial y de los logros (M03).
 *
 * <p>Los dos saldos son independientes y no se convierten entre sí (RN-01).
 * Cada consulta autoriza al actor contra el estudiante pedido: el estudiante
 * solo ve lo suyo, el padre solo a sus hijos —vínculo verificado en la base de
 * datos— y el administrador ve todo (RN-08).</p>
 */
public class BilleteraService extends ServicioBase {

    private final BilleteraDAO billeteraDAO;
    private final MovimientoDAO movimientoDAO;
    private final LogroDAO logroDAO;
    private final UsuarioDAO usuarioDAO;

    public BilleteraService() {
        this(new BilleteraDAO(), new MovimientoDAO(), new LogroDAO(), new UsuarioDAO());
    }

    public BilleteraService(BilleteraDAO billeteraDAO, MovimientoDAO movimientoDAO,
            LogroDAO logroDAO, UsuarioDAO usuarioDAO) {
        this.billeteraDAO = billeteraDAO;
        this.movimientoDAO = movimientoDAO;
        this.logroDAO = logroDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public Billetera consultarSaldo(Usuario actor, int idEstudiante) {
        exigirAcceso(actor, idEstudiante);
        return consultar(() -> billeteraDAO.obtenerOCrearPorEstudiante(idEstudiante));
    }

    public List<DetalleMovimiento> listarMovimientos(Usuario actor, int idEstudiante) {
        exigirAcceso(actor, idEstudiante);
        return consultar(() -> movimientoDAO.listarPorEstudiante(idEstudiante));
    }

    public List<DetalleLogro> listarLogros(Usuario actor, int idEstudiante) {
        exigirAcceso(actor, idEstudiante);
        return consultar(() -> logroDAO.listarPorEstudiante(idEstudiante));
    }

    /**
     * Resuelve el vínculo padre–hijo en la base de datos y delega la decisión
     * en {@link Permisos}. Es la comprobación que verifica CP-03.
     */
    private void exigirAcceso(Usuario actor, int idEstudiante) {
        boolean esSuHijo = actor != null && actor.getRol() == Rol.PADRE
                && consultar(() -> usuarioDAO.esHijoDe(actor.getIdUsuario(), idEstudiante));
        Permisos.exigirAccesoAEstudiante(actor, idEstudiante, esSuHijo);
    }
}
