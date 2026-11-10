package com.mycompany.escuelavirtual.service;

import com.mycompany.escuelavirtual.dao.UsuarioDAO;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.PasswordUtil;
import com.mycompany.escuelavirtual.util.Validador;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autenticación contra SQL Server (M01).
 *
 * <p>Tras {@value #MAX_INTENTOS} intentos fallidos la cuenta queda bloqueada
 * {@value #MINUTOS_BLOQUEO} minutos; un acceso correcto reinicia el contador y
 * actualiza {@code ultimo_acceso}. La contraseña nunca se registra en el log.</p>
 */
public class AuthService {

    public static final int MAX_INTENTOS = 3;
    public static final int MINUTOS_BLOQUEO = 15;

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this(new UsuarioDAO());
    }

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Valida las credenciales y devuelve el usuario autenticado.
     *
     * @throws NegocioException con {@link CodigoError#CREDENCIALES_INVALIDAS},
     *                          {@link CodigoError#CUENTA_BLOQUEADA},
     *                          {@link CodigoError#CUENTA_INACTIVA} o
     *                          {@link CodigoError#SIN_CONEXION}
     */
    public Usuario login(String correo, String clave) {
        if (!Validador.esCorreoValido(correo) || clave == null || clave.isBlank()) {
            throw new NegocioException(CodigoError.CREDENCIALES_INVALIDAS);
        }
        try {
            Usuario usuario = usuarioDAO.findByCorreo(Validador.normalizarCorreo(correo));
            if (usuario == null) {
                // Mismo mensaje que una clave incorrecta: no revelar qué correos existen.
                throw new NegocioException(CodigoError.CREDENCIALES_INVALIDAS);
            }

            LocalDateTime ahora = usuarioDAO.ahoraEnServidor();
            if (usuario.estaBloqueado(ahora)) {
                throw bloqueada(usuario.getBloqueadoHasta(), ahora);
            }
            if (!usuario.isEstado()) {
                throw new NegocioException(CodigoError.CUENTA_INACTIVA);
            }

            if (!PasswordUtil.verificar(clave, usuario.getPasswordHash())) {
                boolean quedoBloqueada = usuarioDAO.registrarIntentoFallido(
                        usuario.getIdUsuario(), MAX_INTENTOS, MINUTOS_BLOQUEO);
                LOG.log(Level.INFO, "Intento de acceso fallido para el usuario {0}", usuario.getIdUsuario());
                if (quedoBloqueada) {
                    throw new NegocioException(CodigoError.CUENTA_BLOQUEADA,
                            "Cuenta bloqueada por " + MINUTOS_BLOQUEO
                            + " minutos tras " + MAX_INTENTOS + " intentos fallidos.");
                }
                throw new NegocioException(CodigoError.CREDENCIALES_INVALIDAS);
            }

            usuarioDAO.registrarAccesoExitoso(usuario.getIdUsuario());
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuario.setUltimoAcceso(ahora);
            LOG.log(Level.INFO, "Acceso correcto del usuario {0}", usuario.getIdUsuario());
            return usuario;

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos durante el login", e);
            throw new NegocioException(CodigoError.SIN_CONEXION, CodigoError.SIN_CONEXION.getMensaje(), e);
        }
    }

    private static NegocioException bloqueada(LocalDateTime hasta, LocalDateTime ahora) {
        long minutos = Math.max(1, Duration.between(ahora, hasta).toMinutes() + 1);
        return new NegocioException(CodigoError.CUENTA_BLOQUEADA,
                "La cuenta está bloqueada. Vuelva a intentarlo en " + minutos + " minuto(s).");
    }
}
