package com.mycompany.escuelavirtual.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

/**
 * Paleta, tipografías, bordes y espaciados únicos de la aplicación.
 *
 * <p>Ningún panel define colores ni fuentes por su cuenta: todos los toman de
 * aquí para que las nueve pantallas se vean iguales. Los tamaños están pensados
 * para que la ventana funcione desde 1366x768.</p>
 */
public final class Estilos {

    // --- Colores ---
    public static final Color PRIMARIO = new Color(0x1F3A93);
    public static final Color PRIMARIO_OSCURO = new Color(0x16296B);
    public static final Color ACENTO = new Color(0x2E9E5B);
    public static final Color ERROR = new Color(0xC0392B);
    public static final Color ADVERTENCIA = new Color(0xB9770E);
    public static final Color TEXTO = new Color(0x1B1B1B);
    public static final Color TEXTO_SUAVE = new Color(0x5A5A5A);
    public static final Color FONDO = new Color(0xF4F6FA);
    public static final Color FONDO_PANEL = Color.WHITE;
    public static final Color BORDE = new Color(0xD3D8E0);
    public static final Color FILA_ALTERNA = new Color(0xF7F9FC);

    // --- Tipografías ---
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_NORMAL = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FUENTE_NEGRITA = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 12);

    // --- Espaciados y tamaños ---
    public static final int ESPACIO_CHICO = 4;
    public static final int ESPACIO = 8;
    public static final int ESPACIO_GRANDE = 16;
    public static final int ALTO_CAMPO = 30;
    public static final int ALTO_FILA_TABLA = 26;

    private Estilos() {
        // Clase de utilidad.
    }

    /** Margen interior uniforme para paneles. */
    public static Border margen() {
        return BorderFactory.createEmptyBorder(ESPACIO_GRANDE, ESPACIO_GRANDE, ESPACIO_GRANDE, ESPACIO_GRANDE);
    }

    /** Caja blanca con borde fino, usada por tarjetas y formularios. */
    public static Border tarjeta() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(ESPACIO_GRANDE, ESPACIO_GRANDE, ESPACIO_GRANDE, ESPACIO_GRANDE));
    }

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_TITULO);
        etiqueta.setForeground(PRIMARIO);
        return etiqueta;
    }

    public static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_SUBTITULO);
        etiqueta.setForeground(TEXTO_SUAVE);
        return etiqueta;
    }

    public static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(FUENTE_NORMAL);
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    /** Mensaje de error en rojo y tamaño pequeño, para colocar junto al campo. */
    public static JLabel etiquetaError() {
        JLabel etiqueta = new JLabel(" ");
        etiqueta.setFont(FUENTE_PEQUENA);
        etiqueta.setForeground(ERROR);
        return etiqueta;
    }

    public static JButton botonPrimario(String texto) {
        return pintarBoton(new JButton(texto), PRIMARIO, Color.WHITE);
    }

    public static JButton botonSecundario(String texto) {
        JButton boton = pintarBoton(new JButton(texto), FONDO_PANEL, PRIMARIO);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARIO),
                BorderFactory.createEmptyBorder(ESPACIO_CHICO, ESPACIO_GRANDE, ESPACIO_CHICO, ESPACIO_GRANDE)));
        return boton;
    }

    private static JButton pintarBoton(JButton boton, Color fondo, Color texto) {
        boton.setFont(FUENTE_NEGRITA);
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setBorder(BorderFactory.createEmptyBorder(
                ESPACIO_CHICO + 1, ESPACIO_GRANDE, ESPACIO_CHICO + 1, ESPACIO_GRANDE));
        return boton;
    }

    /** Aplica la fuente normal y un alto de campo consistente. */
    public static <T extends JComponent> T campo(T componente) {
        componente.setFont(FUENTE_NORMAL);
        if (componente instanceof JTextComponent texto) {
            texto.setBackground(FONDO_PANEL);
            texto.setForeground(TEXTO);
            texto.setCaretColor(TEXTO);
        }
        Dimension tamano = componente.getPreferredSize();
        componente.setPreferredSize(new Dimension(Math.max(tamano.width, 220), ALTO_CAMPO));
        return componente;
    }

    /** Marca o desmarca un campo con el borde rojo de error. */
    public static void marcarError(JComponent campo, boolean conError) {
        campo.setBorder(conError
                ? BorderFactory.createLineBorder(ERROR)
                : UIManager.getBorder("TextField.border"));
    }
}
