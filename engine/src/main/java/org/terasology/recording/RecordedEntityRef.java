/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.recording;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.entity.internal.NullEntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;

import java.util.List;

/**
 * Class used instead of other EntityRef types during RecordedEvent deserialization. This class is necessary because
 * during RecordedEvent deserialization, it is not possible to correctly deserialize EntityRef fields because they do
 * not exist yet, so RecordedEntityRef is responsible for saving its id and then try to get the real EntityRef when it
 * is necessary.
 */
public class RecordedEntityRef extends EntityRef {

    /** The EntityRef's id on the recording */
    private long id;
    /** The EntityManager that stores and creates the game's entities */
    private LowLevelEntityManager manager;
    /** The real EntityRef that was recorded with an event during Recording.
     *  This variable is loaded with its real value once a method is called */
    private EntityRef realEntityRef;

    RecordedEntityRef(long id, LowLevelEntityManager manager) {
        this.id = id;
        this.manager = manager;
        this.realEntityRef = EntityRef.NULL;
    }

    @Override
    public EntityRef copy() {
        updateRealEntityRef();
        return this.realEntityRef.copy();
    }

    @Override
    public boolean exists() {
        updateRealEntityRef();
        return this.realEntityRef.exists();
    }

    @Override
    public boolean isActive() {
        updateRealEntityRef();
        return this.realEntityRef.isActive();
    }

    @Override
    public void destroy() {
        updateRealEntityRef();
        this.realEntityRef.destroy();
    }

    @Override
    public <T extends Event> T send(T event) {
        updateRealEntityRef();
        return this.realEntityRef.send(event);
    }

    @Override
    public long getId() {
        updateRealEntityRef();
        return this.realEntityRef.getId();
    }

    @Override
    public boolean isPersistent() {
        updateRealEntityRef();
        return this.realEntityRef.isPersistent();
    }

    @Override
    public boolean isAlwaysRelevant() {
        updateRealEntityRef();
        return this.realEntityRef.isAlwaysRelevant();
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        updateRealEntityRef();
        this.realEntityRef.setAlwaysRelevant(alwaysRelevant);
    }

    @Override
    public EntityRef getOwner() {
        updateRealEntityRef();
        return this.realEntityRef.getOwner();
    }

    @Override
    public void setOwner(EntityRef owner) {
        updateRealEntityRef();
        this.realEntityRef.setOwner(owner);
    }

    @Override
    public Prefab getParentPrefab() {
        updateRealEntityRef();
        return this.realEntityRef.getParentPrefab();
    }

    @Override
    public String toFullDescription() {
        updateRealEntityRef();
        return this.realEntityRef.toFullDescription();
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        updateRealEntityRef();
        return this.realEntityRef.addComponent(component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        updateRealEntityRef();
        this.realEntityRef.removeComponent(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        updateRealEntityRef();
        this.realEntityRef.saveComponent(component);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        updateRealEntityRef();
        return this.realEntityRef.hasComponent(component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        updateRealEntityRef();
        return this.realEntityRef.hasAnyComponents(filterComponents);
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        updateRealEntityRef();
        return this.realEntityRef.hasAllComponents(filterComponents);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        updateRealEntityRef();
        return this.realEntityRef.getComponent(componentClass);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        updateRealEntityRef();
        return this.realEntityRef.iterateComponents();
    }

    private void updateRealEntityRef() {
        if (this.realEntityRef instanceof NullEntityRef) {
            this.realEntityRef = this.manager.getEntity(this.id);
        }
    }
}
