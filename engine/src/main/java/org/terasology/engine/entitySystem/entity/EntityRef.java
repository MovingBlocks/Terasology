// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity;

import com.google.common.base.Objects;
import org.terasology.engine.entitySystem.MutableComponentContainer;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.entity.internal.NullEntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.sectors.SectorSimulationComponent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * A wrapper around an entity id providing access to common functionality
 * *
 *
 */
public abstract class EntityRef implements MutableComponentContainer {

    public static final EntityRef NULL = NullEntityRef.getInstance();

    /**
     * Copies this entity, creating a new entity with identical components.
     * Also copies any owned entities, recursively.
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
     * @deprecated replaced by {{@link #setScope(EntityScope)}}
     */
    @Deprecated
    public abstract void setAlwaysRelevant(boolean alwaysRelevant);

    /**
     * @return The owning entity of this entity
     */
    public abstract EntityRef getOwner();

    /**
     * Sets the scope of the entity
     *
     * @param scope the new scope for the entity
     */
    public void setScope(EntityScope scope) {
    }

    /**
     * Sets the scope of this entity to sector-scope, and sets the {@link SectorSimulationComponent#unloadedMaxDelta}
     * and {@link SectorSimulationComponent#loadedMaxDelta} to the same given value.
     *
     * @param maxDelta the maximum delta for the sector-scope entity (loaded and unloaded)
     */
    public void setSectorScope(long maxDelta) {
    }

    /**
     * Sets the scope of this entity to sector-scope, and sets the {@link SectorSimulationComponent#unloadedMaxDelta}
     * and {@link SectorSimulationComponent#loadedMaxDelta} to the given values.
     *
     * @param unloadedMaxDelta the maximum unloaded delta for the sector-scope entity
     * @param loadedMaxDelta the maximum loaded delta for the sector-scope entity
     */
    public void setSectorScope(long unloadedMaxDelta, long loadedMaxDelta) {
    }

    /**
     * @return the scope of the entity
     */
    public EntityScope getScope() {
        return null;
    }

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

    /**
     * Invalidates this EntityRef
     */
    public void invalidate() {
    }
}
