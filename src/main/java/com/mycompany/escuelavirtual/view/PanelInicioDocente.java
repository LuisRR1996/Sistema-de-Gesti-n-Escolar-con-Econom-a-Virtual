/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.controller.UsuarioController;
import com.mycompany.escuelavirtual.dao.BilleteraDAO;
import com.mycompany.escuelavirtual.dao.LogroDAO;
import com.mycompany.escuelavirtual.dao.LogroDAO.DetalleLogro;
import com.mycompany.escuelavirtual.model.Billetera;
import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.model.enums.Rol;
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
import java.sql.SQLException;
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

        @Override
        public String toString() {
            return nombreCompleto + " (" + grado + " " + seccion + ")";
        }
    }

    private final UsuarioController usuarioController = new UsuarioController();
    private final BilleteraDAO billeteraDAO = new BilleteraDAO();
    private final LogroDAO logroDAO = new LogroDAO();
    private final Usuario docenteActual;

    private final List<AlumnoAula> alumnos = new ArrayList<>();

    private PanelTablaBase<AlumnoAula> tablaAlumnos;
    private PanelTablaBase<DetalleLogro> tablaLogros;
    private final JComboBox<AlumnoAula> cboEstudiante = new JComboBox<>();

    public PanelInicioDocente(Usuario docenteActual) {
        this.docenteActual = docenteActual;

        setLayout(new BorderLayout(0, Estilos.ESPACIO));
        setBackground(Estilos.FONDO);
        setBorder(Estilos.margen());

        add(Estilos.titulo("Panel Docente - Gestión de Logros e Incentivos (BD)"), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(Estilos.ESPACIO_GRANDE, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(crearCuadroOtorgarLogro(), BorderLayout.WEST);
        cuerpo.add(crearCuadrosDerechos(), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);

        recargarDesdeBD();
    }

    private JPanel crearCuadroOtorgarLogro() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilos.FONDO_PANEL);
        form.setBorder(Estilos.tarjeta());

        JComboBox<String> cboTipoMerito = new JComboBox<>(new String[]{
            "Participación en clase", "Puntualidad", "Mejor promedio del bimestre", "Convivencia ejemplar", "Buen rendimiento académico", "Buen comportamiento en aula"
        });
        JTextField txtDescripcion = new JTextField("Destacado desempeño durante la sesión", 16);
        JSpinner spnMonedas = new JSpinner(new SpinnerNumberModel(15, 5, 200, 5));
        JButton btnOtorgar = Estilos.botonPrimario("Otorgar Monedas por Mérito");

        btnOtorgar.addActionListener(e -> {
            AlumnoAula alumno = (AlumnoAula) cboEstudiante.getSelectedItem();
            if (alumno == null) {
                return;
            }
            if (!Validador.esTextoValido(txtDescripcion.getText())) {
                DialogoConfirmacion.error(this, "Ingrese una breve descripción del mérito.");
                return;
            }
            String tipo = (String) cboTipoMerito.getSelectedItem();
            int monedas = (Integer) spnMonedas.getValue();

            if (DialogoConfirmacion.confirmar(this, "Confirmar Incentivo", "¿Otorgar +" + monedas + " monedas a " + alumno.nombreCompleto + " por '" + tipo + "'?")) {
                try {
                    int idDocente = docenteActual != null ? docenteActual.getIdUsuario() : 0;
                    logroDAO.otorgarLogro(alumno.id, idDocente, tipo, txtDescripcion.getText().trim(), monedas);
                    recargarDesdeBD();
                    Toast.exito(this, "¡Se otorgaron +" + monedas + " monedas a " + alumno.nombreCompleto + " en BD!");
                } catch (SQLException ex) {
                    DialogoConfirmacion.error(this, "Error al otorgar logro en BD: " + ex.getMessage());
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
        cuadroAlumnos.add(Estilos.subtitulo("2. Estudiantes y Saldo de Monedas (BD)"), BorderLayout.NORTH);

        tablaAlumnos = new PanelTablaBase<>(
                new String[]{"ID", "Estudiante", "Grado", "Sección", "Monedas Acumuladas"},
                a -> new Object[]{a.id, a.nombreCompleto, a.grado, a.seccion, Formato.monedas(a.monedasAcumuladas)}
        );
        tablaAlumnos.ocultarFiltroFecha();
        cuadroAlumnos.add(tablaAlumnos, BorderLayout.CENTER);

        JPanel cuadroHistorial = new JPanel(new BorderLayout(0, Estilos.ESPACIO_CHICO));
        cuadroHistorial.setBackground(Estilos.FONDO_PANEL);
        cuadroHistorial.setBorder(Estilos.tarjeta());
        cuadroHistorial.add(Estilos.subtitulo("3. Historial Global de Logros (BD)"), BorderLayout.NORTH);

        tablaLogros = new PanelTablaBase<>(
                new String[]{"ID", "Fecha", "Estudiante", "Tipo de Mérito", "Descripción", "Monedas"},
                l -> new Object[]{l.idLogro(), Formato.fechaHora(l.fecha()), l.nombreEstudiante(), l.tipo(), l.descripcion(), "+" + Formato.monedas(l.monedas())}
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
            cboEstudiante.removeAllItems();
            List<Usuario> estudiantes = usuarioController.obtenerPorRol(Rol.ESTUDIANTE);
            for (Usuario e : estudiantes) {
                Billetera b = billeteraDAO.obtenerOCrearPorEstudiante(e.getIdUsuario());
                AlumnoAula aula = new AlumnoAula(e.getIdUsuario(), e.getNombreCompleto(), e.getGrado() == null ? "-" : e.getGrado(), e.getSeccion() == null ? "-" : e.getSeccion(), b.getSaldoMonedas());
                alumnos.add(aula);
                cboEstudiante.addItem(aula);
            }
            tablaAlumnos.setDatos(alumnos);
            tablaLogros.setDatos(logroDAO.listarTodos());
        } catch (SQLException e) {
            DialogoConfirmacion.error(this, "Error listando BD: " + e.getMessage());
        }
    }
}
