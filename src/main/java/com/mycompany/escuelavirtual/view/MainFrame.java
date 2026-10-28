package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.session.SessionManager;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.ui.DialogoConfirmacion;
import com.mycompany.escuelavirtual.util.ui.Estilos;
import com.mycompany.escuelavirtual.util.ui.Toast;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Ventana principal: cabecera, zona central con {@link CardLayout} y barra de
 * estado.
 *
 * <p>T09 añade aquí el {@code JTree} de módulos derivado de {@code Permisos} y
 * registra un panel por módulo en el {@link CardLayout}. T08 solo necesita que
 * el login abra esta ventana y que el cierre de sesión devuelva al login.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final SessionManager sesion;
    private final JPanel centro = new JPanel(new CardLayout());

    public MainFrame(SessionManager sesion) {
        super("Escuela Virtual");
        this.sesion = sesion;
        Usuario usuario = sesion.getUsuario();

        setLayout(new BorderLayout());
        add(crearCabecera(usuario), BorderLayout.NORTH);

        centro.setBackground(Estilos.FONDO);
        
        // Evaluar el rol del usuario conectado para inyectar el panel inicial correcto
        JPanel panelInicio = switch (usuario.getRol()) {
            case ADMIN -> new PanelInicioAdmin();
            case ESTUDIANTE -> new PanelInicioEstudiante();
            case PADRE -> new PanelInicioPadre();
            case DOCENTE -> new PanelInicioDocente();
        };

        // 1. Agregamos el panel al contenedor
        centro.add(panelInicio, "inicio");
        
        // 2. FORZAMOS al CardLayout a que muestre el panel que acabamos de agregar
        CardLayout layout = (CardLayout) centro.getLayout();
        layout.show(centro, "inicio");

        // 3. Agregamos el centro a la ventana
        add(centro, BorderLayout.CENTER);

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
