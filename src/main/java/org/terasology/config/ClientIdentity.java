package org.terasology.config;

import org.terasology.identity.PrivateIdentityCertificate;
import org.terasology.identity.PublicIdentityCertificate;

import java.security.SecurityPermission;

/**
 *
 */
public class ClientIdentity {


    private PublicIdentityCertificate playerPublicCertificate;
    private PrivateIdentityCertificate playerPrivateCertificate;

    public ClientIdentity(PublicIdentityCertificate publicCert, PrivateIdentityCertificate privateCert) {
        this.playerPublicCertificate = publicCert;
        this.playerPrivateCertificate = privateCert;
    }

    public PublicIdentityCertificate getPlayerPublicCertificate() {
        return playerPublicCertificate;
    }

    public PrivateIdentityCertificate getPlayerPrivateCertificate() {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(SecurityConfig.PRIVATE_CERTIFICATE_ACCESS_PERMISSION);
        }
        return playerPrivateCertificate;
    }
}
