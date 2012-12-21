package org.terasology.network;

import org.terasology.math.Vector3i;
import org.terasology.protobuf.NetData;

/**
 * @author Immortius
 */
public final class NetworkUtil {
    private NetworkUtil() {
    }

    public static Vector3i convert(NetData.Vector3iData data) {
        return new Vector3i(data.getX(), data.getY(), data.getZ());
    }

    public static NetData.Vector3iData convert(Vector3i data) {
        return NetData.Vector3iData.newBuilder().setX(data.x).setY(data.y).setZ(data.z).build();
    }
}
