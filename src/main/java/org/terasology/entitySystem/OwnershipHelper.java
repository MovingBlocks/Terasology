/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;

import java.util.Collection;
import java.util.Set;

/**
 * @author Immortius
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
     * @param entity
     * @return A collection of owned entities of the given entity
     */
    public Collection<EntityRef> listOwnedEntities(EntityRef entity) {
        Set<EntityRef> entityRefList = Sets.newHashSet();
        for (ComponentMetadata<?> componentMetadata : componentLibrary) {
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

    private void addOwnedEntitiesFor(Component comp, ComponentMetadata<?> componentMetadata, Collection<EntityRef> outEntityList) {
        for (FieldMetadata field : componentMetadata.iterateFields()) {
            if (field.isOwnedReference()) {
                Object value = field.getValue(comp);
                if (value instanceof Collection) {
                    for (EntityRef ref : ((Collection<EntityRef>) value)) {
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
            }
        }
    }

}
