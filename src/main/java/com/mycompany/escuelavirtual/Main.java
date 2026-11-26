package com.mycompany.escuelavirtual;

import com.mycompany.escuelavirtual.session.SessionManager;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.db.DatabaseConnection;
import com.mycompany.escuelavirtual.view.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * Arranque de la aplicación.
 *
 * <p>Se usa el aspecto multiplataforma de Swing, no el del sistema: la paleta
 * propia de {@code Estilos} supone fondo claro y un tema oscuro del escritorio
 * dejaría los campos y las etiquetas ilegibles.</p>
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (!DatabaseConnection.isAvailable()) {
                DialogoConfirmacion.errorConexion(null, "No responde el servidor configurado en DB_URL.");
                return;
            }
            new LoginFrame(new SessionManager()).setVisible(true);
        });
    }
}
