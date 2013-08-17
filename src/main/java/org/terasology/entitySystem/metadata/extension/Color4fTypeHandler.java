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

import javax.vecmath.Color4f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Color4fTypeHandler extends AbstractTypeHandler<Color4f> {

    public EntityData.Value serialize(Color4f value) {
        return EntityData.Value.newBuilder().addFloat(value.x).addFloat(value.y).addFloat(value.z).addFloat(value.w).build();
    }

    public Color4f deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 3) {
            return new Color4f(value.getFloat(0), value.getFloat(1), value.getFloat(2), value.getFloat(3));
        }
        return null;
    }

    public Color4f copy(Color4f value) {
        if (value != null) {
            return new Color4f(value);
        }
        return null;
    }
}
