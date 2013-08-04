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
package org.terasology.entitySystem.internal;

import com.google.common.base.Objects;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.network.NetworkComponent;
import org.terasology.utilities.collection.NullIterator;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityRef extends EntityRef {
    int id;
    PojoEntityManager entityManager;

    PojoEntityRef(PojoEntityManager manager, int id) {
        this.id = id;
        this.entityManager = manager;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isPersistent() {
        return exists() && (!isActive() || getEntityInfo().persisted);
    }

    @Override
    public void setPersistent(boolean persistent) {
        if (exists()) {
            EntityInfoComponent info = getEntityInfo();
            if (info.persisted != persistent) {
                info.persisted = persistent;
                saveComponent(info);
            }
        }
    }

    @Override
    public boolean isAlwaysRelevant() {
        return isActive() && getEntityInfo().alwaysRelevant;
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        if (exists()) {
            EntityInfoComponent info = getEntityInfo();
            if (info.alwaysRelevant != alwaysRelevant) {
                info.alwaysRelevant = alwaysRelevant;
                saveComponent(info);
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
    public Prefab getParentPrefab() {
        if (exists()) {
            EntityInfoComponent info = getComponent(EntityInfoComponent.class);
            if (info != null) {
                return entityManager.getPrefabManager().getPrefab(info.parentPrefab);
            }
        }
        return null;
    }

    @Override
    public AssetUri getPrefabURI() {
        if (exists()) {
            EntityInfoComponent info = getComponent(EntityInfoComponent.class);
            if (info != null && !info.parentPrefab.isEmpty()) {
                return new AssetUri(AssetType.PREFAB, info.parentPrefab);
            }
        }
        return null;
    }

    @Override
    public boolean exists() {
        return id != PojoEntityManager.NULL_ID;
    }

    @Override
    public boolean isActive() {
        return exists() && entityManager.isEntityActive(id);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        if (exists()) {
            return entityManager.getComponent(id, componentClass);
        }
        return null;
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        if (isActive()) {
            return entityManager.addComponent(id, component);
        }
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        if (isActive()) {
            entityManager.removeComponent(id, componentClass);
        }
    }

    @Override
    public void saveComponent(Component component) {
        if (isActive()) {
            entityManager.saveComponent(id, component);
        }
    }

    @Override
    public Iterable<Component> iterateComponents() {
        if (exists()) {
            return entityManager.iterateComponents(id);
        }
        return NullIterator.newInstance();
    }

    @Override
    public void destroy() {
        if (isActive()) {
            entityManager.destroy(id);
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
        return exists() && entityManager.hasComponent(id, component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EntityRef) {
            EntityRef other = (EntityRef) o;
            return !exists() && !(other.exists()) || getId() == other.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return !exists() ? 0 : Objects.hashCode(id);
    }

    @Override
    public String toString() {
        AssetUri prefabUri = getPrefabURI();
        StringBuilder builder = new StringBuilder();
        builder.append("EntityRef{id = ");
        builder.append(id);
        NetworkComponent networkComponent = getComponent(NetworkComponent.class);
        if (networkComponent != null) {
            builder.append(", netId = ");
            builder.append(networkComponent.getNetworkId());
        }
        if (prefabUri != null) {
            builder.append(", prefab = '");
            builder.append(prefabUri.toSimpleString());
            builder.append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    void invalidate() {
        id = PojoEntityManager.NULL_ID;
        entityManager = null;
    }

    private EntityInfoComponent getEntityInfo() {
        EntityInfoComponent entityInfo = getComponent(EntityInfoComponent.class);
        if (entityInfo == null) {
            entityInfo = addComponent(new EntityInfoComponent());
        }
        return entityInfo;
    }
}
