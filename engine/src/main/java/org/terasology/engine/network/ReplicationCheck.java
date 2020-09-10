// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.nui.reflection.metadata.FieldMetadata;

/**
 * Interface for providing a method to more tightly control when a field should be replicated.
 */
@FunctionalInterface
public interface ReplicationCheck {

    /**
     * @param field   The field being checked
     * @param initial Whether this is the initial replication
     * @param toOwner Whether the component is being replicated to the owner or not
     * @return Whether to replicate the field
     */
    boolean shouldReplicate(FieldMetadata<?, ?> field, boolean initial, boolean toOwner);
}
