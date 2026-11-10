package com.mycompany.escuelavirtual.util;

import com.mycompany.escuelavirtual.model.enums.Rol;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Módulos del sistema y roles que los ven en el árbol de navegación (T09).
 *
 * <p>La tabla «Rol / Módulos visibles» del anexo C del plan de desarrollo es la
 * única fuente de esta matriz, igual que {@link Permisos} lo es para las
 * acciones. El {@code JTree} del {@code MainFrame} y las pruebas CP-01 y CP-18
 * se derivan de aquí, de modo que no puedan discrepar entre sí.</p>
 *
 * <p>Sin dependencias de Swing: el enum es la matriz, y la vista decide qué
 * panel abre cada módulo.</p>
 */
public enum Modulo {

    M01("M01", "Inicio y sesión", Rol.ESTUDIANTE, Rol.PADRE, Rol.DOCENTE, Rol.ADMIN),
    M02("M02", "Usuarios", Rol.ADMIN),
    M03("M03", "Billetera", Rol.ESTUDIANTE, Rol.PADRE, Rol.ADMIN),
    M04("M04", "Recargas", Rol.PADRE, Rol.ADMIN),
    M05("M05", "Tienda y compras", Rol.ESTUDIANTE, Rol.ADMIN),
    M06("M06", "Logros", Rol.ESTUDIANTE, Rol.PADRE, Rol.DOCENTE, Rol.ADMIN),
    M07("M07", "Catálogo y canjes", Rol.ESTUDIANTE, Rol.ADMIN),
    M08("M08", "Supervisión", Rol.PADRE, Rol.DOCENTE, Rol.ADMIN),
    M09("M09", "Reportes y CSV", Rol.ADMIN);

    private final String codigo;
    private final String titulo;
    private final Set<Rol> roles;

    Modulo(String codigo, String titulo, Rol... roles) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.roles = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(roles)));
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    /** Etiqueta del nodo del árbol, por ejemplo {@code "M02 · Usuarios"}. */
    public String getEtiqueta() {
        return codigo + " · " + titulo;
    }

    /** El nodo del {@code JTree} se dibuja con la etiqueta, no con el nombre del enum. */
    @Override
    public String toString() {
        return getEtiqueta();
    }

    /** Módulos que el rol ve, en orden de código. Lista vacía si el rol es nulo. */
    public static List<Modulo> visiblesPara(Rol rol) {
        return rol == null ? List.of()
                : Stream.of(values()).filter(m -> m.roles.contains(rol)).toList();
    }
}
