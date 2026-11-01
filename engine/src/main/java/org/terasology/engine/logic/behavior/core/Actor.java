// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.ComponentFieldUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;
import org.terasology.reflection.metadata.FieldMetadata;

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
        return entity.getComponent(type);
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
