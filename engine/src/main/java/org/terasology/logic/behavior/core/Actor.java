/*
 * Copyright 2015 MovingBlocks
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
import org.terasology.engine.ComponentFieldUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.logic.location.LocationComponent;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.logic.SkeletalMeshComponent;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Actor is the context of a tree evaluation. All state information is stored here.
 */
public class Actor {
    private final EntityRef minion;

    private float delta;
    private final Map<Integer, Object> dataMap = Maps.newHashMap();

    public Actor(EntityRef minion) {
        this.minion = minion;
    }

    @SuppressWarnings("unchecked")
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
