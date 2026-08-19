package com.papel.imdb_clone.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


/**
 * Utility class for hashing and verifying passwords using PBKDF2.
 * This class cannot be instantiated.
 */
public class PasswordHasher {

    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PasswordHasher() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Constants for the hash function
    private static final int ITERATIONS = 10000; // means the number of times to hash the password to make it more secure
    private static final int KEY_LENGTH = 256; // means the length of the key in bits

    /*
     * PBKDF2WithHmacSHA256 is a key derivation function that uses the PBKDF2 algorithm with HMAC and SHA-256.
     * It is a secure way to generate a key from a password.
     * It works by using a random salt and a number of iterations to generate a key from a password.
     * The salt is a random value that is used to make the hash unique for each user.
     * The iterations are the number of times the hash function is applied to the password.
     * The key length is the length of the key in bits.
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256"; // means the algorithm to use.
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a password with a random salt.
     *
     * @param password the password to hash
     * @return a string containing the algorithm, iterations, salt, and hash
     */
    /**
     * Hashes a password with a random salt.
     *
     * @param password the password to hash
     * @return a string containing the algorithm, iterations, salt, and hash
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            logger.error("Attempted to hash null or empty password");
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        logger.debug("Starting password hashing process");
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        try {
            byte[] hash = pbkdf2(password.toCharArray(), salt, KEY_LENGTH);
            String result = String.format("pbkdf2:%s:%s",
                    Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(hash));

            logger.debug("Successfully hashed password");
            return result;
        } catch (Exception e) {
            logger.error("Error hashing password: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password the password to verify
     * @param storedHash the stored hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    /**
     * Verifies a password against a stored hash.
     *
     * @param password   the password to verify
     * @param storedHash the stored hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || password.isEmpty()) {
            logger.warn("Attempted to verify null or empty password");
            return false;
        }

        if (storedHash == null || !storedHash.startsWith("pbkdf2:")) {
            logger.warn("Invalid stored hash format");
            return false;
        }

        logger.debug("Starting password verification");

        try {
            // Split the stored hash into algorithm, salt, and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 3) {
                logger.warn("Invalid stored hash format: unexpected number of parts");
                return false;
            }

            // Extract the salt and hash from the stored hash
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);

            logger.debug("Generating hash for verification");
            byte[] testHash = pbkdf2(password.toCharArray(), salt, hash.length * 8);

            // Constant time comparison to prevent timing attacks
            boolean result = constantTimeEquals(hash, testHash);

            if (result) {
                logger.debug("Password verification successful");
            } else {
                logger.warn("Password verification failed - hashes do not match");
            }

            return result;
        } catch (Exception e) {
            logger.error("Error during password verification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Helper method to perform PBKDF2 hashing.
     *
     * @param password  the password to hash
     * @param salt      the salt to use which means random bytes
     * @param keyLength the key length to use which means the length of the key in bits
     * @return the hash of the password
     */
    /**
     * Compares two byte arrays in constant time to prevent timing attacks.
     *
     * @param a first byte array
     * @param b second byte array
     * @return true if the arrays are equal, false otherwise
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }

        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Helper method to perform PBKDF2 hashing.
     *
     * @param password  the password to hash
     * @param salt      the salt to use
     * @param keyLength the key length in bits
     * @return the hash of the password
     * @throws RuntimeException if hashing fails
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int keyLength) {
        try {
            logger.trace("Generating PBKDF2 hash with {} iterations and key length {}", ITERATIONS, keyLength);
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error in PBKDF2 hashing: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to hash password", e);
        } finally {
            // Clear sensitive data
            if (password != null) {
                java.util.Arrays.fill(password, '\0');
            }
        }
    }
}

