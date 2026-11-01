// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.metadata.ReplicatedFieldMetadata;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedData;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.Module;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.metadata.FieldMetadata;

import java.util.Map;

/**
 * ComponentSerializer provides the ability to serialize and deserialize between Components and the protobuf
 * EntityData.Component
 * <br><br>
 * If provided with a idTable, then the components will be serialized and deserialized using those ids rather
 * than the names of each component, saving some space.
 * <br><br>
 * When serializing, a FieldSerializeCheck can be provided to determine whether each field should be serialized or not
 *
 */
public class ComponentSerializer {

    private static final Logger logger = LoggerFactory.getLogger(ComponentSerializer.class);

    private ComponentLibrary componentLibrary;
    private BiMap<Class<? extends Component>, Integer> idTable = ImmutableBiMap.<Class<? extends Component>, Integer>builder().build();
    private boolean usingFieldIds;
    private TypeHandlerLibrary typeHandlerLibrary;
    private ProtobufPersistedDataSerializer serializationContext;

    /**
     * Creates the component serializer.
     *
     * @param componentLibrary The component library used to provide information on each component and its fields.
     */
    public ComponentSerializer(ComponentLibrary componentLibrary, TypeHandlerLibrary typeHandlerLibrary) {
        this.componentLibrary = componentLibrary;
        this.typeHandlerLibrary = typeHandlerLibrary;
        this.serializationContext = new ProtobufPersistedDataSerializer();
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
        return deserialize(componentData, null);
    }

    /**
     * @param componentData
     * @param context       The module this component belongs to, or null if it is not being loaded from a module
     * @return The component described by the componentData, or null if it couldn't be deserialized
     */
    public Component deserialize(EntityData.Component componentData, Module context) {
        ComponentMetadata<? extends Component> componentMetadata = getComponentMetadata(componentData, context);
        if (componentMetadata != null) {
            Component component = componentMetadata.newInstance();
            return deserializeOnto(component, componentData, componentMetadata, FieldSerializeCheck.NullCheck.<Component>newInstance());
        } else {
            logger.atWarn().log("Unable to deserialize unknown component type: {}", componentData.getType());
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
        return deserializeOnto(target, componentData, FieldSerializeCheck.NullCheck.<Component>newInstance(), null);
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @param target
     * @param componentData
     * @param context       The module that contains the component being deserialized. May be null if it is not contained in a module.
     * @return The target component.
     */
    public Component deserializeOnto(Component target, EntityData.Component componentData, Module context) {
        return deserializeOnto(target, componentData, FieldSerializeCheck.NullCheck.<Component>newInstance(), context);
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
        return deserializeOnto(target, componentData, fieldCheck, null);
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @param target
     * @param componentData
     * @param fieldCheck
     * @param context       The module this component is being deserialized from, or null if it isn't within a module
     * @return The target component.
     */
    public Component deserializeOnto(Component target, EntityData.Component componentData,
                                     FieldSerializeCheck<Component> fieldCheck, Module context) {
        ComponentMetadata<? extends Component> componentMetadata = getComponentMetadata(componentData, context);
        if (componentMetadata != null) {
            return deserializeOnto(target, componentData, componentMetadata, fieldCheck);
        } else {
            logger.atWarn().log("Unable to deserialize unknown component type: {}", componentData.getType());
        }
        return target;
    }


    private <T extends Component> Component deserializeOnto(Component targetComponent, EntityData.Component componentData,
                                                            ComponentMetadata<T> componentMetadata, FieldSerializeCheck<Component> fieldCheck) {
        Serializer serializer = typeHandlerLibrary.getSerializerFor(componentMetadata);
        Map<FieldMetadata<?, ?>, PersistedData> dataMap = Maps.newHashMapWithExpectedSize(componentData.getFieldCount());
        for (EntityData.NameValue field : componentData.getFieldList()) {
            FieldMetadata<?, ?> fieldInfo = null;
            if (field.hasNameIndex()) {
                fieldInfo = componentMetadata.getField(field.getNameIndex());
            } else if (field.hasName()) {
                fieldInfo = componentMetadata.getField(field.getName());
            }
            if (fieldInfo != null) {
                dataMap.put(fieldInfo, new ProtobufPersistedData(field.getValue()));
            } else if (field.hasName()) {
                logger.atWarn().log("Cannot deserialize unknown field '{}' onto '{}'", field.getName(), componentMetadata.getId());
            }
        }
        serializer.deserializeOnto(targetComponent, dataMap, fieldCheck);
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

        ComponentMetadata<?> componentMetadata = componentLibrary.getMetadata(component.getClass());
        if (componentMetadata == null) {
            logger.atError().log("Unregistered component type: {}", component.getClass());
            return null;
        }
        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        serializeComponentType(componentMetadata, componentMessage);

        Serializer serializer = typeHandlerLibrary.getSerializerFor(componentMetadata);
        for (ReplicatedFieldMetadata<?, ?> field : componentMetadata.getFields()) {
            if (check.shouldSerializeField(field, component)) {
                PersistedData result = serializer.serialize(field, component, serializationContext);
                if (!result.isNull()) {
                    EntityData.Value itemValue = ((ProtobufPersistedData) result).getValue();
                    if (usingFieldIds) {
                        componentMessage.addField(EntityData.NameValue.newBuilder().setNameIndex(field.getId()).setValue(itemValue));
                    } else {
                        componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(itemValue));
                    }
                }
            }
        }

        return componentMessage.build();
    }

    private void serializeComponentType(ComponentMetadata<?> componentMetadata, EntityData.Component.Builder componentMessage) {
        Integer compId = idTable.get(componentMetadata.getType());
        if (compId != null) {
            componentMessage.setTypeIndex(compId);
        } else {
            componentMessage.setType(componentMetadata.getId().toString());
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
        ComponentMetadata<?> componentMetadata = componentLibrary.getMetadata(base.getClass());
        if (componentMetadata == null) {
            logger.atError().log("Unregistered component type: {}", base.getClass());
            return null;
        }

        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        serializeComponentType(componentMetadata, componentMessage);

        Serializer serializer = typeHandlerLibrary.getSerializerFor(componentMetadata);
        boolean changed = false;
        for (ReplicatedFieldMetadata field : componentMetadata.getFields()) {
            if (check.shouldSerializeField(field, delta) && serializer.getHandlerFor(field) != null) {
                Object origValue = field.getValue(base);
                Object deltaValue = field.getValue(delta);

                if (!Objects.equal(origValue, deltaValue)) {
                    PersistedData value = serializer.serializeValue(field, deltaValue, serializationContext);
                    if (!value.isNull()) {
                        EntityData.Value dataValue = ((ProtobufPersistedData) value).getValue();
                        if (usingFieldIds) {
                            componentMessage.addField(EntityData.NameValue.newBuilder().setNameIndex(field.getId()).setValue(dataValue).build());
                        } else {
                            componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(dataValue).build());
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
        return getComponentMetadata(componentData, null);
    }

    /**
     * Determines the component class that the serialized component is for.
     *
     * @param componentData
     * @param context       the module this component is being loaded from
     * @return The component class the given componentData describes, or null if it is unknown.
     */
    public ComponentMetadata<? extends Component> getComponentMetadata(EntityData.Component componentData, Module context) {
        if (componentData.hasTypeIndex()) {
            ComponentMetadata<? extends Component> metadata = null;
            if (!idTable.isEmpty()) {
                Class<? extends Component> componentClass = idTable.inverse().get(componentData.getTypeIndex());
                if (componentClass != null) {
                    metadata = componentLibrary.getMetadata(componentClass);
                }
            }
            if (metadata == null) {
                logger.atWarn().log("Unable to deserialize unknown component with id: {}", componentData.getTypeIndex());
                return null;
            }
            return metadata;
        } else if (componentData.hasType()) {
            ComponentMetadata<? extends Component> metadata;
            if (context != null) {
                metadata = componentLibrary.resolve(componentData.getType(), context);
            } else {
                metadata = componentLibrary.resolve(componentData.getType());
            }
            if (metadata == null) {
                logger.atWarn().log("Unable to deserialize unknown component type: {}", componentData.getType());
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
