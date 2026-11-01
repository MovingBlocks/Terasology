// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.LowLevelEntityManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.sectors.SectorSimulationComponent;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.persistence.serializers.EntityDataJSONFormat;
import org.terasology.engine.persistence.serializers.EntitySerializer;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;

import static org.terasology.engine.entitySystem.entity.internal.EntityScope.CHUNK;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.GLOBAL;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.SECTOR;

public abstract class BaseEntityRef extends EntityRef {

    private static final Logger logger = LoggerFactory.getLogger(BaseEntityRef.class);
    protected LowLevelEntityManager entityManager;

    public BaseEntityRef(LowLevelEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean isPersistent() {
        return exists() && (!isActive() || getEntityInfo().persisted);
    }

    @Override
    public boolean isAlwaysRelevant() {
        return isActive() && getScope().getAlwaysRelevant();
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        if (exists() && alwaysRelevant != isAlwaysRelevant()) {
            setScope(alwaysRelevant ? GLOBAL : CHUNK);
        }
    }

    @Override
    public EntityRef getOwner() {
        if (exists()) {
            return getEntityInfo().owner;
        }
        return EntityRef.NULL;
    }

    @Override
    public void setOwner(EntityRef owner) {
        if (exists()) {
            EntityInfoComponent info = getEntityInfo();
            if (!info.owner.equals(owner)) {
                info.owner = owner;
                saveComponent(info);
            }
        }
    }

    @Override
    public EntityScope getScope() {
        if (exists()) {
            return getEntityInfo().scope;
        }
        return null;
    }

    @Override
    public void setScope(EntityScope scope) {
        if (exists()) {
            EntityInfoComponent info = getEntityInfo();
            if (!info.scope.equals(scope)) {

                EngineEntityPool newPool;
                switch (scope) {
                    case GLOBAL:
                    case CHUNK:
                        newPool = entityManager.getGlobalPool();
                        removeComponent(SectorSimulationComponent.class);
                        break;
                    case SECTOR:
                        newPool = entityManager.getSectorManager();
                        if (!hasComponent(SectorSimulationComponent.class)) {
                            addComponent(new SectorSimulationComponent());
                        }
                        break;
                    default:
                        logger.error("Unrecognised scope {}.", scope);
                        return;
                }

                entityManager.moveToPool(getId(), newPool);
                info.scope = scope;
                saveComponent(info);
            }
        }
    }

    @Override
    public void setSectorScope(long maxDelta) {
        setSectorScope(maxDelta, maxDelta);
    }

    @Override
    public void setSectorScope(long unloadedMaxDelta, long loadedMaxDelta) {
        setScope(SECTOR);
        SectorSimulationComponent simulationComponent = getComponent(SectorSimulationComponent.class);
        simulationComponent.unloadedMaxDelta = unloadedMaxDelta;
        simulationComponent.loadedMaxDelta = loadedMaxDelta;
        saveComponent(simulationComponent);
    }

    @Override
    public Prefab getParentPrefab() {
        if (exists()) {
            EntityInfoComponent info = getComponent(EntityInfoComponent.class);
            if (info != null) {
                return info.parentPrefab;
            }
        }
        return null;
    }

    @Override
    public boolean isActive() {
        return exists() && entityManager.isActiveEntity(getId());
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        if (exists()) {
            return entityManager.getComponent(getId(), componentClass);
        }
        return null;
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        if (isActive()) {
            return entityManager.addComponent(getId(), component);
        }
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        if (isActive()) {
            entityManager.removeComponent(getId(), componentClass);
        }
    }

    @Override
    public void saveComponent(Component component) {
        if (isActive()) {
            entityManager.saveComponent(getId(), component);
        }
    }

    @Override
    public Iterable<Component> iterateComponents() {
        if (exists()) {
            return entityManager.iterateComponents(getId());
        }
        return Collections.emptyList();
    }

    @Override
    public void destroy() {
        if (isActive()) {
            entityManager.destroy(getId());
        }
    }

    @Override
    public <T extends Event> T send(T event) {
        if (exists()) {
            entityManager.getEventSystem().send(this, event);
        }
        return event;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return exists() && entityManager.hasComponent(getId(), component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        boolean hasComponents = false;
        for (Class<? extends Component> component : filterComponents) {
            hasComponents |= entityManager.hasComponent(getId(), component);
        }
        return exists() && hasComponents;
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        int numPosessedComponents = 0;
        for (Class<? extends Component> component : filterComponents) {
            numPosessedComponents += entityManager.hasComponent(getId(), component) ? 1 : 0;
        }
        return exists() && (numPosessedComponents == filterComponents.size());
    }

    @Override
    public String toString() {
        Prefab parent = getParentPrefab();
        StringBuilder builder = new StringBuilder();
        builder.append("EntityRef{id = ");
        builder.append(getId());
        NetworkComponent networkComponent = getComponent(NetworkComponent.class);
        if (networkComponent != null) {
            builder.append(", netId = ");
            builder.append(networkComponent.getNetworkId());
        }
        if (parent != null) {
            builder.append(", prefab = '");
            builder.append(parent.getUrn());
            builder.append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public void invalidate() {
        entityManager = null;
    }

    private EntityInfoComponent getEntityInfo() {
        EntityInfoComponent entityInfo = getComponent(EntityInfoComponent.class);
        if (entityInfo == null) {
            entityInfo = addComponent(new EntityInfoComponent());
        }
        return entityInfo;
    }

    @Override
    public String toFullDescription() {
        EntitySerializer serializer = new EntitySerializer((EngineEntityManager) entityManager);
        serializer.setUsingFieldIds(false);
        return AccessController.doPrivileged((PrivilegedAction<String>) () ->
               EntityDataJSONFormat.write(serializer.serialize(this)));
    }
}
