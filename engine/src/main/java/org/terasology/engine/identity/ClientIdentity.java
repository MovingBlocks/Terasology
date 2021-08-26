// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity;

import org.terasology.engine.config.SecurityConfig;

import java.util.Objects;

public class ClientIdentity {


    private PublicIdentityCertificate playerPublicCertificate;
    private PrivateIdentityCertificate playerPrivateCertificate;

    /**
     * @param publicCert  The public certificate to be passed
     * @param privateCert The private certificate to be passed
     */
    public ClientIdentity(PublicIdentityCertificate publicCert, PrivateIdentityCertificate privateCert) {
        this.playerPublicCertificate = publicCert;
        this.playerPrivateCertificate = privateCert;
    }

    /**
     * @return A PublicIdentityCertificate belonging to the player
     */
    public PublicIdentityCertificate getPlayerPublicCertificate() {
        return playerPublicCertificate;
    }

    /**
     * @return A PrivateIndentityCertificate belonging to the player, if possible (exists and is allowed)
     */
    public PrivateIdentityCertificate getPlayerPrivateCertificate() {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(SecurityConfig.PRIVATE_CERTIFICATE_ACCESS_PERMISSION);
        }
        return playerPrivateCertificate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPublicCertificate, playerPrivateCertificate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClientIdentity) {
            ClientIdentity other = (ClientIdentity) obj;
            return Objects.equals(playerPublicCertificate, other.playerPublicCertificate)
                    && Objects.equals(playerPrivateCertificate, other.playerPrivateCertificate);
        }
        return false;
    }
}
