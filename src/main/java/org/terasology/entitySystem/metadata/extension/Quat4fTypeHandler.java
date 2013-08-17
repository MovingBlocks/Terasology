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
package org.terasology.entitySystem.metadata.extension;

import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Quat4f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Quat4fTypeHandler extends AbstractTypeHandler<Quat4f> {

    public EntityData.Value serialize(Quat4f value) {
        return EntityData.Value.newBuilder().addFloat(value.x).addFloat(value.y).addFloat(value.z).addFloat(value.w).build();
    }

    public Quat4f deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 3) {
            return new Quat4f(value.getFloat(0), value.getFloat(1), value.getFloat(2), value.getFloat(3));
        }
        return null;
    }

    public Quat4f copy(Quat4f value) {
        if (value != null) {
            return new Quat4f(value);
        }
        return null;
    }
}
