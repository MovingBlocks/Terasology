/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * ComponentSerializer provides the ability to serialize and deserialize between Components and the protobuf
 * EntityData.Component
 * <p/>
 * If provided with a idTable, then the components will be serialized and deserialized using those ids rather
 * than the names of each component, saving some space.
 * <p/>
 * When serializing, a FieldSerializeCheck can be provided to determine whether each field should be serialized or not
 *
 * @author Immortius
 */
public class ComponentSerializer {

    private static final Logger logger = LoggerFactory.getLogger(ComponentSerializer.class);

    private ComponentLibrary componentLibrary;
    private BiMap<Class<? extends Component>, Integer> idTable = ImmutableBiMap.<Class<? extends Component>, Integer>builder().build();
    private boolean usingFieldIds = false;

    /**
     * Creates the component serializer.
     *
     * @param componentLibrary The component library used to provide information on each component and its fields.
     */
    public ComponentSerializer(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    public void setUsingFieldIds(boolean usingFieldIds) {
        this.usingFieldIds = usingFieldIds;
    }

    public boolean isUsingFieldIds() {
        return usingFieldIds;
    }

    /**
     * Sets the mapping between component classes and the ids that are used for serialization
     *
     * @param table
     */
    public void setIdMapping(Map<Class<? extends Component>, Integer> table) {
        idTable = ImmutableBiMap.copyOf(table);
    }

    /**
     * Clears the mapping between component classes and ids. This causes components to be serialized with their component
     * class name instead.
     */
    public void removeIdMapping() {
        idTable = ImmutableBiMap.<Class<? extends Component>, Integer>builder().build();
    }

    /**
     * @param componentData
     * @return The component described by the componentData, or null if it couldn't be deserialized
     */
    public Component deserialize(EntityData.Component componentData) {
        ComponentMetadata<? extends Component> componentMetadata = getComponentMetadata(componentData);
        if (componentMetadata != null) {
            Component component = componentMetadata.newInstance();
            return deserializeOnto(component, componentData, componentMetadata, FieldSerializeCheck.NullCheck.<Component>newInstance());
        } else {
            logger.warn("Unable to deserialize unknown component type: {}", componentData.getType());
        }
        return null;
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @param target
     * @param componentData
     * @return The target component.
     */
    public Component deserializeOnto(Component target, EntityData.Component componentData) {
        return deserializeOnto(target, componentData, FieldSerializeCheck.NullCheck.<Component>newInstance());
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @param target
     * @param componentData
     * @param fieldCheck
     * @return The target component.
     */
    public Component deserializeOnto(Component target, EntityData.Component componentData, FieldSerializeCheck<Component> fieldCheck) {
        ComponentMetadata<? extends Component> componentMetadata = getComponentMetadata(componentData);
        if (componentMetadata != null) {
            return deserializeOnto(target, componentData, componentMetadata, fieldCheck);
        } else {
            logger.warn("Unable to deserialize unknown component type: {}", componentData.getType());
        }
        return target;
    }


    private Component deserializeOnto(Component targetComponent, EntityData.Component componentData, ClassMetadata componentMetadata, FieldSerializeCheck<Component> fieldCheck) {
        for (EntityData.NameValue field : componentData.getFieldList()) {
            FieldMetadata fieldInfo = null;
            if (field.hasNameIndex()) {
                fieldInfo = componentMetadata.getFieldById(field.getNameIndex());
            } else if (field.hasName()) {
                fieldInfo = componentMetadata.getField(field.getName());
            }
            if (fieldInfo == null || !fieldCheck.shouldDeserializeField(fieldInfo)) {
                continue;
            }

            fieldInfo.deserializeOnto(targetComponent, field.getValue());
        }
        return targetComponent;
    }


    /**
     * Serializes a component.
     *
     * @param component
     * @return The serialized component, or null if it could not be serialized.
     */
    public EntityData.Component serialize(Component component) {
        return serialize(component, FieldSerializeCheck.NullCheck.<Component>newInstance());
    }

    /**
     * Serializes a component.
     *
     * @param component
     * @param check     A check to use to see if each field should be serialized.
     * @return The serialized component, or null if it could not be serialized.
     */
    public EntityData.Component serialize(Component component, FieldSerializeCheck<Component> check) {
        ClassMetadata<?> componentMetadata = componentLibrary.getMetadata(component.getClass());
        if (componentMetadata == null) {
            logger.error("Unregistered component type: {}", component.getClass());
            return null;
        }
        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        serializeComponentType(component, componentMessage);

        for (FieldMetadata field : componentMetadata.iterateFields()) {
            if (check.shouldSerializeField(field, component)) {
                EntityData.NameValue fieldData = field.serializeNameValue(component, usingFieldIds);
                if (fieldData != null) {
                    componentMessage.addField(fieldData);
                }
            }
        }

        return componentMessage.build();
    }

    private void serializeComponentType(Component component, EntityData.Component.Builder componentMessage) {
        Integer compId = idTable.get(component.getClass());
        if (compId != null) {
            componentMessage.setTypeIndex(compId);
        } else {
            componentMessage.setType(MetadataUtil.getComponentClassName(component));
        }
    }

    /**
     * Serializes the differences between two components.
     *
     * @param base  The base component to compare against.
     * @param delta The component whose differences will be serialized
     * @return The serialized component, or null if it could not be serialized
     */
    public EntityData.Component serialize(Component base, Component delta) {
        return serialize(base, delta, FieldSerializeCheck.NullCheck.<Component>newInstance());
    }

    /**
     * Serializes the differences between two components.
     *
     * @param base  The base component to compare against.
     * @param delta The component whose differences will be serialized
     * @param check A check to use to see if each field should be serialized.
     * @return The serialized component, or null if it could not be serialized
     */
    public EntityData.Component serialize(Component base, Component delta, FieldSerializeCheck<Component> check) {
        ClassMetadata<?> componentMetadata = componentLibrary.getMetadata(base.getClass());
        if (componentMetadata == null) {
            logger.error("Unregistered component type: {}", base.getClass());
            return null;
        }

        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        serializeComponentType(delta, componentMessage);

        boolean changed = false;
        for (FieldMetadata field : componentMetadata.iterateFields()) {
            if (check.shouldSerializeField(field, delta)) {
                Object origValue = field.getValue(base);
                Object deltaValue = field.getValue(delta);

                if (!Objects.equal(origValue, deltaValue)) {
                    EntityData.Value value = field.serializeValue(deltaValue);
                    if (value != null) {
                        if (usingFieldIds) {
                            componentMessage.addField(EntityData.NameValue.newBuilder().setNameIndex(field.getId()).setValue(value).build());
                        } else {
                            componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build());
                        }
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            return componentMessage.build();
        }

        return null;
    }

    /**
     * Determines the component class that the serialized component is for.
     *
     * @param componentData
     * @return The component class the given componentData describes, or null if it is unknown.
     */
    public ComponentMetadata<? extends Component> getComponentMetadata(EntityData.Component componentData) {
        if (componentData.hasTypeIndex()) {
            ComponentMetadata<? extends Component> metadata = null;
            if (!idTable.isEmpty()) {
                Class<? extends Component> componentClass = idTable.inverse().get(componentData.getTypeIndex());
                if (componentClass != null) {
                    metadata = componentLibrary.getMetadata(componentClass);
                }
            }
            if (metadata == null) {
                logger.warn("Unable to deserialize unknown component with id: {}", componentData.getTypeIndex());
                return null;
            }
            return metadata;
        } else if (componentData.hasType()) {
            ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(componentData.getType());
            if (metadata == null) {
                logger.warn("Unable to deserialize unknown component type: {}", componentData.getType());
                return null;
            }
            return metadata;
        }
        logger.warn("Unable to deserialize component, no type provided.");

        return null;
    }

    /**
     * @return An immutable copy of the id mapping
     */
    public Map<Class<? extends Component>, Integer> getIdMapping() {
        return ImmutableMap.copyOf(idTable);
    }
}
