// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import gnu.trove.list.TFloatList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.persistence.typeHandling.RegisterTypeHandler;
import org.terasology.engine.utilities.modifiable.ModifiableValue;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;

import java.util.Optional;

/**
 * The ModifiableValue type is deserialized from prefabs directly from the corresponding component values.
 * <b>Only</b> the base value is to be specified in the prefabs, this will deserialize into a ModifiableValue type,
 * with set preModifier=0, multiplier=1, postModifier=0.
 * "<field_name>": <base_value>
 * It is not recommended to set the modifiers in the prefab but it can be done via an unlabeled array.
 * "<field_name>": [<base_value>, <pre_modifier>, <multiplier>, <post_modifier>]
 */
@RegisterTypeHandler
public class ModifiableValueTypeHandler extends org.terasology.persistence.typeHandling.TypeHandler<ModifiableValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifiableValueTypeHandler.class);

    @Override
    public PersistedData serializeNonNull(ModifiableValue value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.getBaseValue(), value.getPreModifier(), value.getMultiplier(),
                value.getPostModifier());
    }

    @Override
    public Optional<ModifiableValue> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray vals = data.getAsArray();
            if (vals.isNumberArray()) {
                TFloatList floatList = vals.getAsFloatArray();
                ModifiableValue modifiableValue = new ModifiableValue(floatList.get(0));
                if (floatList.size() == 4) {
                    modifiableValue.setPreModifier(floatList.get(1));
                    modifiableValue.setMultiplier(floatList.get(2));
                    modifiableValue.setPostModifier(floatList.get(3));
                }
                return Optional.of(modifiableValue);
            }
        }
        return Optional.empty();
    }
}
