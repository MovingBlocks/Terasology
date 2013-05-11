package org.terasology.identity;

/**
 *
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
