package com.mycompany.escuelavirtual.util.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Diálogos modales del sistema: confirmación de operaciones que mueven saldo,
 * errores y el aviso de falta de conexión con SQL Server.
 */
public final class DialogoConfirmacion {

    private DialogoConfirmacion() {
        // Clase de utilidad.
    }

    /**
     * Pregunta antes de una operación que modifica saldo o stock.
     *
     * @return {@code true} solo si el usuario elige «Sí»
     */
    public static boolean confirmar(Component padre, String titulo, String mensaje) {
        int opcion = JOptionPane.showOptionDialog(
                padre, mensaje, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[] {"Sí, continuar", "Cancelar"}, "Cancelar");
        return opcion == JOptionPane.YES_OPTION;
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void informar(Component padre, String titulo, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Aviso con los pasos concretos para recuperar la conexión a la base. */
    public static void errorConexion(Component padre, String detalle) {
        String mensaje = """
                No se pudo conectar con la base de datos EscuelaVirtual.

                Revise, en este orden:
                  1. Que el servicio de SQL Server esté iniciado.
                  2. Que el puerto 1433 esté abierto y no lo bloquee el firewall.
                  3. Que el archivo .env tenga DB_URL, DB_USER y DB_PASSWORD correctos.
                  4. Que el nombre de la instancia y de la base sean los del .env.

                Detalle técnico: """ + (detalle == null ? "sin detalle" : detalle);
        JOptionPane.showMessageDialog(padre, mensaje, "Sin conexión", JOptionPane.ERROR_MESSAGE);
    }
}
