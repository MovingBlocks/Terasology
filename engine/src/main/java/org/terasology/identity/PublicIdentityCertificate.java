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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

/**
 * The public certificate, that can be freely shared to declare identity. Able to encrypt data and verify signatures.
 */
public class PublicIdentityCertificate {
    private static final int SIGNATURE_LENGTH = 256;

    private String id;
    private BigInteger modulus;
    private BigInteger exponent;
    private BigInteger signature;

    public PublicIdentityCertificate(String id, BigInteger modulus, BigInteger exponent, BigInteger signature) {
        this.id = id;
        this.modulus = modulus;
        this.exponent = exponent;
        this.signature = signature;
    }

    public String getId() {
        return id;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public BigInteger getSignature() {
        return signature;
    }

    public byte[] getSignatureBytes() {
        return toBytes(signature, SIGNATURE_LENGTH);
    }

    private byte[] toBytes(BigInteger value, int length) {
        byte[] rawResult = value.toByteArray();
        if (rawResult.length < length) {
            byte[] result = new byte[length];
            System.arraycopy(rawResult, 0, result, result.length - rawResult.length, rawResult.length);
            return result;
        }
        return rawResult;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PublicIdentityCertificate) {
            PublicIdentityCertificate other = (PublicIdentityCertificate) obj;
            return Objects.equals(id, other.id) && Objects.equals(modulus, other.modulus) && Objects.equals(exponent, other.exponent);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    /**
     * Encrypts data such that it can only be decrypted by the paired private certificate, which is held by the certificate owner.
     * <br><br>
     * Note that only a limited amount of data can be encrypted in this fashion - for large exchanges this should be used
     * to establish shared symmetric key which can then be used for the main exchange.
     *
     * @param data
     * @return The encrypted data
     */
    public byte[] encrypt(byte[] data) {
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Insufficient support for '" + IdentityConstants.CERTIFICATE_ALGORITHM + "', required for identity management", e);
        } catch (InvalidKeySpecException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("Unexpected error during encryption", e);
        }
    }

    /**
     * Verifies that the certificate is valid (self signed check)
     *
     * @return Whether the certificate is signed by itself
     */
    public boolean verifySelfSigned() {
        return verifySignedBy(this);
    }

    /**
     * Verifies that the certificate is signed by the given signer's public key
     *
     * @param signer
     * @return
     */
    public boolean verifySignedBy(PublicIdentityCertificate signer) {
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(signer.modulus, signer.exponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(keySpec);
            Signature signatureVerifier = Signature.getInstance(IdentityConstants.SIGNATURE_ALGORITHM);
            signatureVerifier.initVerify(key);
            signatureVerifier.update(id.getBytes(Charsets.UTF_8));
            signatureVerifier.update(modulus.toByteArray());
            signatureVerifier.update(exponent.toByteArray());
            return signatureVerifier.verify(getSignatureBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Insufficient support for '" + IdentityConstants.CERTIFICATE_ALGORITHM + "', required for identity management", e);
        } catch (InvalidKeySpecException e) {
            return false;
        } catch (SignatureException e) {
            return false;
        } catch (InvalidKeyException e) {
            return false;
        }
    }

    /**
     * Verifies that the signedData was created by this certificate's corresponding private certificate, over the
     * given data.
     *
     * @param data
     * @param signedData
     * @return
     */
    public boolean verify(byte[] data, byte[] signedData) {
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(keySpec);
            Signature signatureVerifier = Signature.getInstance(IdentityConstants.SIGNATURE_ALGORITHM);
            signatureVerifier.initVerify(key);
            signatureVerifier.update(data);
            return signatureVerifier.verify(signedData);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Insufficient support for '" + IdentityConstants.CERTIFICATE_ALGORITHM + "', required for identity management", e);
        } catch (InvalidKeySpecException e) {
            return false;
        } catch (SignatureException e) {
            return false;
        } catch (InvalidKeyException e) {
            return false;
        }
    }
}
