package com.mycompany.escuelavirtual.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Punto único de lectura de la configuración de la aplicación.
 *
 * <p>Orden de precedencia (el primero que tenga valor gana):</p>
 * <ol>
 *   <li>Propiedad del sistema: {@code -DCLAVE=valor}</li>
 *   <li>Variable de entorno del sistema operativo: {@code export CLAVE=valor}</li>
 *   <li>Archivo local {@code .env} (o {@code .dotenv} como alias)</li>
 * </ol>
 *
 * <p>Las variables de entorno reales siempre pisan al archivo, de modo que en
 * producción/CI no hace falta copiar ningún {@code .env}. Si no existe el
 * archivo, la aplicación arranca igual y se usan los valores por defecto.</p>
 *
 * <p>Un valor vacío ({@code CLAVE=}) se considera "no configurado", por lo que
 * se aplica el valor por defecto o se devuelve {@code null}. Esto permite
 * versionar plantillas con los campos vacíos.</p>
 */
public final class Config {

    private static final String ENV_FILE = ".env";
    private static final String ENV_FILE_ALIAS = ".dotenv";

    /** Puede ser {@code null} si el archivo no existe o no se pudo leer. */
    private static final Dotenv DOTENV = loadDotenv();

    private Config() {
        // Clase de utilidad: no se instancia.
    }

    /**
     * Busca el archivo de entorno en el directorio de trabajo actual.
     * Prioriza {@code .env} y acepta {@code .dotenv} como alias.
     */
    private static Dotenv loadDotenv() {
        try {
            Path workingDirectory = Paths.get(System.getProperty("user.dir", "."));
            String filename = Files.isRegularFile(workingDirectory.resolve(ENV_FILE))
                    ? ENV_FILE
                    : ENV_FILE_ALIAS;

            return Dotenv.configure()
                    .directory(workingDirectory.toString())
                    .filename(filename)
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load();
        } catch (DotenvException e) {
            System.err.println("[Config] No se pudo leer el archivo de entorno: " + e.getMessage());
            return null;
        }
    }

    /**
     * Devuelve el valor de una clave o {@code null} si no está configurada.
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Devuelve el valor de una clave o {@code defaultValue} si no está
     * configurada (ausente o vacía).
     */
    public static String get(String key, String defaultValue) {
        return resolve(key, defaultValue);
    }

    /**
     * Devuelve el valor de una clave y falla si no está configurada.
     *
     * @throws IllegalStateException si la clave no tiene valor
     */
    public static String require(String key) {
        String value = resolve(key, null);
        if (value == null) {
            throw new IllegalStateException(
                    "Falta la configuración obligatoria '" + key
                    + "'. Defínela en .env o como variable de entorno.");
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = resolve(key, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La configuración '" + key + "' debe ser un número entero, pero vale '" + value + "'.");
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = resolve(key, null);
        if (value == null) {
            return defaultValue;
        }
        return switch (value.toLowerCase()) {
            case "true", "1", "yes", "si", "sí" -> true;
            case "false", "0", "no" -> false;
            default -> throw new IllegalStateException(
                    "La configuración '" + key + "' debe ser true/false, pero vale '" + value + "'.");
        };
    }

    /**
     * Resuelve la clave respetando la precedencia y tratando los valores
     * vacíos como ausentes.
     */
    private static String resolve(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (isPresent(value)) {
            return value;
        }

        value = System.getenv(key);
        if (isPresent(value)) {
            return value;
        }

        if (DOTENV != null) {
            value = DOTENV.get(key);
            if (isPresent(value)) {
                return value;
            }
        }

        return defaultValue;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
