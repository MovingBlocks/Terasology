// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.ComponentFieldMetadata;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
