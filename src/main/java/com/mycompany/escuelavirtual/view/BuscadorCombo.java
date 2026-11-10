/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.escuelavirtual.view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
/**
 *
 * @author user
 */
public class BuscadorCombo<T> {

    private final JComboBox<T> combo;
    private final List<T> todosLosElementos = new ArrayList<>();

    public BuscadorCombo(JComboBox<T> combo) {
        this.combo = combo;
        this.combo.setEditable(true); // Permite escribir en el combo

        JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int tecla = e.getKeyCode();
                // Ignorar flechas de navegación y Enter para que el menú funcione normal
                if (tecla == KeyEvent.VK_ENTER || tecla == KeyEvent.VK_UP
                        || tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_ESCAPE) {
                    return;
                }
                String texto = editor.getText();
                if (texto != null) {
                    filtrar(texto);
                }
            }
        });
    }

    /**
     * Carga la lista oficial de elementos (por ejemplo, desde la Base de
     * Datos).
     */
    public void setElementos(List<T> nuevosElementos) {
        todosLosElementos.clear();
        todosLosElementos.addAll(nuevosElementos);
        filtrar(""); // Muestra todo inicialmente
    }

    private void filtrar(String busqueda) {
        combo.hidePopup();
        combo.removeAllItems();
        String textoMinuscula = busqueda.toLowerCase();

        for (T item : todosLosElementos) {
            if (item.toString().toLowerCase().contains(textoMinuscula)) {
                combo.addItem(item);
            }
        }

        JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.setText(busqueda);

        if (combo.getItemCount() > 0 && !busqueda.isEmpty()) {
            combo.showPopup();
        }
    }

    /**
     * Devuelve el objeto seleccionado de forma segura. Retorna null si el
     * usuario escribió algo que no existe en la lista.
     */
    public T getSeleccionado() {
        Object sel = combo.getSelectedItem();
        if (todosLosElementos.contains(sel)) {
            return (T) sel;
        }
        return null;
    }
}
