package com.mycompany.escuelavirtual.util;

/**
 * Códigos estables de error de negocio. La interfaz puede traducirlos a un
 * mensaje para el usuario sin depender del texto de la excepción.
 */
public enum CodigoError {

    DATO_INVALIDO("Los datos ingresados no son válidos."),
    NO_ENCONTRADO("No se encontró el registro solicitado."),
    CONFLICTO("La operación entra en conflicto con el estado actual."),
    CREDENCIALES_INVALIDAS("El correo o la contraseña no son correctos."),
    CUENTA_INACTIVA("La cuenta está inactiva. Contacte al administrador."),
    CUENTA_BLOQUEADA("La cuenta está bloqueada temporalmente por intentos fallidos."),
    SIN_CONEXION("No hay conexión con la base de datos."),
    PERMISO_DENEGADO("No tiene permiso para realizar esta operación."),
    SALDO_INSUFICIENTE("El saldo disponible no alcanza para la operación."),
    STOCK_INSUFICIENTE("No hay stock suficiente del producto."),
    ESTADO_INVALIDO("La operación no es válida en el estado actual del registro."),
    ERROR_INTERNO("Ocurrió un error interno. Intente nuevamente.");

    private final String mensaje;

    CodigoError(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
