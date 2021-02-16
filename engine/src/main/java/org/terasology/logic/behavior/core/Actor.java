/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior.core;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.ComponentFieldUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.module.sandbox.API;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.CoreRegistry;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * The actor is a decorated entity, which can act on a behavior tree using an Interpreter.
 * <br><br>
 * Besides the actual entity, a blackboard is stored for each actor. Every node may read or write to this blackboard,
 * to communicate their states or exchange variables with other nodes.
 */
@API
public class Actor {
    private static Logger logger = LoggerFactory.getLogger(Actor.class);
    // Stores system-wide information (allows inter-node communication)
    public final Map<String, Object> blackboard;
    private final EntityRef entity;

    // Stores information uniquely for each node that requires it
    // TODO can we use a faster data structure? this gets accessed a lot
    private final Map<Integer, Object> dataMap = Maps.newHashMap();

    private float delta;

    public Actor(EntityRef entity) {
        this.entity = entity;
        blackboard = Maps.newHashMap();
    }

    public <T> T readFromBlackboard(String reference) {
        return (T) blackboard.getOrDefault(reference, null);

    }

    public void writeToBlackboard(String reference, Object value) {
        blackboard.put(reference, value);
    }

    public <T> T getValue(int id) {
        return (T) dataMap.get(id);
    }

    public void setValue(int id, Object obj) {
        dataMap.put(id, obj);
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    /**
     * @param type The type of the component
     * @return The component of the actors minion or null if the minion has no such component.
     */
    public <T extends Component> T getComponent(Class<T> type) {
        T component = entity.getComponent(type);
        return component;
    }

    public Object getComponentField(ComponentFieldUri uri) {
        ComponentLibrary componentLibrary = CoreRegistry.get(EntitySystemLibrary.class).getComponentLibrary();
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(new ResourceUrn(uri.getComponentUri().toString()));
        if (metadata == null) {
            return null;
        }
        Component component = entity.getComponent(metadata.getType());
        if (component == null) {
            return null;
        }
        FieldMetadata<?, ?> fieldMetadata = metadata.getField(uri.getFieldName());
        if (fieldMetadata == null) {
            return null;
        }
        Field field = fieldMetadata.getField();
        try {
            return field.get(component);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param component The class of the component
     * @return true if the entity has the a component of the given class
     */
    public boolean hasComponent(Class<? extends Component> component) {
        return entity.hasComponent(component);
    }

    public void save(Component component) {
        entity.saveComponent(component);
    }

    public EntityRef getEntity() {
        return entity;
    }

}
