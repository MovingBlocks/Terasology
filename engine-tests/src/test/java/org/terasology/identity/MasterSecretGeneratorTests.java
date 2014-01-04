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
    public void md5HashTest() {
        byte[] rawbytes = SecretGenerator.phashMD5(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 80);
        assertEquals(80, rawbytes.length);
    }

    @Test
    public void sha1HashTest() {
        byte[] rawbytes = SecretGenerator.phashSHA1(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 80);
        assertEquals(80, rawbytes.length);
    }

    @Test
    public void computeMasterSecret() {
        byte[] result = SecretGenerator.generate(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, "Test",
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, SecretGenerator.MASTER_SECRET_LENGTH);
        assertEquals(48, result.length);
    }

    @Test
    public void dddd() throws Exception {
        String message = "The Quick Brown Fox Jumped Over The Lazy Dog";

        SecretKeySpec key = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, "ARCFOUR");
        Cipher cipher = Cipher.getInstance("ARCFOUR");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(message.getBytes("UTF-8"));

        cipher.init(Cipher.DECRYPT_MODE, key);
        String result = new String(cipher.doFinal(encrypted), Charset.forName("UTF-8"));
        assertEquals(message, result);
    }
}
