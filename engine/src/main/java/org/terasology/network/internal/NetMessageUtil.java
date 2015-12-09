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

import com.google.protobuf.ByteString;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.math.geom.Vector3i;
import org.terasology.protobuf.NetData;

import java.math.BigInteger;

/**
 * Utility class for converting types between network representation and usable objects
 *
 */
public final class NetMessageUtil {
    private NetMessageUtil() {
    }

    public static Vector3i convert(NetData.Vector3iData data) {
        return new Vector3i(data.getX(), data.getY(), data.getZ());
    }

    public static NetData.Vector3iData convert(Vector3i data) {
        return NetData.Vector3iData.newBuilder().setX(data.x).setY(data.y).setZ(data.z).build();
    }

    public static NetData.Certificate convert(PublicIdentityCertificate data) {
        return NetData.Certificate.newBuilder()
                .setId(data.getId())
                .setModulus(ByteString.copyFrom(data.getModulus().toByteArray()))
                .setExponent(ByteString.copyFrom(data.getExponent().toByteArray()))
                .setSignature(ByteString.copyFrom(data.getSignatureBytes())).build();
    }

    public static PublicIdentityCertificate convert(NetData.Certificate data) {
        return new PublicIdentityCertificate(
                data.getId(),
                new BigInteger(data.getModulus().toByteArray()),
                new BigInteger(data.getExponent().toByteArray()),
                new BigInteger(data.getSignature().toByteArray()));
    }
}
