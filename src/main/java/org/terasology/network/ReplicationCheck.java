package org.terasology.network;

import org.terasology.entitySystem.metadata.FieldMetadata;

/**
 * Interface for providing a method to more tightly control whether a field should be replicated.
 *
 * @author Immortius
 */
public interface ReplicationCheck {

    /**
     * @param field   The field being checked
     * @param initial Whether this is the initial replication
     * @param toOwner Whether the component is being replicated to the owner or not
     * @return Whether to replicate the field
     */
    boolean shouldReplicate(FieldMetadata field, boolean initial, boolean toOwner);
}
