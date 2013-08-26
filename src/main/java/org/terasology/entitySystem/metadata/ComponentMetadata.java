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

import org.terasology.entitySystem.Component;

/**
 * Metadata on a component class
 * @author Immortius
 */
public interface ComponentMetadata<T extends Component> extends ClassMetadata<T> {
    /**
     * @return Whether this component owns any references
     */
    boolean isReferenceOwner();

    /**
     * @return Whether this component replicates any fields from owner to server
     */
    boolean isReplicatedFromOwner();

    /**
     * @return Whether this component needs to be replicated
     */
    boolean isReplicated();

    /**
     * @return Whether this component forces a block active
     */
    boolean isForceBlockActive();

    /**
     * @return Whether this component should be retained unaltered on block change
     */
    boolean isRetainUnalteredOnBlockChange();

    /**
     * @return Whether this component makes a block valid for block lifecycle events
     */
    boolean isBlockLifecycleEventsRequired();
}
