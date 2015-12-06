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

import com.google.common.base.Objects;
import org.terasology.entitySystem.MutableComponentContainer;
import org.terasology.entitySystem.entity.internal.NullEntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;

/**
 * A wrapper around an entity id providing access to common functionality
 * *
 *
 */
public abstract class EntityRef implements MutableComponentContainer {

    public static final EntityRef NULL = NullEntityRef.getInstance();

    /**
     * Copies this entity, creating a new entity with identical components.
     * Note: You will need to be careful when copying entities, particularly around ownership - this method does nothing to prevent you ending up
     * with multiple entities owning the same entities.
     *
     * @return A copy of this entity.
     */
    public abstract EntityRef copy();

    /**
     * @return Does this entity exist - that is, is not deleted.
     */
    public abstract boolean exists();

    /**
     * @return Whether this entity is currently loaded (not stored)
     */
    public abstract boolean isActive();

    /**
     * Removes all components and destroys it
     */
    public abstract void destroy();

    /**
     * Transmits an event to this entity
     *
     * @param event
     */
    public abstract <T extends Event> T send(T event);

    /**
     * @return The identifier of this entity. Should be avoided where possible and the EntityRef
     * used instead to allow it to be invalidated if the entity is destroyed.
     */
    public abstract long getId();

    /**
     * @return Whether this entity should be saved
     */
    public abstract boolean isPersistent();

    /**
     * @return Whether this entity should remain active even when the part of the world/owner of the entity is not
     * relevant
     */
    public abstract boolean isAlwaysRelevant();

    /**
     * Sets whether the entity should remain active even when its owner or the part of the world it resides in is
     * not relevant
     *
     * @param alwaysRelevant
     */
    public abstract void setAlwaysRelevant(boolean alwaysRelevant);

    /**
     * @return The owning entity of this entity
     */
    public abstract EntityRef getOwner();

    /**
     * Sets the entity that owns this entity.
     *
     * @param owner
     */
    public abstract void setOwner(EntityRef owner);

    /**
     * @return The prefab this entity is based off of
     */
    public abstract Prefab getParentPrefab();

    /**
     * @return A full, json style description of the entity.
     */
    public abstract String toFullDescription();

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof EntityRef) {
            EntityRef other = (EntityRef) o;
            return !exists() && !other.exists() || getId() == other.getId();
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(getId());
    }
}
