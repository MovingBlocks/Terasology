// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.network.NoReplicate;
import org.terasology.engine.network.Replicate;

import java.lang.reflect.Field;

/**
 * An extended FieldMetadata that provides information on whether a the field should be replicated, and under what conditions
 *
 */
public class ReplicatedFieldMetadata<T, U> extends FieldMetadata<T, U> {

    private boolean replicated;
    private Replicate replicationInfo;

    public ReplicatedFieldMetadata(ClassMetadata<T, ?> owner, Field field, CopyStrategyLibrary copyStrategyLibrary, ReflectFactory factory, boolean replicatedByDefault)
            throws InaccessibleFieldException {
        super(owner, field, copyStrategyLibrary, factory);
        this.replicated = replicatedByDefault;
        if (field.getAnnotation(NoReplicate.class) != null) {
            replicated = false;
        }
        if (field.getAnnotation(Replicate.class) != null) {
            replicated = true;
        }
        this.replicationInfo = field.getAnnotation(Replicate.class);
    }

    /**
     * @return Whether this field should be replicated on the network
     */
    public boolean isReplicated() {
        return replicated;
    }

    /**
     * @return The replication information for this field, or null if it isn't marked with the Replicate annotation
     */
    public Replicate getReplicationInfo() {
        return replicationInfo;
    }
}
