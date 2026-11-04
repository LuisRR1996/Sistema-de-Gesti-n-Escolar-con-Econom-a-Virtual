package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.dao.LogroDAO;
import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.Permisos;
import com.mycompany.escuelavirtual.util.Permisos.Accion;
import com.mycompany.escuelavirtual.util.Validador;
import com.mycompany.escuelavirtual.util.db.Transaccion;
import java.util.List;

/**
 * Registro de logros y abono de monedas especiales (M06).
 *
 * <p>Solo un docente o un administrador puede registrar un logro, de modo que
 * un estudiante no pueda asignarse monedas (RN-09). El logro queda atribuido a
 * quien lo otorgó de verdad (RN-14) y el abono de monedas va en la misma
 * transacción que el logro (RN-05); las recargas de dinero no otorgan monedas
 * y este abono no toca el saldo de dinero (RN-02).</p>
 */
public class LogroService extends ServicioBase {

    private final LogroDAO logroDAO;
    private final BilleteraDAO billeteraDAO;
    private final UsuarioDAO usuarioDAO;

    public LogroService() {
        this(new LogroDAO(), new BilleteraDAO(), new UsuarioDAO());
    }

    public LogroService(LogroDAO logroDAO, BilleteraDAO billeteraDAO, UsuarioDAO usuarioDAO) {
        this.logroDAO = logroDAO;
        this.billeteraDAO = billeteraDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public void registrar(Usuario actor, int idEstudiante, String tipo, String descripcion, int monedas) {
        Permisos.exigir(actor, Accion.REGISTRAR_LOGRO);
        if (!Validador.esTextoValido(tipo, 100)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO, "Indique la categoría del mérito.");
        }
        if (!Validador.esTextoValido(descripcion, 255)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "Ingrese una breve descripción del mérito.");
        }
        if (!Validador.esMonedasValidas(monedas)) {
            throw new NegocioException(CodigoError.DATO_INVALIDO,
                    "Las monedas a otorgar deben ser un entero positivo.");
        }

        // La base de datos solo exige que el identificador exista: el rol se verifica aquí.
        Usuario estudiante = consultar(() -> usuarioDAO.buscarPorId(idEstudiante));
        if (estudiante == null || estudiante.getRol() != Rol.ESTUDIANTE) {
            throw new NegocioException(CodigoError.NO_ENCONTRADO,
                    "El destinatario del logro no es un estudiante registrado.");
        }
        if (!estudiante.isEstado()) {
            throw new NegocioException(CodigoError.CONFLICTO,
                    "El estudiante está inactivo: no se le pueden otorgar monedas.");
        }

        ejecutar(() -> Transaccion.ejecutar(cn -> {
            Billetera billetera = billeteraDAO.bloquearPorEstudiante(cn, idEstudiante);
            int idBilletera = billetera != null
                    ? billetera.getIdBilletera()
                    : billeteraDAO.crear(cn, idEstudiante).getIdBilletera();
            logroDAO.insertar(cn, idEstudiante, actor.getIdUsuario(), tipo.trim(), descripcion.trim(), monedas);
            billeteraDAO.acreditarMonedas(cn, idBilletera, monedas);
        }));
    }

    /** Logros que registró el propio actor: es la vista del docente sobre M06. */
    public List<DetalleLogro> listarOtorgadosPor(Usuario actor) {
        Permisos.exigir(actor, Accion.VER_LOGROS_OTORGADOS);
        return consultar(() -> logroDAO.listarPorOtorgante(actor.getIdUsuario()));
    }
}
