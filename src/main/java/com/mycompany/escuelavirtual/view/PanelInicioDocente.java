/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.Validador;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelTablaBase;
import com.mycompany.escuelavirtual.util.ui.Toast;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author user
 */
public class PanelInicioDocente extends JPanel {

    private static final long serialVersionUID = 1L;

    private static class AlumnoAula {

        int id, monedasAcumuladas;
        String nombreCompleto, grado, seccion;

        AlumnoAula(int id, String nombreCompleto, String grado, String seccion, int monedasAcumuladas) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.grado = grado;
            this.seccion = seccion;
            this.monedasAcumuladas = monedasAcumuladas;
        }
    }

    private static class RegistroLogro {

        int id, monedas;
        String estudiante, tipo, descripcion;
        LocalDateTime fecha;

        RegistroLogro(int id, String estudiante, String tipo, String descripcion, int monedas, LocalDateTime fecha) {
            this.id = id;
            this.estudiante = estudiante;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.monedas = monedas;
            this.fecha = fecha;
        }
    }

    private final List<AlumnoAula> alumnos = new ArrayList<>();
    private final List<RegistroLogro> logrosOtorgados = new ArrayList<>();

    private PanelTablaBase<AlumnoAula> tablaAlumnos;
    private PanelTablaBase<RegistroLogro> tablaLogros;
    private final JComboBox<String> cboEstudiante = new JComboBox<>();

    public PanelInicioDocente() {
        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        cargarDatosDocente();

        add(Estilos.titulo("Panel Docente - Gestión de Logros e Incentivos Estudiantiles"), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(Estilos.ESPACIO_GRANDE, 0));
        cuerpo.setOpaque(false);

        cuerpo.add(crearCuadroOtorgarLogro(), BorderLayout.WEST);
        cuerpo.add(crearCuadrosDerechos(), BorderLayout.CENTER);

        add(cuerpo, BorderLayout.CENTER);
        refrescarTablas();
    }

    private JPanel crearCuadroOtorgarLogro() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilos.FONDO_PANEL);
        form.setBorder(Estilos.tarjeta());

        for (AlumnoAula a : alumnos) {
            cboEstudiante.addItem(a.nombreCompleto + " (" + a.grado + " " + a.seccion + ")");
        }

        JComboBox<String> cboTipoMerito = new JComboBox<>(new String[]{
            "Participación en clase",
            "Puntualidad",
            "Mejor promedio del bimestre",
            "Convivencia ejemplar",
            "Buen rendimiento académico",
            "Buen comportamiento en aula"
        });
        JTextField txtDescripcion = new JTextField("Destacado desempeño durante la sesión", 16);
        JSpinner spnMonedas = new JSpinner(new SpinnerNumberModel(15, 5, 200, 5));
        JButton btnOtorgar = Estilos.botonPrimario("Otorgar Monedas por Mérito");

        btnOtorgar.addActionListener(e -> {
            int idx = cboEstudiante.getSelectedIndex();
            if (idx < 0) {
                return;
            }
            if (!Validador.esTextoValido(txtDescripcion.getText())) {
                DialogoConfirmacion.error(this, "Ingrese una breve descripción del mérito.");
                return;
            }
            AlumnoAula alumno = alumnos.get(idx);
            String tipo = (String) cboTipoMerito.getSelectedItem();
            int monedas = (Integer) spnMonedas.getValue();

            if (DialogoConfirmacion.confirmar(this, "Confirmar Incentivo",
                    "¿Otorgar +" + monedas + " monedas a " + alumno.nombreCompleto + " por '" + tipo + "'?")) {
                alumno.monedasAcumuladas += monedas;
                logrosOtorgados.add(0, new RegistroLogro(
                        logrosOtorgados.size() + 1,
                        alumno.nombreCompleto,
                        tipo,
                        txtDescripcion.getText().trim(),
                        monedas,
                        LocalDateTime.now()
                ));
                refrescarTablas();
                Toast.exito(this, "¡Se otorgaron +" + monedas + " monedas a " + alumno.nombreCompleto + "!");
            }
        });

        int f = 0;
        agregarFila(form, f++, "1. Otorgar Monedas por Mérito", null, true);
        agregarFila(form, f++, "Estudiante:", cboEstudiante, false);
        agregarFila(form, f++, "Categoría de Mérito:", cboTipoMerito, false);
        agregarFila(form, f++, "Descripción / Motivo:", txtDescripcion, false);
        agregarFila(form, f++, "Monedas a Otorgar:", spnMonedas, false);
        agregarFila(form, f, "", btnOtorgar, false);

        return form;
    }

    private JPanel crearCuadrosDerechos() {
        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, Estilos.ESPACIO));
        derecha.setOpaque(false);

        // Cuadro Superior: Estudiantes a cargo y sus monedas
        JPanel cuadroAlumnos = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroAlumnos.setBackground(Estilos.FONDO_PANEL);
        cuadroAlumnos.setBorder(Estilos.tarjeta());
        cuadroAlumnos.add(Estilos.subtitulo("2. Estudiantes a Cargo y Saldo Actual de Monedas"), BorderLayout.NORTH);

        tablaAlumnos = new PanelTablaBase<>(
                new String[]{"ID", "Estudiante", "Grado", "Sección", "Monedas Acumuladas"},
                a -> new Object[]{a.id, a.nombreCompleto, a.grado, a.seccion, Formato.monedas(a.monedasAcumuladas)}
        );
        tablaAlumnos.ocultarFiltroFecha();
        cuadroAlumnos.add(tablaAlumnos, BorderLayout.CENTER);

        // Cuadro Inferior: Historial de logros registrados
        JPanel cuadroHistorial = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroHistorial.setBackground(Estilos.FONDO_PANEL);
        cuadroHistorial.setBorder(Estilos.tarjeta());
        cuadroHistorial.add(Estilos.subtitulo("3. Historial de Logros y Méritos Otorgados"), BorderLayout.NORTH);

        tablaLogros = new PanelTablaBase<>(
                new String[]{"ID", "Fecha", "Estudiante", "Tipo de Mérito", "Descripción", "Monedas"},
                l -> new Object[]{l.id, Formato.fechaHora(l.fecha), l.estudiante, l.tipo, l.descripcion, "+" + Formato.monedas(l.monedas)}
        );
        tablaLogros.ocultarFiltroFecha();
        cuadroHistorial.add(tablaLogros, BorderLayout.CENTER);

        derecha.add(cuadroAlumnos);
        derecha.add(cuadroHistorial);
        return derecha;
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, JComponent campo, boolean titulo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO, Estilos.ESPACIO_CHICO);
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (titulo) {
            c.gridx = 0;
            c.gridy = fila;
            c.gridwidth = 2;
            panel.add(Estilos.subtitulo(etiqueta), c);
            return;
        }
        c.gridx = 0;
        c.gridy = fila;
        panel.add(Estilos.etiqueta(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(campo, c);
    }

    private void cargarDatosDocente() {
        alumnos.add(new AlumnoAula(5, "Luis Fernando Torres Ramos", "5.º", "A", 120));
        alumnos.add(new AlumnoAula(6, "Carla Sofía Torres Ramos", "5.º", "A", 80));
        alumnos.add(new AlumnoAula(7, "Diego Alonso Ramos Flores", "4.º", "B", 45));

        LocalDateTime ahora = LocalDateTime.now();
        logrosOtorgados.add(new RegistroLogro(1, "Luis Fernando Torres Ramos", "Participación en clase", "Aportes voluntarios durante la semana de repaso.", 10, ahora.minusDays(6)));
        logrosOtorgados.add(new RegistroLogro(2, "Luis Fernando Torres Ramos", "Puntualidad", "Asistió a tiempo todos los días del mes.", 5, ahora.minusDays(4)));
        logrosOtorgados.add(new RegistroLogro(3, "Carla Sofía Torres Ramos", "Mejor promedio del bimestre", "Promedio destacado en el área de Matemática.", 50, ahora.minusDays(3)));
        logrosOtorgados.add(new RegistroLogro(4, "Diego Alonso Ramos Flores", "Convivencia ejemplar", "Medió un conflicto entre compañeros de su aula.", 15, ahora.minusDays(2)));
    }

    private void refrescarTablas() {
        tablaAlumnos.setDatos(alumnos);
        tablaLogros.setDatos(logrosOtorgados);
    }

}
