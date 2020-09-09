// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.mathTypes.legacy;

import gnu.trove.list.TIntList;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataArray;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;

import java.util.Optional;

/**
 */
public class LegacyVector2iTypeHandler extends org.terasology.engine.persistence.typeHandling.TypeHandler<Vector2i> {

    @Override
    public PersistedData serializeNonNull(Vector2i value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x, value.y);
    }

    @Override
    public Optional<Vector2i> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 1) {
                TIntList ints = dataArray.getAsIntegerArray();
                return Optional.of(new Vector2i(ints.get(0), ints.get(1)));
            }
        }
        return Optional.empty();
    }
}
