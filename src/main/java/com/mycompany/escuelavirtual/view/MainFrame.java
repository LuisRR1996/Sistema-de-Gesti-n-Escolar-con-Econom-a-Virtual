package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.session.SessionManager;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.Toast;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Ventana principal: cabecera, panel de inicio del rol y barra de estado.
 *
 * <p>La ventana muestra una sola pantalla, la del módulo M01, que es la única
 * construida: el panel de inicio de cada rol. Las pantallas prototipo de M02 a
 * M09 y el árbol de navegación que las abría se retiraron de la ventana; la
 * matriz de módulos por rol sigue en {@code Modulo} y la leen las pruebas
 * CP-01 y CP-18.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final SessionManager sesion;

    public MainFrame(SessionManager sesion) {
        super("Escuela Virtual");
        this.sesion = sesion;
        Usuario usuario = sesion.getUsuario();

        setLayout(new BorderLayout());
        add(crearCabecera(usuario), BorderLayout.NORTH);

        // Se inyecta el usuario a cada panel para que filtren la BD
        JPanel panelInicio = switch (usuario.getRol()) {
            case ADMIN -> new PanelInicioAdmin(usuario);
            case ESTUDIANTE -> new PanelInicioEstudiante(usuario);
            case PADRE -> new PanelInicioPadre(usuario);
            case DOCENTE -> new PanelInicioDocente(usuario);
        };

        add(panelInicio, BorderLayout.CENTER);
        add(crearBarraEstado(usuario), BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 640));
        setSize(1200, 720);
        setLocationRelativeTo(null);
    }

    private JPanel crearCabecera(Usuario usuario) {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Estilos.PRIMARIO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(
                Estilos.ESPACIO, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO, Estilos.ESPACIO_GRANDE));

        JLabel titulo = new JLabel("Escuela Virtual");
        titulo.setFont(Estilos.FUENTE_TITULO);
        titulo.setForeground(java.awt.Color.WHITE);
        cabecera.add(titulo, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, Estilos.ESPACIO_GRANDE, 0));
        derecha.setOpaque(false);
        JLabel quien = new JLabel(usuario.getNombreCompleto() + " · " + usuario.getRol());
        quien.setFont(Estilos.FUENTE_NEGRITA);
        quien.setForeground(java.awt.Color.WHITE);
        derecha.add(quien);

        JButton salir = Estilos.botonSecundario("Cerrar sesión");
        salir.addActionListener(e -> cerrarSesion());
        derecha.add(salir);
        cabecera.add(derecha, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel crearBarraEstado(Usuario usuario) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, Estilos.ESPACIO_GRANDE, Estilos.ESPACIO_CHICO));
        barra.setBackground(Estilos.FONDO_PANEL);
        barra.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Estilos.BORDE));
        barra.add(Estilos.subtitulo("Sesión: " + usuario.getCorreo()));
        barra.add(Estilos.subtitulo("Último acceso: "
                + (usuario.getUltimoAcceso() == null ? "primero" : Formato.fechaHora(usuario.getUltimoAcceso()))));
        return barra;
    }

    private void cerrarSesion() {
        if (!DialogoConfirmacion.confirmar(this, "Cerrar sesión", "¿Desea cerrar la sesión actual?")) {
            return;
        }
        Toast.info(this, "Sesión cerrada");
        sesion.cerrar();
        new LoginFrame(sesion).setVisible(true);
        dispose();
    }
}
