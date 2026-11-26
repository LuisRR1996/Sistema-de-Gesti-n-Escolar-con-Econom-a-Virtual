package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.LoginController;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.util.CodigoError;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.Validador;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelFormularioBase;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Pantalla de inicio de sesión (M01). Primera pantalla construida sobre
 * {@link PanelFormularioBase}.
 */
public class PanelLogin extends PanelFormularioBase {

    private static final long serialVersionUID = 1L;

    private final JTextField correo = agregarCampo("Correo:", new JTextField());
    private final JPasswordField clave = agregarCampo("Contraseña:", new JPasswordField());
    private final JCheckBox mostrarClave = agregarFila(new JCheckBox("Mostrar contraseña"));
    private final JButton ingresar = agregarBoton(Estilos.botonPrimario("Ingresar"));

    private final LoginController controlador;
    private final Consumer<Usuario> alAutenticar;
    private final char echoPorDefecto = clave.getEchoChar();

    public PanelLogin(LoginController controlador, Consumer<Usuario> alAutenticar) {
        super("Escuela Virtual", "Ingrese sus credenciales para continuar");
        this.controlador = controlador;
        this.alAutenticar = alAutenticar;

        mostrarClave.setOpaque(false);
        mostrarClave.setFont(Estilos.FUENTE_PEQUENA);
        mostrarClave.setForeground(Estilos.TEXTO);
        mostrarClave.addActionListener(e ->
                clave.setEchoChar(mostrarClave.isSelected() ? (char) 0 : echoPorDefecto));

        ingresar.addActionListener(e -> enviar());
        correo.addActionListener(e -> enviar());   // Enter envía el formulario
        clave.addActionListener(e -> enviar());
    }

    /** Botón que debe activar la tecla Enter en toda la ventana. */
    public JButton getBotonIngresar() {
        return ingresar;
    }

    public void enfocarPrimerCampo() {
        correo.requestFocusInWindow();
    }

    private void enviar() {
        limpiarErrores();
        if (!validarCampos()) {
            return;
        }
        char[] secreto = clave.getPassword();
        bloquear(true);
        try {
            controlador.autenticar(correo.getText().trim(), new String(secreto),
                    this::alTenerExito, this::alFallar);
        } finally {
            Arrays.fill(secreto, '\0');
        }
    }

    private boolean validarCampos() {
        boolean valido = true;
        if (!Validador.esCorreoValido(correo.getText())) {
            mostrarError(correo, "Correo no válido. Ejemplo: nombre@colegio.edu");
            valido = false;
        }
        if (clave.getPassword().length == 0) {
            mostrarError(clave, "La contraseña es obligatoria");
            valido = false;
        }
        return valido;
    }

    private void alTenerExito(Usuario usuario) {
        bloquear(false);
        clave.setText("");
        alAutenticar.accept(usuario);
    }

    private void alFallar(NegocioException error) {
        bloquear(false);
        clave.setText("");
        if (error.getCodigo() == CodigoError.SIN_CONEXION) {
            DialogoConfirmacion.errorConexion(this,
                    error.getCause() == null ? null : error.getCause().getMessage());
            return;
        }
        mostrarError(error.getMessage());
        if (error.getCodigo() == CodigoError.CREDENCIALES_INVALIDAS) {
            clave.requestFocusInWindow();
        }
    }
}
