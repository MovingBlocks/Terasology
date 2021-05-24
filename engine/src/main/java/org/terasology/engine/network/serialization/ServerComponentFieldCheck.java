// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.serialization;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.metadata.ReplicatedFieldMetadata;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.ReplicationCheck;
import org.terasology.engine.persistence.serializers.FieldSerializeCheck;

/**
 * Determines which fields should be sent and received by the server
 *
 */
public class ServerComponentFieldCheck implements FieldSerializeCheck<Component> {
    private boolean owned;
    private boolean entityInitial;

    public ServerComponentFieldCheck(boolean owned, boolean entityInitial) {
        this.owned = owned;
        this.entityInitial = entityInitial;
    }

    @Override
    public boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, Component object) {
        return shouldSerializeField(field, object, false);
    }

    @Override
    public boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, Component component, boolean componentInitial) {
        // The server will send fields that are replicated when
        // 1. It is the initial send of the component
        // 2. The field is replicated from Server to Client
        // 3. The field is replicated from Server to Owner and the client owns the entity
        // 4. The field is replicated from owner and the client doesn't own it
        // Except if the field is initialOnly and it isn't the initial send
        boolean initial = entityInitial || componentInitial;
        boolean result = field.isReplicated() && (initial
                || !field.getReplicationInfo().initialOnly()
                && (field.getReplicationInfo().value() == FieldReplicateType.SERVER_TO_CLIENT
                || (field.getReplicationInfo().value() == FieldReplicateType.SERVER_TO_OWNER && owned)
                || (field.getReplicationInfo().value().isReplicateFromOwner() && !owned)));
        if (result && component instanceof ReplicationCheck) {
            return ((ReplicationCheck) component).shouldReplicate(field, initial, owned);
        }
        return result;
    }

    @Override
    public boolean shouldDeserialize(ClassMetadata<?, ?> classMetadata, FieldMetadata<?, ?> fieldMetadata) {
        // The server only accepts fields that are replicated from owner
        ReplicatedFieldMetadata<?, ?> replicatedFieldMetadata = (ReplicatedFieldMetadata<?, ?>) fieldMetadata;
        return replicatedFieldMetadata.isReplicated() && replicatedFieldMetadata.getReplicationInfo().value().isReplicateFromOwner();
    }
}
