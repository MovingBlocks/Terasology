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
package org.terasology.entitySystem.entity.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.sectors.SectorSimulationComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.EntitySerializer;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;

import static org.terasology.entitySystem.entity.internal.EntityScope.CHUNK;
import static org.terasology.entitySystem.entity.internal.EntityScope.GLOBAL;
import static org.terasology.entitySystem.entity.internal.EntityScope.SECTOR;

/**
 */
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
        if (exists()) {
            if (alwaysRelevant != isAlwaysRelevant()) {
                setScope(alwaysRelevant ? GLOBAL : CHUNK);
            }
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
