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

package org.terasology.network;

import org.terasology.reflection.metadata.FieldMetadata;

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
