package com.mycompany.escuelavirtual.util;

import java.util.Objects;

/**
 * Excepción de negocio con un {@link CodigoError} estable. Los servicios la
 * lanzan para que la interfaz muestre un mensaje claro sin filtrar detalles
 * técnicos de SQL Server.
 */
public class NegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final CodigoError codigo;

    public NegocioException(CodigoError codigo) {
        this(codigo, codigo == null ? null : codigo.getMensaje());
    }

    public NegocioException(CodigoError codigo, String mensaje) {
        super(mensaje);
        this.codigo = Objects.requireNonNull(codigo, "codigo");
    }

    public NegocioException(CodigoError codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = Objects.requireNonNull(codigo, "codigo");
    }

    public CodigoError getCodigo() {
        return codigo;
    }
}
