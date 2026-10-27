package com.mycompany.escuelavirtual.util.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Búsqueda, filtro por fecha y exportación del componente base de tablas. */
class PanelTablaBaseTest {

    private record Fila(String fecha, String concepto) {
    }

    private PanelTablaBase<Fila> panel;

    @BeforeEach
    void crearPanel() {
        panel = new PanelTablaBase<>(new String[] {"Fecha", "Concepto"},
                f -> new Object[] {f.fecha(), f.concepto()});
        panel.setColumnaFecha(0);
        panel.setDatos(List.of(
                new Fila("20/10/2026", "Compra de cuaderno"),
                new Fila("25/10/2026", "Recarga aprobada"),
                new Fila("30/10/2026", "Canje de lápiz")));
    }

    @Test
    @DisplayName("Sin filtros se ven todas las filas")
    void sinFiltros() {
        assertEquals(3, panel.getFilasVisibles());
    }

    @Test
    @DisplayName("La búsqueda no distingue mayúsculas y mira todas las columnas")
    void busqueda() {
        panel.setBusqueda("RECARGA");
        assertEquals(1, panel.getFilasVisibles());

        panel.setBusqueda("no existe");
        assertEquals(0, panel.getFilasVisibles());
    }

    @Test
    @DisplayName("El filtro por fecha incluye los extremos del rango")
    void filtroPorFecha() {
        panel.setRangoFechas("25/10/2026", "30/10/2026");
        assertEquals(2, panel.getFilasVisibles());

        panel.setRangoFechas("", "20/10/2026");
        assertEquals(1, panel.getFilasVisibles());
    }

    @Test
    @DisplayName("Una fecha mal escrita no descarta filas")
    void fechaInvalida() {
        panel.setRangoFechas("31/31/2026", "");
        assertEquals(3, panel.getFilasVisibles());
    }

    @Test
    @DisplayName("El CSV exporta solo lo visible y escapa el separador")
    void exportaCsv() throws IOException {
        panel.setDatos(List.of(new Fila("20/10/2026", "Refrigerio; con \"comillas\"")));

        StringWriter salida = new StringWriter();
        panel.escribirCsv(salida);
        String csv = salida.toString();

        assertTrue(csv.startsWith("Fecha;Concepto\r\n"), "debe empezar por la cabecera: " + csv);
        assertTrue(csv.contains("\"Refrigerio; con \"\"comillas\"\"\""), "debe escapar «;» y comillas: " + csv);
        assertEquals(2, csv.split("\r\n").length, "cabecera + una fila");
    }
}
