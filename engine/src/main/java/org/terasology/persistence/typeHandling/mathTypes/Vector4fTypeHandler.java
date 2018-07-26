/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.mathTypes;


import gnu.trove.list.TFloatList;
import org.terasology.math.geom.Vector4f;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.SerializationContext;

/**
 */
public class Vector4fTypeHandler implements org.terasology.persistence.typeHandling.TypeHandler<Vector4f> {

    @Override
    public PersistedData serialize(Vector4f value, SerializationContext context) {
        if (value == null) {
            return context.createNull();
        } else {
            return context.create(value.x, value.y, value.z, value.w);
        }
    }

    @Override
    public Vector4f deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TFloatList floats = dataArray.getAsFloatArray();
                return new Vector4f(floats.get(0), floats.get(1), floats.get(2), floats.get(3));
            }
        }
        return null;
    }
}
