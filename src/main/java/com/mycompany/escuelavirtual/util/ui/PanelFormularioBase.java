package com.mycompany.escuelavirtual.util.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * Base de todas las pantallas con formulario: cabecera, campos en
 * {@link GridBagLayout} y pie de botones.
 *
 * <p>Los errores se muestran debajo del campo que los provoca, tal como pide
 * el plan; {@link #mostrarError(String)} queda para los errores generales de la
 * pantalla. El panel no conoce servicios ni SQL: solo muestra lo que el
 * controlador le entrega.</p>
 */
public class PanelFormularioBase extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JPanel campos = new JPanel(new GridBagLayout());
    private final JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO, 0));
    private final JLabel errorGeneral = Estilos.etiquetaError();
    private final Map<JComponent, JLabel> erroresPorCampo = new LinkedHashMap<>();
    private final List<JComponent> editables = new ArrayList<>();
    private final List<JButton> botones = new ArrayList<>();
    private int fila;

    public PanelFormularioBase(String titulo, String subtitulo) {
        super(new BorderLayout(0, Estilos.ESPACIO_GRANDE));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        add(crearCabecera(titulo, subtitulo), BorderLayout.NORTH);

        campos.setOpaque(false);
        JPanel centro = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        centro.setBackground(Estilos.FONDO_PANEL);
        centro.setBorder(Estilos.tarjeta());
        centro.add(campos, BorderLayout.CENTER);
        errorGeneral.setBorder(BorderFactory.createEmptyBorder(Estilos.ESPACIO, 0, 0, 0));
        centro.add(errorGeneral, BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);

        pie.setOpaque(false);
        add(pie, BorderLayout.SOUTH);
    }

    private JPanel crearCabecera(String titulo, String subtitulo) {
        JPanel cabecera = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cabecera.setOpaque(false);
        cabecera.add(Estilos.titulo(titulo), BorderLayout.NORTH);
        if (subtitulo != null && !subtitulo.isBlank()) {
            cabecera.add(Estilos.subtitulo(subtitulo), BorderLayout.CENTER);
        }
        return cabecera;
    }

    /**
     * Añade una fila «etiqueta + campo» y reserva el espacio del mensaje de
     * error justo debajo del campo.
     */
    protected <T extends JComponent> T agregarCampo(String etiqueta, T campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(Estilos.ESPACIO_CHICO, 0, 0, Estilos.ESPACIO);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = fila;
        campos.add(Estilos.etiqueta(etiqueta), c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        campos.add(Estilos.campo(campo), c);

        JLabel error = Estilos.etiquetaError();
        c.gridx = 1;
        c.gridy = ++fila;
        c.insets = new Insets(0, 0, 0, Estilos.ESPACIO);
        campos.add(error, c);

        erroresPorCampo.put(campo, error);
        editables.add(campo);
        fila++;
        return campo;
    }

    /** Añade un componente que ocupa las dos columnas (casillas, enlaces...). */
    protected <T extends JComponent> T agregarFila(T componente) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(Estilos.ESPACIO_CHICO, 0, 0, Estilos.ESPACIO);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 1;
        c.gridy = fila++;
        campos.add(componente, c);
        return componente;
    }

    protected JButton agregarBoton(JButton boton) {
        pie.add(boton);
        botones.add(boton);
        return boton;
    }

    /** Muestra un error general de la pantalla (credenciales, conexión...). */
    public void mostrarError(String mensaje) {
        errorGeneral.setText(mensaje == null || mensaje.isBlank() ? " " : mensaje);
    }

    /** Muestra el error junto al campo y lo resalta. */
    public void mostrarError(JComponent campo, String mensaje) {
        JLabel error = erroresPorCampo.get(campo);
        if (error != null) {
            error.setText(mensaje == null || mensaje.isBlank() ? " " : mensaje);
            error.setToolTipText(mensaje); // La ventana ya está dimensionada: si el texto no cabe, se ve aquí.
        }
        Estilos.marcarError(campo, mensaje != null && !mensaje.isBlank());
    }

    /** Borra todos los mensajes de error y los bordes rojos. */
    public void limpiarErrores() {
        errorGeneral.setText(" ");
        erroresPorCampo.forEach((campo, error) -> {
            error.setText(" ");
            Estilos.marcarError(campo, false);
        });
    }

    /** Vacía los campos de texto y borra los errores. */
    public void limpiar() {
        limpiarErrores();
        for (JComponent campo : editables) {
            if (campo instanceof JTextComponent texto) {
                texto.setText("");
            }
        }
    }

    /**
     * Bloquea campos y botones mientras hay una operación en curso, para que no
     * se dispare dos veces desde el EDT.
     */
    public void bloquear(boolean bloqueado) {
        for (JComponent campo : editables) {
            campo.setEnabled(!bloqueado);
        }
        for (JButton boton : botones) {
            boton.setEnabled(!bloqueado);
        }
        setCursor(java.awt.Cursor.getPredefinedCursor(
                bloqueado ? java.awt.Cursor.WAIT_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
    }

    /** Permite a las subclases añadir componentes al pie (enlaces, ayuda). */
    protected void agregarAlPie(Component componente) {
        pie.add(componente, 0);
    }
}
