/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import com.google.common.collect.Maps;
import org.terasology.engine.API;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.logic.SkeletalMeshComponent;

import java.util.Map;

/**
 * The actor is a decorated entity, which can act on a behavior tree using an Interpreter.
 * <p/>
 * Besides the actual entity, a blackboard is stored for each actor. Every node may read or write to this blackboard,
 * to communicate their states or exchange variables with other nodes.
 *
 * @author synopia
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
            ComponentMetadata<T> metadata = CoreRegistry.get(ComponentLibrary.class).getMetadata(type);
            component = metadata.newInstance();
            minion.addComponent(component);
        }
        return component;
    }

    public SkeletalMeshComponent skeletalMesh() {
        return minion.getComponent(SkeletalMeshComponent.class);
    }

    public LocationComponent location() {
        return minion.getComponent(LocationComponent.class);
    }

    public void save(Component component) {
        minion.saveComponent(component);
    }

    public EntityRef minion() {
        return minion;
    }
}
