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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;

/**
 * The class represents a future entity ref that has yet to be bound to an entity manager.
 * <p>
 * Currently it gets used by the StorageManager to create entity refs on the main thread for storage off the main
 * thread. During the storage the entities will be bound to the entity manager that is private the the saving thread.
 *
 */
public class DelayedEntityRef extends EntityRef {
    private final long id;
    private EntityRef entityRef;

    public DelayedEntityRef(long id) {
        this.id = id;
        this.entityRef = null;
    }

    private EntityRef getEntityRef() {
        if (entityRef == null) {
            throw new IllegalStateException("The entity ref must be bound to an entity manager before it can be used");
        }
        return entityRef;
    }

    public void bindTo(EntityManager entityManager) {
        if (entityRef != null) {
            throw new IllegalStateException("Entity was already bound to an entity manager");
        }
        entityRef = entityManager.getEntity(id);
    }

    @Override
    public EntityRef copy() {
        return getEntityRef().copy();
    }

    @Override
    public boolean exists() {
        return getEntityRef().exists();
    }

    @Override
    public boolean isActive() {
        return getEntityRef().isActive();
    }

    @Override
    public void destroy() {
        getEntityRef().destroy();
    }

    @Override
    public <T extends Event> T send(T event) {
        return getEntityRef().send(event);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isPersistent() {
        return getEntityRef().isPersistent();
    }

    @Override
    public boolean isAlwaysRelevant() {
        return getEntityRef().isAlwaysRelevant();
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        getEntityRef().setAlwaysRelevant(alwaysRelevant);
    }

    @Override
    public EntityRef getOwner() {
        return getEntityRef().getOwner();
    }

    @Override
    public void setOwner(EntityRef owner) {
        getEntityRef().setOwner(owner);
    }

    @Override
    public Prefab getParentPrefab() {
        return getEntityRef().getParentPrefab();
    }

    @Override
    public String toFullDescription() {
        return getEntityRef().toFullDescription();
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return getEntityRef().addComponent(component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        getEntityRef().removeComponent(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        getEntityRef().saveComponent(component);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return getEntityRef().hasComponent(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return getEntityRef().getComponent(componentClass);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return getEntityRef().iterateComponents();
    }
}
