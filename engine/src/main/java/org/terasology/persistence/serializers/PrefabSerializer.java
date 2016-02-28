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

package org.terasology.persistence.serializers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.module.Module;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.protobuf.EntityData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides the ability to serialize and deserialize prefabs to the EntityData.Prefab proto buffer format.
 * <br><br>
 * As with the component serializer, a component id mapping can be provided to have components serialized against
 * ids rather than name strings.
 * <br><br>
 * It is also possible to set whether entity ids will be handled or ignored - if ignored then deserialized entities will
 * be given new ids.
 *
 */
public class PrefabSerializer {
    private static final Logger logger = LoggerFactory.getLogger(PrefabSerializer.class);

    private ComponentSerializer componentSerializer;
    private ComponentLibrary componentLibrary;

    public PrefabSerializer(ComponentLibrary componentLibrary, TypeSerializationLibrary typeSerializationLibrary) {
        this.componentLibrary = componentLibrary;
        this.componentSerializer = new ComponentSerializer(componentLibrary, typeSerializationLibrary);
    }

    /**
     * Sets the mapping between component classes and the ids that are used for serialization
     *
     * @param table
     */
    public void setComponentIdMapping(Map<Class<? extends Component>, Integer> table) {
        componentSerializer.setIdMapping(table);
    }

    /**
     * Clears the mapping between component classes and ids. This causes components to be serialized with their component
     * class name instead.
     */
    public void removeComponentIdMapping() {
        componentSerializer.removeIdMapping();
    }

    /**
     * @param prefab
     * @return The serialized prefab
     */
    public EntityData.Prefab serialize(Prefab prefab) {
        EntityData.Prefab.Builder prefabData = EntityData.Prefab.newBuilder();
        prefabData.setName(prefab.getName());
        if (prefab.getParent() != null) {
            prefabData.setParentName(prefab.getParent().getName());
        }
        prefabData.setAlwaysRelevant(prefab.isAlwaysRelevant());
        prefabData.setPersisted(prefab.isPersisted());

        // Delta off the parent
        for (Component component : prefab.iterateComponents()) {
            if (prefab.getParent() != null && prefab.getParent().hasComponent(component.getClass())) {
                EntityData.Component serializedComponent = componentSerializer.serialize(prefab.getParent().getComponent(component.getClass()), component);
                if (serializedComponent != null) {
                    prefabData.addComponent(serializedComponent);
                }
            } else {
                prefabData.addComponent(componentSerializer.serialize(component));
            }
        }
        if (prefab.getParent() != null) {
            for (Component parentComp : prefab.getParent().iterateComponents()) {
                if (!prefab.hasComponent(parentComp.getClass())) {
                    prefabData.addRemovedComponent(componentLibrary.getMetadata(parentComp).getUri().toString());
                }
            }
        }
        return prefabData.build();
    }

    /**
     * Deserializes a prefab
     *
     * @param prefabData
     * @return The deserialized prefab
     */
    public PrefabData deserialize(EntityData.Prefab prefabData) {
        return deserialize(prefabData, Collections.<EntityData.Prefab>emptyList());
    }

    /**
     * Deserializes a prefab
     *
     * @param prefabData
     * @param deltas
     * @return The deserialized prefab
     */
    public PrefabData deserialize(EntityData.Prefab prefabData, List<EntityData.Prefab> deltas) {
        Module context = ModuleContext.getContext();
        PrefabData result = new PrefabData();
        deserializeCommonData(prefabData, result);
        for (EntityData.Prefab delta : deltas) {
            applyCommonDataDelta(delta, result);
        }

        addInheritedComponents(result);

        applyComponentChanges(context, prefabData, result);
        for (EntityData.Prefab delta : deltas) {
            applyComponentChanges(context, delta, result);
        }

        return result;
    }

    public void deserializeDeltaOnto(EntityData.Prefab delta, PrefabData result) {
        Module context = ModuleContext.getContext();
        applyCommonDataDelta(delta, result);
        applyComponentChanges(context, delta, result);
    }

    private void applyComponentChanges(Module context, EntityData.Prefab prefabData, PrefabData result) {
        for (String removedComponent : prefabData.getRemovedComponentList()) {
            ComponentMetadata<?> metadata = componentLibrary.resolve(removedComponent, context);
            if (metadata != null) {
                result.removeComponent(metadata.getType());
            }
        }
        for (EntityData.Component componentData : prefabData.getComponentList()) {
            ComponentMetadata<?> metadata = componentLibrary.resolve(componentData.getType(), context);
            if (metadata != null) {
                Component existing = result.getComponent(metadata.getType());
                if (existing != null) {
                    componentSerializer.deserializeOnto(existing, componentData, context);
                } else {
                    Component newComponent = componentSerializer.deserialize(componentData, context);
                    if (newComponent != null) {
                        result.addComponent(newComponent);
                    }
                }
            } else if (componentData.hasType()) {
                logger.error("Prefab contains unknown component '{}'", componentData.getType());
            }
        }
    }

    private void addInheritedComponents(PrefabData result) {
        if (result.getParent() != null) {
            for (Component comp : result.getParent().iterateComponents()) {
                result.addComponent(componentLibrary.copy(comp));
            }
        }
    }

    private void applyCommonDataDelta(EntityData.Prefab delta, PrefabData result) {
        if (delta.hasPersisted()) {
            result.setPersisted(delta.getPersisted());
        }
        if (delta.hasAlwaysRelevant()) {
            result.setAlwaysRelevant(delta.getAlwaysRelevant());
        }
        if (delta.hasParentName()) {
            Optional<? extends Prefab> parent = Assets.get(delta.getParentName(), Prefab.class);
            result.setParent(parent.orElse(null));
        }
    }

    private void deserializeCommonData(EntityData.Prefab prefabData, PrefabData result) {
        result.setPersisted((prefabData.hasPersisted()) ? prefabData.getPersisted() : true);
        result.setAlwaysRelevant(prefabData.hasAlwaysRelevant() ? prefabData.getAlwaysRelevant() : false);
        if (prefabData.hasParentName()) {
            Prefab parent = Assets.get(prefabData.getParentName(), Prefab.class).orElse(null);
            result.setParent(parent);
        }
    }


}
