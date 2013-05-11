package org.terasology.identity;

import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MasterSecretGeneratorTests {

    @Test
    public void MD5HashTest() {
        byte[] rawbytes = SecretGenerator.phashMD5(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 80);
        assertEquals(80, rawbytes.length);
    }

    @Test
    public void SHA1HashTest() {
        byte[] rawbytes = SecretGenerator.phashSHA1(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 80);
        assertEquals(80, rawbytes.length);
    }

    @Test
    public void computeMasterSecret() {
        byte[] result = SecretGenerator.generate(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, "Test", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, SecretGenerator.MASTER_SECRET_LENGTH);
        assertEquals(48, result.length);
    }

    @Test
    public void dddd() throws Exception {
        String message = "The Quick Brown Fox Jumped Over The Lazy Dog";

        SecretKeySpec key = new SecretKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, "ARCFOUR");
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));

        cipher.init(Cipher.DECRYPT_MODE, key);
        String result = new String(cipher.doFinal(encrypted), Charset.forName("UTF-8"));
        assertEquals(message, result);
    }
}
