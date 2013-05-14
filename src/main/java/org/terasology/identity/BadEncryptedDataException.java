package org.terasology.identity;

/**
 * This exception indicates an issue during decryption c
 */
public class BadEncryptedDataException extends Exception {

    public BadEncryptedDataException() {}

    public BadEncryptedDataException(String message) {
        super(message);
    }

    public BadEncryptedDataException(Throwable cause) {
        super(cause);
    }

    public BadEncryptedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
