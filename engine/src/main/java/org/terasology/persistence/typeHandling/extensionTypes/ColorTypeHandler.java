// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import gnu.trove.list.TIntList;
import org.terasology.nui.Color;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 * Serializes {@link Color} instances to an int array <code>[r, g, b, a]</code>.
 * De-serializing also supports hexadecimal strings such as <code>"AAAAAAFF"</code>.
 */
public class ColorTypeHandler extends TypeHandler<Color> {

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
