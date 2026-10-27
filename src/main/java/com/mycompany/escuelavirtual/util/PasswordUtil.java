package com.mycompany.escuelavirtual.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilidades para almacenar y verificar contraseñas con PBKDF2.
 *
 * <p>Formato almacenado: {@code iteraciones$saltBase64$hashBase64}. El salt es
 * aleatorio de 16 bytes y el hash de 32 bytes (256 bits), generado con
 * {@value #ALGORITMO} y {@value #ITERACIONES} iteraciones.</p>
 */
public final class PasswordUtil {

    public static final int ITERACIONES = 120_000;

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int HASH_BYTES = HASH_BITS / 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Clase de utilidad.
    }

    /**
     * Genera un hash nuevo con salt aleatorio.
     *
     * @throws IllegalArgumentException si la contraseña es nula o vacía
     */
    public static String hash(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(contrasena.toCharArray(), salt, ITERACIONES, HASH_BYTES);
        return ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifica una contraseña contra un hash almacenado en el formato
     * {@code iteraciones$saltBase64$hashBase64}.
     *
     * <p>La comparación se hace en tiempo constante con
     * {@link MessageDigest#isEqual(byte[], byte[])}. Un hash malformado
     * devuelve {@code false}, nunca lanza una excepción de credenciales.</p>
     */
    public static boolean verificar(String contrasena, String hashAlmacenado) {
        if (contrasena == null || hashAlmacenado == null) {
            return false;
        }
        String[] partes = hashAlmacenado.split("\\$");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            if (iteraciones <= 0) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            if (salt.length == 0 || esperado.length == 0) {
                return false;
            }
            byte[] calculado = pbkdf2(contrasena.toCharArray(), salt, iteraciones, esperado.length);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] salt, int iteraciones, int bytes) {
        PBEKeySpec spec = new PBEKeySpec(contrasena, salt, iteraciones, bytes * 8);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo calcular el hash PBKDF2.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
