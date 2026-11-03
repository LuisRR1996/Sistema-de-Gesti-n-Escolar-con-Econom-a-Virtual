package com.mycompany.escuelavirtual.view;

import com.mycompany.escuelavirtual.model.Usuario;
import com.mycompany.escuelavirtual.session.SessionManager;
import com.mycompany.escuelavirtual.util.Formato;
import com.mycompany.escuelavirtual.util.Modulo;
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
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Ventana principal: cabecera, zona central con {@link CardLayout} y barra de
 * estado.
 *
 * <p>El {@code JTree} de la izquierda muestra solo los módulos que el rol ve
 * según {@link Modulo}, que traduce el anexo C del plan. Cada nodo abre su
 * tarjeta en el {@link CardLayout}: el módulo M01 abre el panel de inicio del
 * rol y el resto abre su pantalla prototipo (T09, defecto D-T11-01).</p>
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

        // Se inyecta el usuario a cada panel para que filtren la BD
        JPanel panelInicio = switch (usuario.getRol()) {
            case ADMIN -> new PanelInicioAdmin(usuario);
            case ESTUDIANTE -> new PanelInicioEstudiante(usuario);
            case PADRE -> new PanelInicioPadre(usuario);
            case DOCENTE -> new PanelInicioDocente(usuario);
        };

        add(crearArbolModulos(usuario, panelInicio), BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);
        add(crearBarraEstado(usuario), BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 640));
        setSize(1200, 720);
        setLocationRelativeTo(null);
    }

    /**
     * Árbol de módulos del rol y su tarjeta en el {@link CardLayout}.
     *
     * <p>Los nodos salen de {@link Modulo#visiblesPara}, de modo que el árbol
     * y las pruebas CP-01 y CP-18 leen la misma matriz.</p>
     */
    private JScrollPane crearArbolModulos(Usuario usuario, JPanel panelInicio) {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Módulos");
        for (Modulo modulo : Modulo.visiblesPara(usuario.getRol())) {
            raiz.add(new DefaultMutableTreeNode(modulo));
            centro.add(modulo == Modulo.M01 ? panelInicio : new PanelModuloPrototipo(modulo),
                    modulo.name());
        }

        JTree arbol = new JTree(raiz);
        arbol.setName("arbolModulos");
        arbol.setFont(Estilos.FUENTE_NORMAL);
        arbol.setBackground(Estilos.FONDO_PANEL);
        arbol.setRootVisible(false);
        arbol.setShowsRootHandles(false);
        TreeSelectionModel seleccion = new DefaultTreeSelectionModel();
        seleccion.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        arbol.setSelectionModel(seleccion);
        arbol.addTreeSelectionListener(e -> {
            TreePath ruta = arbol.getSelectionPath();
            if (ruta != null
                    && ((DefaultMutableTreeNode) ruta.getLastPathComponent())
                            .getUserObject() instanceof Modulo modulo) {
                ((CardLayout) centro.getLayout()).show(centro, modulo.name());
            }
        });
        arbol.setSelectionRow(0);

        JScrollPane marco = new JScrollPane(arbol);
        marco.setPreferredSize(new Dimension(220, 0));
        marco.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Estilos.BORDE));
        return marco;
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
