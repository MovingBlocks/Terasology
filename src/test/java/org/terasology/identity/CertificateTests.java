package org.terasology.identity;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CertificateTests {

    @Test
    public void checkGeneratedSelfSignedCertificateValid() {
        CertificateGenerator gen = new CertificateGenerator();
        CertificatePair pair = gen.generateSelfSigned();
        assertTrue(pair.getPublicCert().verifySelfSigned());
    }
}
