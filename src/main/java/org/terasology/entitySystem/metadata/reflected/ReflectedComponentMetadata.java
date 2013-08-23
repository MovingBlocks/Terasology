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

package org.terasology.entitySystem.metadata.reflected;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.network.Replicate;
import org.terasology.world.block.ForceBlockActive;
import org.terasology.world.block.RequiresBlockLifecycleEvents;

import java.lang.reflect.Field;

/**
 * @author Immortius
 */
public class ReflectedComponentMetadata<T extends Component> extends ReflectedClassMetadata<T> implements ComponentMetadata<T> {

    private boolean replicated;
    private boolean replicatedFromOwner;
    private boolean referenceOwner;
    private boolean forceBlockActive;
    private boolean retainUnalteredOnBlockChange;
    private boolean blockLifecycleEventsRequired;

    public ReflectedComponentMetadata(Class<T> simpleClass, TypeHandlerLibrary typeHandlers, String primaryName, String... additionalNames) throws NoSuchMethodException {
        super(simpleClass, typeHandlers, primaryName, additionalNames);
        replicated = simpleClass.getAnnotation(Replicate.class) != null;
        blockLifecycleEventsRequired = simpleClass.getAnnotation(RequiresBlockLifecycleEvents.class) != null;
        ForceBlockActive forceBlockActiveAnnotation = simpleClass.getAnnotation(ForceBlockActive.class);
        if (forceBlockActiveAnnotation != null) {
            forceBlockActive = true;
            retainUnalteredOnBlockChange = forceBlockActiveAnnotation.retainUnalteredOnBlockChange();
        }
        addFields(typeHandlers);
    }

    @Override
    protected FieldMetadata<T, ?> addField(Field field, TypeHandler typeHandler) {
        ReflectedFieldMetadata<T, ?> metadata = new ReflectedFieldMetadata(this, field, typeHandler, false);
        if (metadata.isReplicated()) {
            replicated = true;
            if (metadata.getReplicationInfo().value().isReplicateFromOwner()) {
                replicatedFromOwner = true;
            }
        }
        if (metadata.isOwnedReference()) {
            referenceOwner = true;
        }
        return metadata;
    }

    /**
     * @param object
     * @return A copy of the given instance of this component, or null if the component does not belong to this metadata
     */
    public T copy(Component object) {
        if (getType().isInstance(object)) {
            return super.copy(getType().cast(object));
        }
        return null;
    }

    /**
     * @return Whether this component owns any references
     */
    @Override
    public boolean isReferenceOwner() {
        return referenceOwner;
    }

    /**
     * @return Whether this component replicates any fields from owner to server
     */
    @Override
    public boolean isReplicatedFromOwner() {
        return replicatedFromOwner;
    }

    /**
     * @return Whether this component needs to be replicated
     */
    @Override
    public boolean isReplicated() {
        return replicated;
    }

    /**
     * @return Whether this component forces a block active
     */
    @Override
    public boolean isForceBlockActive() {
        return forceBlockActive;
    }

    /**
     * @return Whether this component should be retained unaltered on block change
     */
    @Override
    public boolean isRetainUnalteredOnBlockChange() {
        return retainUnalteredOnBlockChange;
    }

    /**
     * @return Whether this component makes a block valid for block lifecycle events
     */
    @Override
    public boolean isBlockLifecycleEventsRequired() {
        return blockLifecycleEventsRequired;
    }
}
