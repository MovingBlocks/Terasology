package org.terasology.identity;

/**
 *
 */
public final class IdentityConstants {
    public static final String CERTIFICATE_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    public static final String SYMMETRIC_ENCRYPTION_ALGORITHM = "ARCFOUR";

    public static final int PREMASTER_SECRET_LENGTH = 46;
    public static final int SERVER_CLIENT_RANDOM_LENGTH = 28;
    public static final int SYMMETRIC_ENCRYPTION_KEY_LENGTH = 16;


    private IdentityConstants() {}
}
