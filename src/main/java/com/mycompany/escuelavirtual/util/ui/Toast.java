package com.mycompany.escuelavirtual.util.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Aviso no modal que aparece abajo a la derecha de la ventana y se cierra solo.
 *
 * <p>Se usa para confirmar operaciones (una compra, una recarga aprobada) sin
 * obligar al usuario a pulsar «Aceptar». Para preguntas o errores que exigen
 * atención se usa {@link DialogoConfirmacion}.</p>
 */
public final class Toast {

    private static final int DURACION_MS = 3000;
    private static final int MARGEN = 24;

    private Toast() {
        // Clase de utilidad.
    }

    public static void exito(Component origen, String mensaje) {
        mostrar(origen, mensaje, Estilos.ACENTO);
    }

    public static void info(Component origen, String mensaje) {
        mostrar(origen, mensaje, Estilos.PRIMARIO);
    }

    public static void error(Component origen, String mensaje) {
        mostrar(origen, mensaje, Estilos.ERROR);
    }

    private static void mostrar(Component origen, String mensaje, Color fondo) {
        Window padre = origen == null ? null : SwingUtilities.getWindowAncestor(origen);
        if (padre == null || !padre.isShowing()) {
            return; // Sin ventana visible no hay dónde anclarlo; no es un error.
        }

        JWindow aviso = new JWindow(padre);
        JLabel texto = new JLabel(mensaje);
        texto.setFont(Estilos.FUENTE_NEGRITA);
        texto.setForeground(Color.WHITE);

        JPanel caja = new JPanel(new BorderLayout());
        caja.setBackground(fondo);
        caja.setBorder(BorderFactory.createEmptyBorder(
                Estilos.ESPACIO, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO, Estilos.ESPACIO_GRANDE));
        caja.add(texto, BorderLayout.CENTER);

        aviso.setContentPane(caja);
        aviso.pack();

        Point esquina = padre.getLocationOnScreen();
        aviso.setLocation(
                esquina.x + padre.getWidth() - aviso.getWidth() - MARGEN,
                esquina.y + padre.getHeight() - aviso.getHeight() - MARGEN);
        aviso.setVisible(true);

        Timer temporizador = new Timer(DURACION_MS, e -> aviso.dispose());
        temporizador.setRepeats(false);
        temporizador.start();
    }
}
