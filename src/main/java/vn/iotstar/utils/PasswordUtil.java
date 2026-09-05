package vn.iotstar.utils;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2-sha256";
    private static final int ITERATIONS = 600_000;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_BITS = 256;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mật khẩu không được để trống.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);

        byte[] result = derive(password, salt, ITERATIONS);

        return PREFIX
                + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(result);
    }

    public static boolean verify(
            String password,
            String storedHash) {

        if (password == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split("\\$", -1);

            if (parts.length != 4 || !PREFIX.equals(parts[0])) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            // Giới hạn để không xử lý dữ liệu hash bất thường.
            if (iterations < 100_000 || iterations > 2_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);

            if (salt.length != SALT_LENGTH
                    || expected.length != HASH_BITS / 8) {
                return false;
            }

            byte[] actual = derive(password, salt, iterations);

            try {
                return MessageDigest.isEqual(expected, actual);
            } finally {
                Arrays.fill(actual, (byte) 0);
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isEncoded(String value) {
        return value != null
                && value.startsWith(PREFIX + "$");
    }

    private static byte[] derive(
            String password,
            byte[] salt,
            int iterations) {

        char[] characters = password.toCharArray();
        PBEKeySpec specification = new PBEKeySpec(
                characters, salt, iterations, HASH_BITS);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            return factory.generateSecret(specification).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Không thể xử lý mật khẩu.", e);
        } finally {
            specification.clearPassword();
            Arrays.fill(characters, '\0');
        }
    }
}