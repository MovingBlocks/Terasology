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
package org.terasology.entitySystem;

import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.common.NullEntityRef;
import org.terasology.entitySystem.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.persistence.EntitySerializer;
import org.terasology.game.CoreRegistry;

/**
 * A wrapper around an entity id providing access to common functionality
 *
 * @author Immortius <immortius@gmail.com>
 */
public abstract class EntityRef implements ComponentContainer {

    public static final NullEntityRef NULL = NullEntityRef.getInstance();

    /**
     * @return Does this entity exist - that is, is not deleted.
     */
    public abstract boolean exists();

    /**
     * Removes all components from this entity, effectively destroying it
     */
    public abstract void destroy();

    /**
     * Transmits an event to this entity
     *
     * @param event
     */
    public abstract void send(Event event);

    /**
     * @return The identifier of this entity. Should be avoided where possible and the EntityRef
     *         used instead to allow it to be invalidated if the entity is destroyed.
     */
    public abstract int getId();

    public abstract boolean isPersisted();

    public abstract void setPersisted(boolean persisted);

    public abstract Prefab getParentPrefab();

    public abstract AssetUri getPrefabURI();

    public String toFullDescription() {
        EntitySerializer serializer = new EntitySerializer((PersistableEntityManager) CoreRegistry.get(EntityManager.class));
        serializer.setUsingFieldIds(false);
        return EntityDataJSONFormat.write(serializer.serialize(this));
    }
}
