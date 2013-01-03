/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.pojo;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.common.NullIterator;

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
    public boolean isPersisted() {
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
    public void setPersisted(boolean persisted) {
        if (exists()) {
            EntityInfoComponent info = getComponent(EntityInfoComponent.class);
            if (info == null) {
                info = new EntityInfoComponent();
                info.persisted = persisted;
                addComponent(info);
            } else if (info.persisted != persisted) {
                info.persisted = persisted;
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
        if (prefabUri != null) {
            return "EntityRef{id = " + id + ", prefab = '" + prefabUri.getSimpleString() + "'}";
        } else {
            return "EntityRef{" +
                    "id=" + id +
                    '}';
        }
    }

    void invalidate() {
        id = PojoEntityManager.NULL_ID;
    }
}
