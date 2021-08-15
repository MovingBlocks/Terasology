// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity;

/**
 * Holds a public/private certificate pair
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
