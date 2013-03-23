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
