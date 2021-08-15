// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.serialization;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.metadata.ReplicatedFieldMetadata;
import org.terasology.engine.persistence.serializers.FieldSerializeCheck;

/**
 * Determines which fields should be serialized and deserialized by the client.
 *
 */
public class ClientComponentFieldCheck implements FieldSerializeCheck<Component> {


    @Override
    public boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, Component object) {
        return shouldSerializeField(field, object, false);
    }

    @Override
    public boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, Component component, boolean componentInitial) {
        // Clients only send fields that are replicated from the owner
        return field.isReplicated() && field.getReplicationInfo().value().isReplicateFromOwner();
    }

    @Override
    public boolean shouldDeserialize(ClassMetadata<?, ?> classMetadata, FieldMetadata<?, ?> fieldMetadata) {
        // Clients should use all replicated fields
        return true;
    }
}
