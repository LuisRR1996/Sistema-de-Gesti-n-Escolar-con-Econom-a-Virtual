package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.LoginController;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.session.SessionManager;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Ventana de inicio de sesión. Al autenticar abre {@link MainFrame} y se cierra
 * a sí misma para no dejar ventanas ni hilos vivos.
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final SessionManager sesion;

    public LoginFrame(SessionManager sesion) {
        super("Escuela Virtual · Iniciar sesión");
        this.sesion = sesion;

        PanelLogin panel = new PanelLogin(new LoginController(sesion), this::alAutenticar);
        panel.setBackground(Estilos.FONDO);
        setContentPane(panel);
        getRootPane().setDefaultButton(panel.getBotonIngresar());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(480, 360));
        pack();
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(panel::enfocarPrimerCampo);
    }

    private void alAutenticar(Usuario usuario) {
        MainFrame principal = new MainFrame(sesion);
        principal.setVisible(true);
        dispose();
    }
}
