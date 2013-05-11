/*
 * Copyright 2013 Moving Blocks
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

import sun.security.util.BigInt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 *
 */
public class PrivateIdentityCertificate {
    private BigInteger modulus;
    private BigInteger exponent;

    public PrivateIdentityCertificate(BigInteger modulus, BigInteger exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public byte[] decrypt(byte[] data) throws BadEncryptedDataException {
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            Cipher cipher = Cipher.getInstance(IdentityConstants.CERTIFICATE_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Insufficient support for '" + IdentityConstants.CERTIFICATE_ALGORITHM + "', required for identity management", e);
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException("Unexpected error during encryption", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new BadEncryptedDataException("Invalid encrypted data", e);
        }
    }
}
