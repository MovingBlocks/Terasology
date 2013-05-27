/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.network.serialization;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.ReplicationCheck;

/**
 * Determines which fields should be sent and received by the server
 * @author Immortius
 */
public class ServerComponentFieldCheck implements FieldSerializeCheck<Component> {
    private boolean owned = false;
    private boolean initial = false;

    public ServerComponentFieldCheck(boolean owned, boolean initial) {
        this.owned = owned;
        this.initial = initial;
    }

    @Override
    public boolean shouldSerializeField(FieldMetadata field, Component component) {
        // The server will send fields that are replicated when
        // 1. It is the initial send of the component
        // 2. The field is replicated from Server to Client
        // 3. The field is replicated from Server to Owner and the client owns the entity
        // 4. The field is replicated from owner and the client doesn't own it
        // Except if the field is initialOnly and it isn't the initial send
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
    public boolean shouldDeserializeField(FieldMetadata fieldInfo) {
        // The server only accepts fields that are replicated from owner
        return fieldInfo.isReplicated() && fieldInfo.getReplicationInfo().value().isReplicateFromOwner();
    }
}
