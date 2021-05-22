// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class CertificateTests {

    @Test
    public void testGeneratedSelfSignedCertificateValid() {
        CertificateGenerator gen = new CertificateGenerator();
        CertificatePair pair = gen.generateSelfSigned();
        assertTrue(pair.getPublicCert().verifySelfSigned());
    }
}
