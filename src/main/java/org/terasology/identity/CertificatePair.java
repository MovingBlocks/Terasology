package org.terasology.identity;

/**
 *
 */
public class CertificatePair {
    private PublicIdentityCertificate publicCert;
    private PrivateIdentityCertificate privateCert;

    public CertificatePair(PublicIdentityCertificate publicCert, PrivateIdentityCertificate privateCert) {
        this.privateCert = privateCert;
        this.publicCert = publicCert;
    }

    public PublicIdentityCertificate getPublicCert() {
        return publicCert;
    }

    public PrivateIdentityCertificate getPrivateCert() {
        return privateCert;
    }
}
