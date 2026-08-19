// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;

import java.util.List;

/**
 * An {@link EntityRef} that defers resolving its target entity until it is actually used.
 * <p>
 * Restoring a store of entities (e.g. a save file's global store, or a player store) deserializes one entity at a
 * time. A component on entity A may reference entity B by id - for example an inventory's item list - before B
 * itself has been deserialized and registered with the entity manager, simply because B comes later in the store.
 * Resolving that reference eagerly at deserialize time (a plain {@code entityManager.getEntity(id)} call) makes it
 * permanently resolve to "doesn't exist", even though B exists moments later once its own turn in the batch comes
 * up - there is no second resolution pass afterward. By the time gameplay code first touches a deserialized
 * {@link EntityRef}, the whole store has always already finished loading, so deferring resolution until then is
 * sufficient to make cross-references within the same store resolve correctly regardless of load order.
 * <p>
 * Resolution is memoized only once it succeeds, so a ref touched (unusually) before its target exists keeps
 * retrying on every subsequent call rather than permanently caching a dead lookup.
 */
public class LazyLoadEntityRef extends EntityRef {
    private final EngineEntityManager entityManager;
    private final long id;
    private EntityRef resolved;

    public LazyLoadEntityRef(EngineEntityManager entityManager, long id) {
        this.entityManager = entityManager;
        this.id = id;
    }

    private EntityRef resolve() {
        if (resolved == null || !resolved.exists()) {
            EntityRef candidate = entityManager.getEntity(id);
            if (candidate.exists()) {
                resolved = candidate;
            } else {
                return candidate;
            }
        }
        return resolved;
    }

    @Override
    public EntityRef copy() {
        EntityRef target = resolve();
        if (target.exists()) {
            return target.copy();
        }
        // Not resolvable yet - the copy has to stay lazy too, or it freezes into "doesn't exist"
        // permanently the moment something copies this ref before its target has finished loading.
        return new LazyLoadEntityRef(entityManager, id);
    }

    @Override
    public boolean exists() {
        return resolve().exists();
    }

    @Override
    public boolean isActive() {
        return resolve().isActive();
    }

    @Override
    public void destroy() {
        resolve().destroy();
    }

    @Override
    public <T extends Event> T send(T event) {
        return resolve().send(event);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isPersistent() {
        return resolve().isPersistent();
    }

    @Override
    public boolean isAlwaysRelevant() {
        return resolve().isAlwaysRelevant();
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        resolve().setAlwaysRelevant(alwaysRelevant);
    }

    @Override
    public EntityRef getOwner() {
        return resolve().getOwner();
    }

    @Override
    public void setOwner(EntityRef owner) {
        resolve().setOwner(owner);
    }

    @Override
    public void setScope(EntityScope scope) {
        resolve().setScope(scope);
    }

    @Override
    public void setSectorScope(long maxDelta) {
        resolve().setSectorScope(maxDelta);
    }

    @Override
    public void setSectorScope(long unloadedMaxDelta, long loadedMaxDelta) {
        resolve().setSectorScope(unloadedMaxDelta, loadedMaxDelta);
    }

    @Override
    public EntityScope getScope() {
        return resolve().getScope();
    }

    @Override
    public void invalidate() {
        resolve().invalidate();
    }

    @Override
    public Prefab getParentPrefab() {
        return resolve().getParentPrefab();
    }

    @Override
    public String toFullDescription() {
        return resolve().toFullDescription();
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return resolve().addComponent(component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        resolve().removeComponent(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        resolve().saveComponent(component);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return resolve().hasComponent(component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        return resolve().hasAnyComponents(filterComponents);
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        return resolve().hasAllComponents(filterComponents);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return resolve().getComponent(componentClass);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return resolve().iterateComponents();
    }
}
