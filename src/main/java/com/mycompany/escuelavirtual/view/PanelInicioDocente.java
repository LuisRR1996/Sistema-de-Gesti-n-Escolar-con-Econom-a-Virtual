/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.LogroController;
import com.mycompany.escuelavirtual.controller.UsuarioController;
import com.mycompany.escuelavirtual.model.DetalleLogro;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.NegocioException;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.PanelTablaBase;
import com.mycompany.escuelavirtual.util.ui.Toast;
import com.mycompany.escuelavirtual.view.BuscadorCombo;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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
 * Pantalla de inicio del docente (M06).
 *
 * <p>Según el anexo C, el docente registra logros y consulta los que ha
 * registrado, pero no ve billeteras ni historiales de estudiantes; por eso esta
 * pantalla no muestra saldos.</p>
 */
public class PanelInicioDocente extends JPanel {

    private static final long serialVersionUID = 1L;

    private static class AlumnoAula {

        int id;
        String nombreCompleto, grado, seccion;

        AlumnoAula(int id, String nombreCompleto, String grado, String seccion) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.grado = grado;
            this.seccion = seccion;
        }

        @Override
        public String toString() {
            return nombreCompleto + " (" + grado + " " + seccion + ")";
        }
    }

    private final UsuarioController usuarioController;
    private final LogroController logroController;

    private final List<AlumnoAula> alumnos = new ArrayList<>();

    private PanelTablaBase<AlumnoAula> tablaAlumnos;
    private PanelTablaBase<DetalleLogro> tablaLogros;
    private final JComboBox<AlumnoAula> cboEstudiante = new JComboBox<>();
    
    private BuscadorCombo<AlumnoAula> buscadorEstudiante;

    public PanelInicioDocente(Usuario docenteActual) {
        this.usuarioController = new UsuarioController(docenteActual);
        this.logroController = new LogroController(docenteActual);

        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        JPanel cabeceraTitulo = new JPanel(new BorderLayout());
        cabeceraTitulo.setOpaque(false);
        cabeceraTitulo.add(Estilos.titulo("Panel Docente - Registro de Logros e Incentivos"), BorderLayout.WEST);
        JButton btnRefrescar = Estilos.botonSecundario("↻ Actualizar Datos");
        btnRefrescar.addActionListener(e -> {
            recargarDesdeBD();
            Toast.info(this, "Datos actualizados desde SQL Server.");
        });
        cabeceraTitulo.add(btnRefrescar, BorderLayout.EAST);
        add(cabeceraTitulo, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(Estilos.ESPACIO_GRANDE, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(crearCuadroOtorgarLogro(), BorderLayout.WEST);
        cuerpo.add(crearCuadrosDerechos(), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);

        buscadorEstudiante = new BuscadorCombo<>(cboEstudiante); 
        
        recargarDesdeBD();
    }

    private JPanel crearCuadroOtorgarLogro() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilos.FONDO_PANEL);
        form.setBorder(Estilos.tarjeta());

        JComboBox<String> cboTipoMerito = new JComboBox<>(new String[]{
            "Participación en clase", "Puntualidad", "Mejor promedio del bimestre",
            "Convivencia ejemplar", "Buen rendimiento académico", "Buen comportamiento en aula"
        });
        JTextField txtDescripcion = new JTextField("Destacado desempeño durante la sesión", 16);
        JSpinner spnMonedas = new JSpinner(new SpinnerNumberModel(15, 5, 200, 5));
        JButton btnOtorgar = Estilos.botonPrimario("Otorgar Monedas por Mérito");

        btnOtorgar.addActionListener(e -> {
            AlumnoAula alumno = buscadorEstudiante.getSeleccionado();
            if (alumno == null) {
                DialogoConfirmacion.error(this, "Seleccione un estudiante.");
                return;
            }
            String tipo = (String) cboTipoMerito.getSelectedItem();
            int monedas = (Integer) spnMonedas.getValue();

            if (DialogoConfirmacion.confirmar(this, "Confirmar Incentivo",
                    "¿Otorgar +" + monedas + " monedas a " + alumno.nombreCompleto + " por '" + tipo + "'?")) {
                try {
                    logroController.registrar(alumno.id, tipo, txtDescripcion.getText(), monedas);
                    recargarDesdeBD();
                    Toast.exito(this, "Se otorgaron +" + monedas + " monedas a " + alumno.nombreCompleto + ".");
                } catch (NegocioException ex) {
                    DialogoConfirmacion.error(this, ex.getMessage());
                }
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

        JPanel cuadroAlumnos = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroAlumnos.setBackground(Estilos.FONDO_PANEL);
        cuadroAlumnos.setBorder(Estilos.tarjeta());
        cuadroAlumnos.add(Estilos.subtitulo("2. Estudiantes Activos"), BorderLayout.NORTH);

        tablaAlumnos = new PanelTablaBase<>(
                new String[]{"ID", "Estudiante", "Grado", "Sección"},
                a -> new Object[]{a.id, a.nombreCompleto, a.grado, a.seccion}
        );
        tablaAlumnos.ocultarFiltroFecha();
        cuadroAlumnos.add(tablaAlumnos, BorderLayout.CENTER);

        JPanel cuadroHistorial = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroHistorial.setBackground(Estilos.FONDO_PANEL);
        cuadroHistorial.setBorder(Estilos.tarjeta());
        cuadroHistorial.add(Estilos.subtitulo("3. Logros que he registrado"), BorderLayout.NORTH);

        tablaLogros = new PanelTablaBase<>(
                new String[]{"ID", "Fecha", "Estudiante", "Tipo de Mérito", "Descripción", "Monedas"},
                l -> new Object[]{l.idLogro(), Formato.fechaHora(l.fecha()), l.nombreEstudiante(),
                    l.tipo(), l.descripcion(), "+" + Formato.monedas(l.monedas())}
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
    
    private void recargarDesdeBD() {
        try {
            alumnos.clear();
            List<AlumnoAula> nuevaLista = new ArrayList<>(); 
            for (Usuario e : usuarioController.listarPorRol(Rol.ESTUDIANTE)) {
                AlumnoAula aula = new AlumnoAula(
                        e.getIdUsuario(),
                        e.getNombreCompleto(),
                        e.getGrado() == null ? "-" : e.getGrado(),
                        e.getSeccion() == null ? "-" : e.getSeccion());
                alumnos.add(aula);
                nuevaLista.add(aula);
            }
            buscadorEstudiante.setElementos(nuevaLista);
            tablaAlumnos.setDatos(alumnos);
            tablaLogros.setDatos(logroController.listarOtorgadosPorMi());
        } catch (NegocioException e) {
            DialogoConfirmacion.error(this, e.getMessage());
        }
    }   
}
