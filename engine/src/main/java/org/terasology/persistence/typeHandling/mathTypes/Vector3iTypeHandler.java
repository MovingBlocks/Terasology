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

import gnu.trove.list.TIntList;
import org.terasology.math.geom.Vector3i;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.SimpleTypeHandler;

/**
 */
public class Vector3iTypeHandler extends SimpleTypeHandler<Vector3i> {

    @Override
    public PersistedData serialize(Vector3i value, SerializationContext context) {
        if (value == null) {
            return context.createNull();
        } else {
            return context.create(value.x, value.y, value.z);
        }
    }

    @Override
    public Vector3i deserialize(PersistedData data, DeserializationContext context) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 2) {
                TIntList ints = dataArray.getAsIntegerArray();
                return new Vector3i(ints.get(0), ints.get(1), ints.get(2));
            }
        }
        return null;
    }
}
