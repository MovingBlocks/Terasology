// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.MapMaker;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collection;
import java.util.Map;

/**
 *
 * Records changes made to all entities. It gets used by {@link ReadWriteStorageManager} to determine which changes
 * have been made since the last auto save. This save delta can then be applied to a copy the entities as they were at
 * the point of the last auto save. By doing so the auto save can access a snapshot of all entities on
 * off the main thread.
 *
 */
class EntitySetDeltaRecorder {
    private final ComponentLibrary componentLibrary;

    private TLongObjectMap<EntityDelta> entityDeltas = new TLongObjectHashMap<>();
    private TLongSet destroyedEntities = new TLongHashSet();
    private TLongSet deactivatedEntities = new TLongHashSet();
    /**
     * The used keys are unique, so that it is a collection of {@link DelayedEntityRef}s that cleans itself up
     * when the{@link DelayedEntityRef}s get no longer referenced
     * */
    private Map<Object, DelayedEntityRef> delayedEntityRefs =  new MapMaker().weakValues().makeMap();

    /**
     *
     * @param specialComponentLibrary must be a component library that uses a special copy strategy for entity refs.
     */
    EntitySetDeltaRecorder(ComponentLibrary specialComponentLibrary) {
        this.componentLibrary = specialComponentLibrary;
    }

    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> componentClass) {
        if (entity.isPersistent()) {
            onEntityComponentChange(entity, componentClass);
        }
    }

    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> componentClass) {
        if (entity.isPersistent()) {
            EntityDelta entityDelta = getOrCreateEntityDeltaFor(entity);
            Component component = entity.getComponent(componentClass);
            Component componentSnapshot = componentLibrary.copy(component);
            entityDelta.setChangedComponent(componentSnapshot);
        }
    }

    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        if (entity.isPersistent()) {
            EntityDelta entityDelta = getOrCreateEntityDeltaFor(entity);
            entityDelta.removeComponent(component);
        }
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

    public void onEntityDestroyed(EntityRef entity) {
        if (entity.isPersistent()) {
            entityDeltas.remove(entity.getId());
            destroyedEntities.add(entity.getId());
        }
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
        if (entity.isPersistent()) {
            EntityDelta entityDelta = getOrCreateEntityDeltaFor(entity);
            for (Component component : components) {
                Component componentSnapshot = componentLibrary.copy(component);
                entityDelta.setChangedComponent(componentSnapshot);
            }
        }
    }

    public void onBeforeDeactivation(EntityRef entity, Collection<Component> components) {
        if (entity.isPersistent()) {
            deactivatedEntities.add(entity.getId());
        }
    }

    public void registerDelayedEntityRef(DelayedEntityRef delayedEntity) {
        delayedEntityRefs.put(new Object(), delayedEntity);
    }

    public void bindAllDelayedEntityRefsTo(EntityManager entityManager) {
        for (DelayedEntityRef delayedEntityRef: delayedEntityRefs.values()) {
            delayedEntityRef.bindTo(entityManager);
        }
    }
}
