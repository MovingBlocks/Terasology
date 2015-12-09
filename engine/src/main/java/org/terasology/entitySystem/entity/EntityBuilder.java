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
package org.terasology.entitySystem.entity;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.MutableComponentContainer;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityInfoComponent;

import java.util.Map;

/**
 * An entity builder provides the ability to set up an entity before creating it. This prevents events being sent
 * for components being added or modified before it is fully set up.
 *
 */
public class EntityBuilder implements MutableComponentContainer {

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private EngineEntityManager manager;

    public EntityBuilder(EngineEntityManager manager) {
        this.manager = manager;
    }

    /**
     * Produces an entity with the components contained in this entity builder
     *
     * @return The built entity.
     */
    public EntityRef build() {
        return manager.create(components.values());
    }

    public EntityRef buildWithoutLifecycleEvents() {
        return manager.createEntityWithoutLifecycleEvents(components.values());
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.keySet().contains(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        components.put(component.getClass(), component);
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        components.remove(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        components.put(component.getClass(), component);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return components.values();
    }

    public boolean isPersistent() {
        return getEntityInfo().persisted;
    }

    public void setPersistent(boolean persistent) {
        getEntityInfo().persisted = persistent;
    }

    public boolean isAlwaysRelevant() {
        return getEntityInfo().alwaysRelevant;
    }

    public void setAlwaysRelevant(boolean alwaysRelevant) {
        getEntityInfo().alwaysRelevant = alwaysRelevant;
    }

    public void setOwner(EntityRef owner) {
        getEntityInfo().owner = owner;
    }

    public EntityRef getOwner() {
        return getEntityInfo().owner;
    }

    private EntityInfoComponent getEntityInfo() {
        EntityInfoComponent entityInfo = getComponent(EntityInfoComponent.class);
        if (entityInfo == null) {
            entityInfo = addComponent(new EntityInfoComponent());
        }
        return entityInfo;
    }

}
