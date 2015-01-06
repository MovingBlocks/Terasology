/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.persistence.internal;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;

import java.util.Collection;

/**
 *
 * Records changes made to all entities. It gets used by {@link }StorageManagerInternal} to determine which changes
 * have been made since the last auto save. This save delta can then be applied to a copy the entities as they were at
 * the point of the last auto save. By doing so the auto save can access a snapshot of all entities on
 * off the main thread.
 *
 * @author Florian <florian@fkoeberle.de>
 */
class EntitySetDeltaRecorder {
    private final ComponentLibrary componentLibrary;

    private TLongObjectMap<EntityDelta> entityDeltas = new TLongObjectHashMap<>();
    private TLongSet destroyedEntities = new TLongHashSet();
    private TLongSet deactivatedEntities = new TLongHashSet();


    /**
     *
     * @param specialComponentLibrary must be a component library that uses a special copy strategy for entity refs.
     */
    public EntitySetDeltaRecorder(ComponentLibrary specialComponentLibrary) {
        this.componentLibrary = specialComponentLibrary;
    }

    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> componentClass) {
        onEntityComponentChange(entity, componentClass);
    }

    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> componentClass) {
        EntityDelta entityDelta = getOrCreateEntityDeltaFor(entity);
        Component component = entity.getComponent(componentClass);
        Component componentSnapshot = componentLibrary.copy(component);
        entityDelta.setChangedComponent(componentSnapshot);
    }

    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component){
        EntityDelta entityDelta = getOrCreateEntityDeltaFor(entity);
        entityDelta.removeComponent(component);
    }

    private EntityDelta getOrCreateEntityDeltaFor(EntityRef entity) {
        long id = entity.getId();
        EntityDelta entityDelta = entityDeltas.get(id);
        if (entityDelta == null) {
            entityDelta = new EntityDelta();
            entityDeltas.put(id, entityDelta);
        }
        return entityDelta;
    }

    public void onEntityDestroyed(long entityId) {
        entityDeltas.remove(entityId);
        destroyedEntities.add(entityId);
    }

    public TLongObjectMap<EntityDelta> getEntityDeltas() {
        return entityDeltas;
    }

    public TLongSet getDestroyedEntities() {
        return destroyedEntities;
    }

    public TLongSet getDeactivatedEntities() {
        return deactivatedEntities;
    }

    public void onReactivation(EntityRef entity, Collection<Component> components) {
        EntityDelta entityDelta = getOrCreateEntityDeltaFor(entity);
        for (Component component : components) {
            Component componentSnapshot = componentLibrary.copy(component);
            entityDelta.setChangedComponent(componentSnapshot);
        }
    }

    public void onBeforeDeactivation(EntityRef entity, Collection<Component> components) {
        deactivatedEntities.add(entity.getId());
    }
}
