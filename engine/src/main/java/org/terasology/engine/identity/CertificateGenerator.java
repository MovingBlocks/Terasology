/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.identity;

import com.google.common.base.Charsets;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;

/**
 * A generator for identity certificates.
 */
public class CertificateGenerator {
    private static final int KEY_SIZE = 2048;
    private KeyPairGenerator keyPairGenerator;
    private KeyFactory keyFactory;
    private Signature signer;

    public CertificateGenerator() {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            keyFactory = KeyFactory.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            signer = Signature.getInstance(IdentityConstants.SIGNATURE_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Insufficient support for '" + IdentityConstants.CERTIFICATE_ALGORITHM + "', required for identity management", e);
        }
    }

    /**
     * Generates a self-signed certificate. These are used to identify servers.
     *
     * @return A matched pair of public and private certificates.
     */
    public CertificatePair generateSelfSigned() {
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair kp = keyPairGenerator.genKeyPair();

        try {
            RSAPublicKeySpec pub = keyFactory.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec priv = keyFactory.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
            String uuid = UUID.randomUUID().toString();
            signer.initSign(kp.getPrivate(), new SecureRandom());
            signer.update(uuid.getBytes(Charsets.UTF_8));
            signer.update(pub.getModulus().toByteArray());
            signer.update(pub.getPublicExponent().toByteArray());
            byte[] rawSig = signer.sign();
            BigInteger signature = new BigInteger(rawSig);

            PublicIdentityCertificate publicCert = new PublicIdentityCertificate(uuid, pub.getModulus(), pub.getPublicExponent(), signature);
            PrivateIdentityCertificate privateCert = new PrivateIdentityCertificate(priv.getModulus(), priv.getPrivateExponent());
            return new CertificatePair(publicCert, privateCert);
        } catch (InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException("Unexpected exception generating certificate", e);
        }
    }

    /**
     * Generates a certificate signed by the given signer - a server will typically generate client identity certificates
     * signed by its certificate.
     *
     * @param signingCertificate
     * @return A matched pair of public and private certificates.
     */
    public CertificatePair generate(PrivateIdentityCertificate signingCertificate) {
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair kp = keyPairGenerator.genKeyPair();

        RSAPrivateKeySpec signingRSAKey = new RSAPrivateKeySpec(signingCertificate.getModulus(), signingCertificate.getExponent());

        try {
            PrivateKey signingKey = keyFactory.generatePrivate(signingRSAKey);

            RSAPublicKeySpec pub = keyFactory.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec priv = keyFactory.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

            String uuid = UUID.randomUUID().toString();
            signer.initSign(signingKey, new SecureRandom());
            signer.update(uuid.getBytes(Charsets.UTF_8));
            signer.update(pub.getModulus().toByteArray());
            signer.update(pub.getPublicExponent().toByteArray());
            byte[] rawSig = signer.sign();
            BigInteger signature = new BigInteger(rawSig);

            PublicIdentityCertificate publicCert = new PublicIdentityCertificate(uuid, pub.getModulus(), pub.getPublicExponent(), signature);
            PrivateIdentityCertificate privateCert = new PrivateIdentityCertificate(priv.getModulus(), priv.getPrivateExponent());
            return new CertificatePair(publicCert, privateCert);
        } catch (InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException("Unexpected exception generating certificate", e);
        }
    }
}
