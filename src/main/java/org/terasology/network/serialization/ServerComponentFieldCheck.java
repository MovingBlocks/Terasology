package org.terasology.network.serialization;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;
import org.terasology.network.ReplicateDirection;

/**
 * @author Immortius
 */
public class ServerComponentFieldCheck implements FieldSerializeCheck<Component> {
    private boolean owned = false;

    public ServerComponentFieldCheck(boolean owned) {
        this.owned = owned;
    }

    @Override
    public boolean shouldSerializeField(FieldMetadata field, Component component) {
        return field.isReplicated()
                && (field.getReplicationInfo().value() == ReplicateDirection.SERVER_TO_CLIENT
                || (field.getReplicationInfo().value() == ReplicateDirection.OWNER_TO_SERVER && !owned));
    }

    @Override
    public boolean shouldDeserializeField(FieldMetadata fieldInfo) {
        return fieldInfo.isReplicated() && fieldInfo.getReplicationInfo().value().isReplicateFromOwner();
    }
}
