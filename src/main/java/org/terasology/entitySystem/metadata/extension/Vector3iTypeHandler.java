/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.metadata.extension;

import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3iTypeHandler extends AbstractTypeHandler<Vector3i> {

    public EntityData.Value serialize(Vector3i value) {
        return EntityData.Value.newBuilder().addInteger(value.x).addInteger(value.y).addInteger(value.z).build();
    }

    public Vector3i deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 2) {
            return new Vector3i(value.getInteger(0), value.getInteger(1), value.getInteger(2));
        }
        return null;
    }

    public Vector3i copy(Vector3i value) {
        if (value != null) {
            return new Vector3i(value);
        }
        return null;
    }
}
