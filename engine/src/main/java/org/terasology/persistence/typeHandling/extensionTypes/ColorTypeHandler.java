/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.persistence.typeHandling.extensionTypes;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.SimpleTypeHandler;
import org.terasology.rendering.nui.Color;

/**
 * @author Immortius
 */
public class ColorTypeHandler extends SimpleTypeHandler<Color> {

    @Override
    public PersistedData serialize(Color value, SerializationContext context) {
        return context.create(value.r(), value.g(), value.b(), value.a());
    }

    @Override
    public Color deserialize(PersistedData data, DeserializationContext context) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TFloatList floats = dataArray.getAsFloatArray();
                if (floats.size() == dataArray.size()) {
                    return new Color(floats.get(0), floats.get(1), floats.get(2), floats.get(3));
                }
                TIntList ints = dataArray.getAsIntegerArray();
                if (ints.size() == dataArray.size()) {
                    return new Color(ints.get(0), ints.get(1), ints.get(2), ints.get(3));
                }
            }
        }
        return null;
    }
}
