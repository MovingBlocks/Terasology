// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.network.internal;

import com.google.protobuf.ByteString;
import org.joml.Vector3ic;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.protobuf.NetData;

import java.math.BigInteger;

/**
 * Utility class for converting types between network representation and usable objects
 *
 */
public final class NetMessageUtil {
    private NetMessageUtil() {
    }

    public static org.joml.Vector3i convert(NetData.Vector3iData data) {
        return new org.joml.Vector3i(data.getX(), data.getY(), data.getZ());
    }

    public static NetData.Vector3iData convert(Vector3ic data) {
        return NetData.Vector3iData.newBuilder().setX(data.x()).setY(data.y()).setZ(data.z()).build();
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
