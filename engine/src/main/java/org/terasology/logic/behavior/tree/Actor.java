/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.behavior.tree;

import com.google.common.collect.Maps;
import org.terasology.engine.ComponentFieldUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.logic.location.LocationComponent;
import org.terasology.module.sandbox.API;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.logic.SkeletalMeshComponent;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * The actor is a decorated entity, which can act on a behavior tree using an Interpreter.
 * <br><br>
 * Besides the actual entity, a blackboard is stored for each actor. Every node may read or write to this blackboard,
 * to communicate their states or exchange variables with other nodes.
 *
 */
@API
public class Actor {
    private final EntityRef minion;
    private final Map<String, Object> blackboard;

    public Actor(EntityRef minion) {
        this.minion = minion;
        blackboard = Maps.newHashMap();
    }

    public <T> T write(String key, T value) {
        return (T) blackboard.put(key, value);
    }

    public <T> T read(String key) {
        return read(key, null);
    }

    public <T> T read(String key, T defaultValue) {
        Object value = blackboard.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    public <T extends Component> T component(Class<T> type) {
        T component = minion.getComponent(type);
        if (component == null) {
            ComponentMetadata<T> metadata = CoreRegistry.get(EntitySystemLibrary.class).getComponentLibrary().getMetadata(type);
            if (metadata == null || !metadata.isConstructable()) {
                throw new RuntimeException("Cannot create component for " + type);
            }
            component = metadata.newInstance();
            minion.addComponent(component);
        }
        return component;
    }

    public Object getComponentField(ComponentFieldUri uri) {
        ComponentLibrary componentLibrary = CoreRegistry.get(EntitySystemLibrary.class).getComponentLibrary();
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(uri.getComponentUri());
        if (metadata == null) {
            return null;
        }
        Component component = minion.getComponent(metadata.getType());
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

    public SkeletalMeshComponent skeletalMesh() {
        return minion.getComponent(SkeletalMeshComponent.class);
    }

    public boolean hasMesh() {
        return minion.hasComponent(SkeletalMeshComponent.class);
    }

    public LocationComponent location() {
        return minion.getComponent(LocationComponent.class);
    }

    public boolean hasLocation() {
        return minion.hasComponent(LocationComponent.class);
    }

    public void save(Component component) {
        minion.saveComponent(component);
    }

    public EntityRef minion() {
        return minion;
    }
}
