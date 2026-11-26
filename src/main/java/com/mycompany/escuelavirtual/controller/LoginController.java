package com.mycompany.escuelavirtual.controller;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.service.AuthService;
import com.mycompany.escuelavirtual.session.SessionManager;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Coordina el inicio de sesión entre {@code PanelLogin} y {@link AuthService}.
 *
 * <p>La consulta se ejecuta fuera del hilo de interfaz con un
 * {@link SwingWorker}; los resultados se entregan ya en el EDT.</p>
 */
public class LoginController {

    private final AuthService authService;
    private final SessionManager sesion;

    public LoginController(SessionManager sesion) {
        this(new AuthService(), sesion);
    }

    public LoginController(AuthService authService, SessionManager sesion) {
        this.authService = authService;
        this.sesion = sesion;
    }

    /**
     * Autentica sin bloquear la interfaz. Si tiene éxito deja la sesión
     * iniciada y llama a {@code alAutenticar}; si falla llama a {@code alFallar}.
     */
    public void autenticar(String correo, String clave,
            Consumer<Usuario> alAutenticar, Consumer<NegocioException> alFallar) {

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() {
                return authService.login(correo, clave);
            }

            @Override
            protected void done() {
                try {
                    Usuario usuario = get();
                    sesion.cerrar(); // Una sola sesión por instancia.
                    sesion.iniciar(usuario);
                    alAutenticar.accept(usuario);
                } catch (ExecutionException e) {
                    alFallar.accept(traducir(e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alFallar.accept(new NegocioException(CodigoError.ERROR_INTERNO));
                }
            }
        }.execute();
    }

    private static NegocioException traducir(Throwable causa) {
        if (causa instanceof NegocioException negocio) {
            return negocio;
        }
        return new NegocioException(CodigoError.ERROR_INTERNO,
                CodigoError.ERROR_INTERNO.getMensaje(), causa);
    }
}
