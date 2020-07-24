// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.extensionTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.RegisterTypeHandler;
import org.terasology.utilities.modifiable.ModifiableValue;

import java.util.Optional;

@RegisterTypeHandler
public class ModifiableValueTypeHandler extends org.terasology.persistence.typeHandling.TypeHandler<ModifiableValue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifiableValueTypeHandler.class);

    @Override
    public PersistedData serializeNonNull(ModifiableValue value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.getValue());
    }

    @Override
    public Optional<ModifiableValue> deserialize(PersistedData data) {
        return Optional.of(new ModifiableValue(data.getAsArray().getAsFloatArray().get(0)));
    }
}
