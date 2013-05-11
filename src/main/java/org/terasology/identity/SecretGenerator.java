package org.terasology.identity;

import org.terasology.math.TeraMath;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 *
 */
public final class SecretGenerator {

    public static final String MASTER_SECRET_LABEL = "master secret";
    public static final String KEY_EXPANSION = "key expansion";
    public static final int MASTER_SECRET_LENGTH = 48;
    private static final String MD5_HASH_ALGORITHM = "HmacMD5";
    private static final String SHA1_HASH_ALGORITHM = "HmacSHA1";

    private SecretGenerator() {
    }

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
            masterSecret[i] = (byte)(md5Result[i] ^ sha1Result[i]);
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
