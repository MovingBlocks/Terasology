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

import gnu.trove.list.TIntList;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.rendering.nui.Color;

import java.util.Optional;

/**
 * Serializes {@link Color} instances to an int array <code>[r, g, b, a]</code>.
 * De-serializing also supports hexadecimal strings such as <code>"AAAAAAFF"</code>.
 */
public class ColorTypeHandler extends org.terasology.persistence.typeHandling.TypeHandler<Color> {

    @Override
    public PersistedData serializeNonNull(Color value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.r(), value.g(), value.b(), value.a());
    }

    @Override
    public Optional<Color> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TIntList vals = dataArray.getAsIntegerArray();
                return Optional.of(new Color(vals.get(0), vals.get(1), vals.get(2), vals.get(3)));
            }
        }
        if (data.isString()) {
            String value = data.getAsString();
            return Optional.of(new Color((int) Long.parseLong(value, 16)));
        }

        return Optional.empty();
    }
}
