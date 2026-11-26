package com.mycompany.escuelavirtual.util;

import com.mycompany.escuelavirtual.model.enums.Rol;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CP-18: el árbol de navegación muestra exactamente los módulos del anexo C.
 *
 * <p>No necesita base de datos ni Swing: comprueba la matriz de la que el
 * {@code JTree} del {@code MainFrame} se construye.</p>
 */
class ModuloTest {

    @Test
    @DisplayName("CP-01 · El administrador ve los 9 módulos, de M01 a M09")
    void administradorVeNueveModulos() {
        List<Modulo> visibles = Modulo.visiblesPara(Rol.ADMIN);

        assertEquals(9, visibles.size());
        assertEquals(List.of(Modulo.values()), visibles, "en orden de código y sin omitir ninguno");
    }

    @Test
    @DisplayName("CP-18 · Cada rol ve los módulos del anexo C y ningún otro")
    void modulosPorRol() {
        assertEquals(List.of(Modulo.M01, Modulo.M03, Modulo.M05, Modulo.M06, Modulo.M07),
                Modulo.visiblesPara(Rol.ESTUDIANTE));
        assertEquals(List.of(Modulo.M01, Modulo.M03, Modulo.M04, Modulo.M06, Modulo.M08),
                Modulo.visiblesPara(Rol.PADRE));
        assertEquals(List.of(Modulo.M01, Modulo.M06, Modulo.M08),
                Modulo.visiblesPara(Rol.DOCENTE));
    }

    @Test
    @DisplayName("CP-18 · Sin rol no se muestra ningún módulo y la etiqueta es legible")
    void sinRolYEtiqueta() {
        assertTrue(Modulo.visiblesPara(null).isEmpty(), "un rol nulo no abre nada");
        assertEquals("M02 · Usuarios", Modulo.M02.getEtiqueta());
    }
}
