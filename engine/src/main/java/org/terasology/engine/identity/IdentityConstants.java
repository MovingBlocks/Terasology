// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity;

/**
 * Collection of constants related to Identity and Authentication
 */
public final class IdentityConstants {
    /**
     * The algorithm used for generating certificate keys
     */
    public static final String CERTIFICATE_ALGORITHM = "RSA";

    /**
     * The algorithm used for generating signatures
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * The algorithm used for symmetric encryption (used for speed and due to limits on the maximum size of data
     * that can be encrypted with asymmetric keys)
     */
    public static final String SYMMETRIC_ENCRYPTION_ALGORITHM = "ARCFOUR";

    /**
     * The length of the premaster secret (random number that is used to generate the symmetric key)
     */
    public static final int PREMASTER_SECRET_LENGTH = 46;

    /**
     * The length of the random numbers used in server and client hello messages (in bytes)
     */
    public static final int SERVER_CLIENT_RANDOM_LENGTH = 28;

    /**
     * The length in bytes of the symmetric key (max of 16 without installing the unlimited strength encryption package)
     */
    public static final int SYMMETRIC_ENCRYPTION_KEY_LENGTH = 16;


    private IdentityConstants() {
    }
}
