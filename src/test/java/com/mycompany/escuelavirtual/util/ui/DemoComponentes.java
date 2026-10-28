package com.mycompany.escuelavirtual.util.ui;

import com.mycompany.escuelavirtual.util.Formato;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Segunda pantalla de prueba de los componentes base de T07 (la primera es
 * {@code PanelLogin}). Muestra {@link PanelTablaBase} con datos de ejemplo,
 * junto a {@link Toast} y {@link DialogoConfirmacion}.
 *
 * <p>Vive en {@code src/test} porque es una ayuda de desarrollo: no se empaqueta
 * con la aplicación. Se ejecuta con
 * {@code mvn test-compile exec:java -Dexec.classpathScope=test
 * -Dexec.mainClass=com.mycompany.escuelavirtual.util.ui.DemoComponentes}.</p>
 */
public final class DemoComponentes {

    /** Fila de ejemplo con la forma de un movimiento de billetera. */
    private record Movimiento(LocalDateTime fecha, String concepto, String metodo, BigDecimal monto) {
    }

    private DemoComponentes() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DemoComponentes::mostrar);
    }

    private static void mostrar() {
        PanelTablaBase<Movimiento> tabla = new PanelTablaBase<>(
                new String[] {"Fecha", "Concepto", "Método", "Monto"},
                m -> new Object[] {
                    Formato.fechaHora(m.fecha()), m.concepto(), m.metodo(), Formato.dinero(m.monto())});
        tabla.setColumnaFecha(0);
        tabla.setDatos(datosDeEjemplo());

        JPanel contenido = new JPanel(new BorderLayout(0, Estilos.ESPACIO));
        contenido.setBackground(Estilos.FONDO);
        contenido.setBorder(Estilos.margen());
        contenido.add(Estilos.titulo("Historial de movimientos (demo de componentes)"), BorderLayout.NORTH);
        contenido.add(tabla, BorderLayout.CENTER);

        JPanel pie = new JPanel();
        pie.setOpaque(false);
        JButton avisar = Estilos.botonSecundario("Probar Toast");
        avisar.addActionListener(e -> Toast.exito(contenido, "Operación registrada"));
        JButton confirmar = Estilos.botonPrimario("Probar confirmación");
        confirmar.addActionListener(e -> {
            if (DialogoConfirmacion.confirmar(contenido, "Confirmar compra",
                    "Se descontarán S/ 12.50 del saldo. ¿Desea continuar?")) {
                Toast.exito(contenido, "Compra confirmada");
            } else {
                Toast.info(contenido, "Operación cancelada");
            }
        });
        pie.add(avisar);
        pie.add(confirmar);
        contenido.add(pie, BorderLayout.SOUTH);

        JFrame ventana = new JFrame("Componentes base · T07");
        ventana.setContentPane(contenido);
        ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ventana.setMinimumSize(new Dimension(900, 520));
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    private static List<Movimiento> datosDeEjemplo() {
        LocalDateTime base = LocalDateTime.of(2026, 10, 20, 8, 30);
        return List.of(
                new Movimiento(base, "Compra de cuaderno", "DINERO", new BigDecimal("12.50")),
                new Movimiento(base.plusDays(1), "Recarga aprobada", "YAPE", new BigDecimal("20.00")),
                new Movimiento(base.plusDays(3), "Canje de lápiz", "MONEDAS", new BigDecimal("0.00")),
                new Movimiento(base.plusDays(6), "Compra de refrigerio; con «comillas»", "DINERO",
                        new BigDecimal("8.90")),
                new Movimiento(base.plusDays(9), "Logro: puntualidad", "MONEDAS", new BigDecimal("0.00")));
    }
}
