// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class ByteTypeHandler extends TypeHandler<Byte> {

    @Override
    public PersistedData serializeNonNull(Byte value, PersistedDataSerializer serializer) {
        return serializer.serialize(new byte[]{value});
    }

    @Override
    public Optional<Byte> deserialize(PersistedData data) {
        if (data.isBytes()) {
            return Optional.of(data.getAsBytes()[0]);
        } else if (data.isNumber()) {
            return Optional.of((byte) data.getAsInteger());
        }

        return Optional.empty();
    }

}
