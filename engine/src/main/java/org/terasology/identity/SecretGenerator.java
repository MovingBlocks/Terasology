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

import org.terasology.math.TeraMath;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Generates secrets using the TLS p_hash method. This is used in two situations:
 * <ol>
 * <li>To generate the master secret from the premaster secret</li>
 * <li>To generate keys for symmetric encryption</li>
 * </ol>
 */
public final class SecretGenerator {

    /**
     * Label for generating master secrets
     */
    public static final String MASTER_SECRET_LABEL = "master secret";

    /**
     * Label when generating a key from a master secret
     */
    public static final String KEY_EXPANSION = "key expansion";

    /**
     * The standard length of a master secret
     */
    public static final int MASTER_SECRET_LENGTH = 48;

    private static final String MD5_HASH_ALGORITHM = "HmacMD5";
    private static final String SHA1_HASH_ALGORITHM = "HmacSHA1";

    private SecretGenerator() {
    }

    /**
     * Generates a secret from another secret, a seed, and a label
     *
     * @param secret
     * @param label
     * @param seed
     * @param targetLength The desired length of the generated secret.
     * @return The generated secret
     */
    public static byte[] generate(byte[] secret, String label, byte[] seed, int targetLength) {
        // Split the secret
        int partLength = TeraMath.ceilToInt(secret.length / 2.0f);
        byte[] part1 = Arrays.copyOfRange(secret, 0, partLength);
        byte[] part2 = Arrays.copyOfRange(secret, secret.length - partLength, secret.length);

        byte[] labelBytes = label.getBytes(Charset.forName("US-ASCII"));

        byte[] combinedLabelSeed = new byte[labelBytes.length + seed.length];
        System.arraycopy(labelBytes, 0, combinedLabelSeed, 0, labelBytes.length);
        System.arraycopy(seed, 0, combinedLabelSeed, labelBytes.length, seed.length);

        // MD5 the first half of the secret
        byte[] md5Result = phashMD5(part1, combinedLabelSeed, targetLength);

        // SHA1 the second half of the secret
        byte[] sha1Result = phashSHA1(part2, combinedLabelSeed, targetLength);

        byte[] masterSecret = new byte[md5Result.length];
        for (int i = 0; i < masterSecret.length; ++i) {
            masterSecret[i] = (byte) (md5Result[i] ^ sha1Result[i]);
        }

        return masterSecret;
    }

    public static byte[] phashMD5(byte[] secret, byte[] seed, int targetLength) {
        return phash(secret, seed, MD5_HASH_ALGORITHM, targetLength);
    }

    public static byte[] phashSHA1(byte[] secret, byte[] seed, int targetLength) {
        return phash(secret, seed, SHA1_HASH_ALGORITHM, targetLength);
    }

    private static byte[] phash(byte[] secret, byte[] seed, String algorithm, int targetLength) {
        SecretKeySpec signingKey = new SecretKeySpec(secret, algorithm);

        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] prevHash = mac.doFinal(seed);
            byte[] result = new byte[targetLength];
            int lengthGenerated = 0;
            while (lengthGenerated < targetLength) {
                byte[] value = new byte[prevHash.length + secret.length];
                System.arraycopy(prevHash, 0, value, 0, prevHash.length);
                System.arraycopy(secret, 0, value, prevHash.length, secret.length);
                prevHash = mac.doFinal(value);
                System.arraycopy(prevHash, 0, result, lengthGenerated, Math.min(prevHash.length, targetLength - lengthGenerated));
                lengthGenerated += prevHash.length;
            }
            return result;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " not supported, required for authentication", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Error computing master secret", e);
        }

    }
}
