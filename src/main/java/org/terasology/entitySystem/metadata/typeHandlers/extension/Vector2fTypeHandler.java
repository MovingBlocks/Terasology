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
package org.terasology.entitySystem.metadata.typeHandlers.extension;

import org.terasology.entitySystem.metadata.typeHandlers.SimpleTypeHandler;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector2f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector2fTypeHandler extends SimpleTypeHandler<Vector2f> {

    public EntityData.Value serialize(Vector2f value) {
        return EntityData.Value.newBuilder().addFloat(value.x).addFloat(value.y).build();
    }

    public Vector2f deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 1) {
            return new Vector2f(value.getFloat(0), value.getFloat(1));
        }
        return null;
    }

    public Vector2f copy(Vector2f value) {
        if (value != null) {
            return new Vector2f(value);
        }
        return null;
    }
}
