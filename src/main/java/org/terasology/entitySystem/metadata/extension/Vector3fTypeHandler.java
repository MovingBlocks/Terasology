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
package org.terasology.entitySystem.metadata.extension;

import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3fTypeHandler extends AbstractTypeHandler<Vector3f> {

    public EntityData.Value serialize(Vector3f value) {
        return EntityData.Value.newBuilder().addFloat(value.x).addFloat(value.y).addFloat(value.z).build();
    }

    public Vector3f deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 2) {
            return new Vector3f(value.getFloat(0), value.getFloat(1), value.getFloat(2));
        }
        return null;
    }

    public Vector3f copy(Vector3f value) {
        if (value != null) {
            return new Vector3f(value);
        }
        return null;
    }
}
