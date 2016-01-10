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
package org.terasology.entitySystem.entity.internal;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentFieldMetadata;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 */
public final class OwnershipHelper {
    private ComponentLibrary componentLibrary;

    public OwnershipHelper(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    /**
     * Produces a collection of entities that are owned by the provided entity.
     * This is immediate ownership only - it does not recursively follow ownership.
     *
     * @param entity The owning entity
     * @return A collection of owned entities of the given entity
     */
    public Collection<EntityRef> listOwnedEntities(EntityRef entity) {
        Set<EntityRef> entityRefList = Sets.newHashSet();
        for (ComponentMetadata<?> componentMetadata : componentLibrary.iterateComponentMetadata()) {
            if (componentMetadata.isReferenceOwner()) {
                Component comp = entity.getComponent(componentMetadata.getType());
                if (comp != null) {
                    addOwnedEntitiesFor(comp, componentMetadata, entityRefList);
                }
            }
        }
        return entityRefList;
    }

    public Collection<EntityRef> listOwnedEntities(Component component) {
        Set<EntityRef> entityRefList = Sets.newHashSet();
        addOwnedEntitiesFor(component, componentLibrary.getMetadata(component.getClass()), entityRefList);
        return entityRefList;
    }

    @SuppressWarnings("unchecked")
    private void addOwnedEntitiesFor(Component comp, ComponentMetadata<?> componentMetadata, Collection<EntityRef> outEntityList) {
        componentMetadata.getFields().stream().filter(ComponentFieldMetadata::isOwnedReference).forEach(field -> {
            Object value = field.getValue(comp);
            if (value instanceof Collection) {
                for (EntityRef ref : ((Collection<EntityRef>) value)) {
                    if (ref.exists()) {
                        outEntityList.add(ref);
                    }
                }
            } else if (value instanceof Map) {
                for (EntityRef ref : ((Map<Object, EntityRef>) value).values()) {
                    if (ref.exists()) {
                        outEntityList.add(ref);
                    }
                }
            } else if (value instanceof EntityRef) {
                EntityRef ref = (EntityRef) value;
                if (ref.exists()) {
                    outEntityList.add(ref);
                }
            }
        });
    }

}
