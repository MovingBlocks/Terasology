/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.entitySystem.metadata;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.network.NoReplicate;
import org.terasology.network.Replicate;

import java.lang.reflect.Field;

/**
 * An extended FieldMetadata that provides information on whether a the field should be replicated, and under what conditions
 *
 */
public class ReplicatedFieldMetadata<T, U> extends FieldMetadata<T, U> {

    private boolean replicated;
    private Replicate replicationInfo;

    public ReplicatedFieldMetadata(ClassMetadata<T, ?> owner, Field field, CopyStrategy<U> copyStrategy, ReflectFactory factory, boolean replicatedByDefault)
            throws InaccessibleFieldException {
        super(owner, field, copyStrategy, factory);
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
