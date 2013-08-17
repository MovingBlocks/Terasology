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
package org.terasology.network.internal;

import com.google.common.primitives.Bytes;
import org.terasology.identity.IdentityConstants;
import org.terasology.identity.SecretGenerator;
import org.terasology.protobuf.NetData;

import javax.crypto.spec.SecretKeySpec;

/**
 * Common methods for both server and client ends of the authentication handshake
 */
public final class HandshakeCommon {

    private HandshakeCommon() {
    }

    /**
     * Generates the master secret, a common secret between the server and client used to generate a symmetric encryption key
     *
     * @param preMasterSecret
     * @param clientRandom
     * @param serverRandom
     * @return
     */
    public static byte[] generateMasterSecret(byte[] preMasterSecret, byte[] clientRandom, byte[] serverRandom) {
        return SecretGenerator.generate(preMasterSecret, SecretGenerator.MASTER_SECRET_LABEL, Bytes.concat(clientRandom, serverRandom), SecretGenerator.MASTER_SECRET_LENGTH);
    }

    /**
     * Generates a symmetric encryption key from the master secret
     *
     * @param masterSecret
     * @param clientRandom
     * @param serverRandom
     * @return
     */
    public static SecretKeySpec generateSymmetricKey(byte[] masterSecret, byte[] clientRandom, byte[] serverRandom) {
        return new SecretKeySpec(SecretGenerator.generate(masterSecret, SecretGenerator.KEY_EXPANSION,
                Bytes.concat(clientRandom, serverRandom), IdentityConstants.SYMMETRIC_ENCRYPTION_KEY_LENGTH), IdentityConstants.SYMMETRIC_ENCRYPTION_ALGORITHM);
    }

    /**
     * Create a set of data to sign to bu
     *
     * @param serverHello
     * @param clientHello
     * @return
     */
    public static byte[] getSignatureData(NetData.HandshakeHello serverHello, NetData.HandshakeHello clientHello) {
        return Bytes.concat(serverHello.toByteArray(), clientHello.toByteArray());
    }
}
