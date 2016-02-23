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

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.world.block.ForceBlockActive;
import org.terasology.world.block.RequiresBlockLifecycleEvents;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Metadata on a component class and its fields.
 *
 */
public class ComponentMetadata<T extends Component> extends ClassMetadata<T, ComponentFieldMetadata<T, ?>> {

    private boolean replicated;
    private boolean replicatedFromOwner;
    private boolean referenceOwner;
    private boolean forceBlockActive;
    private boolean retainUnalteredOnBlockChange;
    private boolean blockLifecycleEventsRequired;
    private List<Annotation> annotations;

    /**
     * @param uri            The uri to identify the component with.
     * @param type           The type to create the metadata for
     * @param factory        A reflection library to provide class construction and field get/set functionality
     * @param copyStrategies A copy strategy library
     * @throws NoSuchMethodException If the component has no default constructor
     */
    public ComponentMetadata(SimpleUri uri, Class<T> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies) throws NoSuchMethodException {
        super(uri, type, factory, copyStrategies, Predicates.<Field>alwaysTrue());
        replicated = type.getAnnotation(Replicate.class) != null;
        blockLifecycleEventsRequired = type.getAnnotation(RequiresBlockLifecycleEvents.class) != null;
        ForceBlockActive forceBlockActiveAnnotation = type.getAnnotation(ForceBlockActive.class);
        if (forceBlockActiveAnnotation != null) {
            forceBlockActive = true;
            retainUnalteredOnBlockChange = forceBlockActiveAnnotation.retainUnalteredOnBlockChange();
        }

        for (ComponentFieldMetadata<T, ?> field : getFields()) {
            if (field.isReplicated()) {
                replicated = true;
                if (field.getReplicationInfo().value().isReplicateFromOwner()) {
                    replicatedFromOwner = true;
                }
            }
            if (field.isOwnedReference()) {
                referenceOwner = true;
            }
        }

        annotations = Lists.newArrayList(type.getAnnotations());
    }

    @Override
    protected <U> ComponentFieldMetadata<T, U> createField(Field field, CopyStrategy<U> copyStrategy, ReflectFactory factory) throws InaccessibleFieldException {
        return new ComponentFieldMetadata<>(this, field, copyStrategy, factory, false);
    }

    /**
     * @return Whether this component owns any references
     */
    public boolean isReferenceOwner() {
        return referenceOwner;
    }

    /**
     * @return Whether this component replicates any fields from owner to server
     */
    public boolean isReplicatedFromOwner() {
        return replicatedFromOwner;
    }

    /**
     * @return Whether this component needs to be replicated
     */
    public boolean isReplicated() {
        return replicated;
    }

    /**
     * @return Whether this component forces a block active
     */
    public boolean isForceBlockActive() {
        return forceBlockActive;
    }

    /**
     * @return Whether this component should be retained unaltered on block change
     */
    public boolean isRetainUnalteredOnBlockChange() {
        return retainUnalteredOnBlockChange;
    }

    /**
     * @return Whether this component makes a block valid for block lifecycle events
     */
    public boolean isBlockLifecycleEventsRequired() {
        return blockLifecycleEventsRequired;
    }

    public T getAnnotation(final Class<T> type) {
        return Iterables.getOnlyElement(Iterables.filter(annotations, type), null);
    }
}
