package com.mycompany.escuelavirtual.util.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Tabla reutilizable con búsqueda, filtro por fecha, estado vacío y
 * exportación a CSV.
 *
 * <p>Se construye con las columnas y una función que convierte cada objeto de
 * dominio en una fila; la pantalla concreta solo llama a
 * {@link #setDatos(List)}. El panel no consulta la base de datos.</p>
 *
 * @param <T> tipo de los objetos de dominio que se listan
 */
public class PanelTablaBase<T> extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String TARJETA_TABLA = "tabla";
    private static final String TARJETA_VACIO = "vacio";
    private static final DateTimeFormatter FORMATO_FILTRO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ModeloTabla modelo;
    private final JTable tabla;
    private final TableRowSorter<ModeloTabla> ordenador;
    private final JTextField busqueda = new JTextField(18);
    private final JTextField desde = new JTextField(9);
    private final JTextField hasta = new JTextField(9);
    private final JLabel etiquetaDesde = Estilos.etiqueta("Desde:");
    private final JLabel etiquetaHasta = Estilos.etiqueta("Hasta:");
    private final JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, Estilos.ESPACIO, Estilos.ESPACIO_CHICO));
    private final JPanel contenido = new JPanel(new CardLayout());
    private final JLabel vacio = new JLabel("No hay registros para mostrar.", SwingConstants.CENTER);
    private final Function<T, Object[]> aFila;

    private List<T> datos = new ArrayList<>();
    private int columnaFecha = -1;

    public PanelTablaBase(String[] columnas, Function<T, Object[]> aFila) {
        super(new BorderLayout(0, Estilos.ESPACIO));
        this.aFila = aFila;
        this.modelo = new ModeloTabla(columnas);
        this.tabla = new JTable(modelo);
        this.ordenador = new TableRowSorter<>(modelo);

        setOpaque(false);
        configurarTabla();
        add(crearBarra(), BorderLayout.NORTH);

        vacio.setFont(Estilos.FUENTE_NORMAL);
        vacio.setForeground(Estilos.TEXTO_SUAVE);
        vacio.setOpaque(true);
        vacio.setBackground(Estilos.FONDO_PANEL);
        vacio.setBorder(BorderFactory.createLineBorder(Estilos.BORDE));

        contenido.add(new JScrollPane(tabla), TARJETA_TABLA);
        contenido.add(vacio, TARJETA_VACIO);
        add(contenido, BorderLayout.CENTER);
        actualizarEstadoVacio();
    }

    private void configurarTabla() {
        tabla.setRowSorter(ordenador);
        tabla.setFont(Estilos.FUENTE_NORMAL);
        tabla.setRowHeight(Estilos.ALTO_FILA_TABLA);
        tabla.setGridColor(Estilos.BORDE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(false);
        tabla.getTableHeader().setFont(Estilos.FUENTE_NEGRITA);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel crearBarra() {
        barra.setOpaque(false);
        barra.add(Estilos.etiqueta("Buscar:"));
        barra.add(Estilos.campo(busqueda));
        barra.add(etiquetaDesde);
        barra.add(Estilos.campo(desde));
        barra.add(etiquetaHasta);
        barra.add(Estilos.campo(hasta));

        JButton exportar = Estilos.botonSecundario("Exportar CSV");
        exportar.addActionListener(e -> exportar());
        barra.add(exportar);

        DocumentListener alEscribir = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltros();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltros();
            }
        };
        busqueda.getDocument().addDocumentListener(alEscribir);
        desde.getDocument().addDocumentListener(alEscribir);
        hasta.getDocument().addDocumentListener(alEscribir);
        desde.setToolTipText("dd/MM/aaaa");
        hasta.setToolTipText("dd/MM/aaaa");
        return barra;
    }

    /** Indica qué columna contiene la fecha sobre la que filtran Desde/Hasta. */
    public void setColumnaFecha(int indice) {
        columnaFecha = indice;
    }

    /** Oculta el filtro por fecha en las pantallas que no lo necesitan. */
    public void ocultarFiltroFecha() {
        desde.setText("");
        hasta.setText("");
        etiquetaDesde.setVisible(false);
        desde.setVisible(false);
        etiquetaHasta.setVisible(false);
        hasta.setVisible(false);
    }

    public void setDatos(List<T> nuevos) {
        datos = nuevos == null ? new ArrayList<>() : new ArrayList<>(nuevos);
        modelo.recargar();
        aplicarFiltros();
        actualizarEstadoVacio();
    }

    public List<T> getDatos() {
        return List.copyOf(datos);
    }

    /** Objeto de dominio seleccionado o {@code null} si no hay selección. */
    public T getSeleccionado() {
        int visible = tabla.getSelectedRow();
        return visible < 0 ? null : datos.get(tabla.convertRowIndexToModel(visible));
    }

    public JTable getTabla() {
        return tabla;
    }

    /** Filas visibles tras aplicar la búsqueda y el filtro de fechas. */
    public int getFilasVisibles() {
        return tabla.getRowCount();
    }

    /** Escribe el texto de búsqueda como si lo tecleara el usuario. */
    public void setBusqueda(String texto) {
        busqueda.setText(texto);
    }

    /** Fija el rango de fechas en formato dd/MM/aaaa; cadena vacía lo quita. */
    public void setRangoFechas(String desdeTexto, String hastaTexto) {
        desde.setText(desdeTexto);
        hasta.setText(hastaTexto);
    }

    private void aplicarFiltros() {
        String texto = busqueda.getText().trim().toLowerCase();
        LocalDate fechaDesde = parsearFecha(desde);
        LocalDate fechaHasta = parsearFecha(hasta);

        if (texto.isEmpty() && fechaDesde == null && fechaHasta == null) {
            ordenador.setRowFilter(null);
        } else {
            ordenador.setRowFilter(new RowFilter<ModeloTabla, Integer>() {
                @Override
                public boolean include(Entry<? extends ModeloTabla, ? extends Integer> fila) {
                    return coincideTexto(fila, texto) && dentroDelRango(fila, fechaDesde, fechaHasta);
                }
            });
        }
        actualizarEstadoVacio();
    }

    private boolean coincideTexto(RowFilter.Entry<? extends ModeloTabla, ? extends Integer> fila, String texto) {
        if (texto.isEmpty()) {
            return true;
        }
        for (int i = 0; i < fila.getValueCount(); i++) {
            if (fila.getStringValue(i).toLowerCase().contains(texto)) {
                return true;
            }
        }
        return false;
    }

    private boolean dentroDelRango(RowFilter.Entry<? extends ModeloTabla, ? extends Integer> fila,
            LocalDate fechaDesde, LocalDate fechaHasta) {
        if (columnaFecha < 0 || (fechaDesde == null && fechaHasta == null)) {
            return true;
        }
        LocalDate fecha = aFecha(fila.getValue(columnaFecha));
        if (fecha == null) {
            return false;
        }
        return (fechaDesde == null || !fecha.isBefore(fechaDesde))
                && (fechaHasta == null || !fecha.isAfter(fechaHasta));
    }

    private static LocalDate aFecha(Object valor) {
        if (valor instanceof LocalDate fecha) {
            return fecha;
        }
        if (valor instanceof LocalDateTime fechaHora) {
            return fechaHora.toLocalDate();
        }
        if (valor instanceof java.util.Date fecha) {
            return new java.sql.Timestamp(fecha.getTime()).toLocalDateTime().toLocalDate();
        }
        if (valor instanceof String texto) {
            try {
                return LocalDate.parse(texto.substring(0, Math.min(10, texto.length())), FORMATO_FILTRO);
            } catch (DateTimeParseException | IndexOutOfBoundsException e) {
                return null;
            }
        }
        return null;
    }

    /** Devuelve la fecha escrita o {@code null}; marca el campo si no es válida. */
    private LocalDate parsearFecha(JTextField campo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            Estilos.marcarError(campo, false);
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(texto, FORMATO_FILTRO);
            Estilos.marcarError(campo, false);
            return fecha;
        } catch (DateTimeParseException e) {
            Estilos.marcarError(campo, true);
            return null;
        }
    }

    private void actualizarEstadoVacio() {
        boolean hayFilas = tabla.getRowCount() > 0;
        vacio.setText(datos.isEmpty()
                ? "No hay registros para mostrar."
                : "Ningún registro coincide con el filtro.");
        ((CardLayout) contenido.getLayout()).show(contenido, hayFilas ? TARJETA_TABLA : TARJETA_VACIO);
    }

    /** Exporta las filas visibles a CSV con BOM UTF-8 y separador «;». */
    public void exportar() {
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
        selector.setDialogTitle("Exportar a CSV");
        selector.setSelectedFile(new File("export.csv"));
        selector.setFileFilter(new FileNameExtensionFilter("Archivo CSV", "csv"));
        if (selector.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".csv")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".csv");
        }
        try (Writer salida = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(destino.toPath()), StandardCharsets.UTF_8))) {
            salida.write('﻿'); // BOM: Excel reconoce las tildes
            escribirCsv(salida);
            Toast.exito(this, "Archivo exportado: " + destino.getName());
        } catch (IOException e) {
            DialogoConfirmacion.error(this, "No se pudo escribir el archivo: " + e.getMessage());
        }
    }

    /** Escribe las filas visibles en formato CSV. Separado para poder probarlo. */
    public void escribirCsv(Writer salida) throws IOException {
        for (int c = 0; c < modelo.getColumnCount(); c++) {
            salida.write(escapar(modelo.getColumnName(c)));
            salida.write(c == modelo.getColumnCount() - 1 ? "\r\n" : ";");
        }
        for (int f = 0; f < tabla.getRowCount(); f++) {
            for (int c = 0; c < modelo.getColumnCount(); c++) {
                Object valor = tabla.getValueAt(f, c);
                salida.write(escapar(valor == null ? "" : valor.toString()));
                salida.write(c == modelo.getColumnCount() - 1 ? "\r\n" : ";");
            }
        }
    }

    private static String escapar(String valor) {
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return '"' + valor.replace("\"", "\"\"") + '"';
        }
        return valor;
    }

    /** Modelo de solo lectura que proyecta la lista de datos en filas. */
    private final class ModeloTabla extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private final String[] columnas;
        private List<Object[]> filas = new ArrayList<>();

        private ModeloTabla(String[] columnas) {
            this.columnas = columnas.clone();
        }

        private void recargar() {
            List<Object[]> nuevas = new ArrayList<>(datos.size());
            for (T dato : datos) {
                nuevas.add(aFila.apply(dato));
            }
            filas = nuevas;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.length;
        }

        @Override
        public String getColumnName(int columna) {
            return columnas[columna];
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            return filas.get(fila)[columna];
        }

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    }
}
