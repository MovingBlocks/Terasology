package org.terasology.network.serialization;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;
import org.terasology.network.ReplicateType;
import org.terasology.network.ReplicationCheck;

/**
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
        if (component instanceof ReplicationCheck) {
        }
        boolean result = field.isReplicated() && (initial
                || field.getReplicationInfo().value() == ReplicateType.SERVER_TO_CLIENT
                || (field.getReplicationInfo().value() == ReplicateType.SERVER_TO_OWNER && owned)
                || (field.getReplicationInfo().value().isReplicateFromOwner() && !owned));
        if (result && component instanceof ReplicationCheck) {
            return ((ReplicationCheck)component).shouldReplicate(field, initial, owned);
        }
        return result;
    }

    @Override
    public boolean shouldDeserializeField(FieldMetadata fieldInfo) {
        return fieldInfo.isReplicated() && fieldInfo.getReplicationInfo().value().isReplicateFromOwner();
    }
}
