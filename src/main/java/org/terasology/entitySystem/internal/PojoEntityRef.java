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

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.network.NetworkComponent;

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
        if (exists()) {
            EntityInfoComponent info = getComponent(EntityInfoComponent.class);
            if (info == null) {
                info = new EntityInfoComponent();
                addComponent(info);
            }
            return info.persisted;
        }
        return false;
    }

    @Override
    public void setPersistent(boolean persistent) {
        if (exists()) {
            EntityInfoComponent info = getComponent(EntityInfoComponent.class);
            if (info == null) {
                info = new EntityInfoComponent();
                info.persisted = persistent;
                addComponent(info);
            } else if (info.persisted != persistent) {
                info.persisted = persistent;
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
        if (exists()) {
            return entityManager.isEntityLoaded(id);
        }
        return false;
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
        if (exists()) {
            return entityManager.addComponent(id, component);
        }
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        if (exists()) {
            entityManager.removeComponent(id, componentClass);
        }
    }

    @Override
    public void saveComponent(Component component) {
        if (exists()) {
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
        if (exists()) {
            entityManager.destroy(id);
        }
    }

    @Override
    public void send(Event event) {
        if (exists()) {
            entityManager.getEventSystem().send(this, event);
        }
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return exists() && entityManager.hasComponent(id, component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof EntityRef) {
            EntityRef other = (EntityRef) o;
            if (!exists() && !(other.exists())) {
                return true;
            }
            return getId() == other.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return !exists() ? 0 : (int) (id ^ (id >>> 32));
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
            builder.append(prefabUri.getSimpleString());
            builder.append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    void invalidate() {
        id = PojoEntityManager.NULL_ID;
    }
}
