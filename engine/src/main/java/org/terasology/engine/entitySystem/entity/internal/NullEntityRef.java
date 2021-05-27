// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;

import java.util.Collections;
import java.util.List;

/**
 * Null entity implementation - acts the same as an empty entity, except you cannot add anything to it.
 *
 */
public final class NullEntityRef extends EntityRef {
    private static NullEntityRef instance = new NullEntityRef();

    private NullEntityRef() {
    }

    public static NullEntityRef getInstance() {
        return instance;
    }

    @Override
    public EntityRef copy() {
        return this;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return false;
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        return false;
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        return false;
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return null;
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return null;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
    }

    @Override
    public void saveComponent(Component component) {
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return Collections.emptyList();
    }

    @Override
    public void destroy() {
    }

    @Override
    public <T extends Event> T send(T event) {
        return event;
    }

    @Override
    public long getId() {
        return PojoEntityManager.NULL_ID;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public boolean isAlwaysRelevant() {
        return false;
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
    }

    @Override
    public EntityRef getOwner() {
        return EntityRef.NULL;
    }

    @Override
    public void setOwner(EntityRef owner) {
    }

    @Override
    public Prefab getParentPrefab() {
        return null;
    }

    @Override
    public String toString() {
        return "EntityRef{" +
                "id=" + PojoEntityManager.NULL_ID +
                '}';
    }

    @Override
    public String toFullDescription() {
        return "{}";
    }
}
