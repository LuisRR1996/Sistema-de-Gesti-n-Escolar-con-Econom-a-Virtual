package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.util.Modulo;
import com.mycompany.escuelavirtual.util.ui.Estilos;

import java.awt.BorderLayout;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Panel prototipo de un módulo cuya pantalla propia aún no existe.
 *
 * <p>Cubre el entregable «9 paneles prototipo» del criterio de salida de la
 * Etapa 1: el árbol de navegación muestra los módulos del anexo C y cada nodo
 * abre una pantalla que nombra el módulo y la tarea del plan que lo
 * implementa. Las funciones ya construidas en la Etapa 1 viven en el panel de
 * inicio (M01), y a él remite este texto.</p>
 */
public class PanelModuloPrototipo extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Tarea del plan de desarrollo que construye la pantalla de cada módulo. */
    private static final Map<Modulo, String> TAREA = Map.of(
            Modulo.M02, "T10 · ABU de usuarios",
            Modulo.M03, "S01 · Billetera y movimientos",
            Modulo.M04, "S02 · Recargas y aprobación",
            Modulo.M05, "S03 · Tienda, compra y transacción",
            Modulo.M06, "S04 · Logros",
            Modulo.M07, "S04 · Catálogo y canjes",
            Modulo.M08, "S05 · Supervisión",
            Modulo.M09, "S06 · Reportes y CSV");

    public PanelModuloPrototipo(Modulo modulo) {
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Estilos.FONDO_PANEL);
        tarjeta.setBorder(Estilos.tarjeta());
        tarjeta.add(Estilos.titulo(modulo.getEtiqueta()));
        tarjeta.add(Estilos.subtitulo("Pantalla prototipo. Se implementa en "
                + TAREA.getOrDefault(modulo, "una tarea posterior") + "."));
        tarjeta.add(Estilos.subtitulo(
                "Las funciones de este módulo ya disponibles se usan desde «"
                + Modulo.M01.getEtiqueta() + "»."));

        add(tarjeta, BorderLayout.NORTH);
    }
}
