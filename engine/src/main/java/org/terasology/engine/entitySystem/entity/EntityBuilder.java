// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Verify.verifyNotNull;

/**
 * An entity builder provides the ability to set up an entity before creating it. This prevents events being sent
 * for components being added or modified before it is fully set up.
 *
 */
public class EntityBuilder implements MutableComponentContainer {


    private static final Logger logger = LoggerFactory.getLogger(EntityBuilder.class);

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private EngineEntityPool pool;
    private EngineEntityManager entityManager;

    private boolean sendLifecycleEvents = true;
    private Optional<EntityScope> scope = Optional.empty();
    private Optional<Long> id = Optional.empty();

    public EntityBuilder(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        this.pool = entityManager.getCurrentWorldPool();
    }

    public EntityBuilder(EngineEntityManager entityManager, EngineEntityPool pool) {
        this.entityManager = entityManager;
        this.pool = pool;
    }

    /**
     * Adds all of the components from a prefab to this builder
     *
     * @param prefabName the name of the prefab to add
     * @return whether the prefab was successfully added
     */
    public boolean addPrefab(String prefabName) {
        if (prefabName != null && !prefabName.isEmpty()) {
            Prefab prefab = entityManager.getPrefabManager().getPrefab(prefabName);
            if (prefab == null) {
                logger.warn("Unable to instantiate unknown prefab: \"{}\"", prefabName);
                return false;
            }
            addPrefab(prefab);
            return true;
        } else {
            return false;
        }
    }
    /**
     * Adds all components from a prefab to this builder
     *
     * @param prefab the prefab to add
     */
    public void addPrefab(Prefab prefab) {
        if (prefab != null) {
            for (Component component : prefab.iterateComponents()) {
                Component componentCopy = entityManager.getComponentLibrary().copy(component);
                addComponent(verifyNotNull(componentCopy, "Component %s not registered (in prefab %s)", component, prefab));
            }
            addComponent(new EntityInfoComponent(prefab, prefab.isPersisted(), prefab.isAlwaysRelevant()));
        } else {
            addComponent(new EntityInfoComponent());
        }
    }

    /**
     * Produces an entity with the components contained in this entity builder
     *
     * @return The built entity.
     */
    public EntityRef build() {
        if (id.isPresent() && !entityManager.registerId(id.get())) {
            return EntityRef.NULL;
        }
        long finalId = id.orElse(entityManager.createEntity());

        components.values().forEach(c -> entityManager.getComponentStore().put(finalId, c));

        entityManager.assignToPool(finalId, pool);

        EntityRef entity = entityManager.getEntity(finalId);

        if (sendLifecycleEvents && entityManager.getEventSystem() != null) {
            //TODO: don't send OnAddedComponent when the entity is being re-loaded from storage
            entity.send(OnAddedComponent.newInstance());
            entity.send(OnActivatedComponent.newInstance());
        }

        //Retrieve the components again in case they were modified by the previous events
        for (Component component : entityManager.iterateComponents(entity.getId())) {
            entityManager.notifyComponentAdded(entity, component.getClass());
        }

        entity.setScope(scope.orElse(getEntityInfo().scope));

        return entity;
    }

    public EntityRef buildWithoutLifecycleEvents() {
        sendLifecycleEvents = false;
        return build();
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.keySet().contains(component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        return !Collections.disjoint(components.keySet(), filterComponents);
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        return components.keySet().containsAll(filterComponents);
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

    public void addComponents(Iterable<? extends Component> maybeComponents) {
        Optional.ofNullable(maybeComponents).ifPresent(comps -> {
            comps.forEach(this::addComponent);
        });
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

    public void setScope(EntityScope scope) {
        this.scope = Optional.of(scope);
    }

    public EntityScope getScope() {
        return scope.orElse(null);
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

    public boolean willSendLifecycleEvents() {
        return sendLifecycleEvents;
    }

    public void setSendLifecycleEvents(boolean sendLifecycleEvents) {
        this.sendLifecycleEvents = sendLifecycleEvents;
    }

    public void setId(long id) {
        this.id = Optional.of(id);
    }

}
