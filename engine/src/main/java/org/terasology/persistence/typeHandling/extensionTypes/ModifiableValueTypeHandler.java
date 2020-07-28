// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.extensionTypes;

import gnu.trove.list.TFloatList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.RegisterTypeHandler;
import org.terasology.utilities.modifiable.ModifiableValue;

import java.util.Optional;

@RegisterTypeHandler
public class ModifiableValueTypeHandler extends org.terasology.persistence.typeHandling.TypeHandler<ModifiableValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifiableValueTypeHandler.class);

    @Override
    public PersistedData serializeNonNull(ModifiableValue value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.getBaseValue(), value.getPreModifiers(), value.getMultipliers(),
                value.getPostModifiers());
    }

    @Override
    public Optional<ModifiableValue> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray vals = data.getAsArray();
            if (vals.isNumberArray()) {
                TFloatList floatList = vals.getAsFloatArray();
                ModifiableValue modifiableValue = new ModifiableValue(floatList.get(0));
                if (floatList.size() > 1) {
                    modifiableValue.setPreModifiers(floatList.get(1));
                    modifiableValue.setMultipliers(floatList.get(2));
                    modifiableValue.setPostModifiers(floatList.get(3));
                }
                return Optional.of(modifiableValue);
            }
        }
        return Optional.empty();
    }
}
