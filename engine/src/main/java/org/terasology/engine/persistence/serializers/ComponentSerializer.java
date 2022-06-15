// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.metadata.ComponentFieldMetadata;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedData;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.Module;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.metadata.FieldMetadata;
import reactor.core.publisher.Flux;

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
@SuppressWarnings("UnusedReturnValue")
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
     * @return The component described by the componentData, or null if it couldn't be deserialized
     */
    public <T extends Component<T>> T deserialize(EntityData.Component componentData) {
        return deserialize(componentData, null);
    }

    /**
     * @param context       The module this component belongs to, or null if it is not being loaded from a module
     * @return The component described by the componentData, or null if it couldn't be deserialized
     */
    public <T extends Component<T>> T deserialize(EntityData.Component componentData, Module context) {
        ComponentMetadata<T> componentMetadata = getComponentMetadata(componentData, context);
        if (componentMetadata != null) {
            T component = componentMetadata.newInstance();
            return deserializeOnto(component, componentData, componentMetadata, FieldSerializeCheck.NullCheck.newInstance());
        } else {
            logger.warn("Unable to deserialize unknown component type: {}", componentData.getType());
        }
        return null;
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @return The target component.
     */
    public <T extends Component<T>> T deserializeOnto(T target, EntityData.Component componentData) {
        return deserializeOnto(target, componentData, FieldSerializeCheck.NullCheck.newInstance(), null);
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @param context       The module that contains the component being deserialized. May be null if it is not contained in a module.
     * @return The target component.
     */
    public <T extends Component<T>> T deserializeOnto(T target, EntityData.Component componentData, Module context) {
        return deserializeOnto(target, componentData, FieldSerializeCheck.NullCheck.newInstance(), context);
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @return The target component.
     */
    public <T extends Component<T>> T deserializeOnto(T target, EntityData.Component componentData, FieldSerializeCheck<T> fieldCheck) {
        return deserializeOnto(target, componentData, fieldCheck, null);
    }

    /**
     * Deserializes the componentData on top of the target component. Any fields that are not present in the componentData,
     * or which cannot be deserialized, are left unaltered.
     *
     * @param context       The module this component is being deserialized from, or null if it isn't within a module
     * @return The target component.
     */
    public <T extends Component<T>> T deserializeOnto(T target, EntityData.Component componentData,
                                     FieldSerializeCheck<T> fieldCheck, Module context) {
        ComponentMetadata<T> componentMetadata = getComponentMetadata(componentData, context);
        if (componentMetadata != null) {
            return deserializeOnto(target, componentData, componentMetadata, fieldCheck);
        } else {
            logger.warn("Unable to deserialize unknown component type: {}", componentData.getType());
        }
        return target;
    }


    private <T extends Component<T>> T deserializeOnto(T targetComponent, EntityData.Component componentData,
                                                            ComponentMetadata<T> componentMetadata, FieldSerializeCheck<T> fieldCheck) {
        Serializer<T> serializer = typeHandlerLibrary.getSerializerFor(componentMetadata);
        Map<FieldMetadata<T, ?>, PersistedData> dataMap = Maps.newHashMapWithExpectedSize(componentData.getFieldCount());
        for (EntityData.NameValue field : componentData.getFieldList()) {
            FieldMetadata<T, ?> fieldInfo = null;
            if (field.hasNameIndex()) {
                fieldInfo = componentMetadata.getField(field.getNameIndex());
            } else if (field.hasName()) {
                fieldInfo = componentMetadata.getField(field.getName());
            }
            if (fieldInfo != null) {
                dataMap.put(fieldInfo, new ProtobufPersistedData(field.getValue()));
            } else if (field.hasName()) {
                logger.warn("Cannot deserialize unknown field '{}' onto '{}'", field.getName(), componentMetadata.getId());
            }
        }
        serializer.deserializeOnto(targetComponent, dataMap, fieldCheck);
        return targetComponent;
    }


    /**
     * Serializes a component.
     *
     * @return The serialized component, or null if it could not be serialized.
     */
    public <T extends Component<T>> EntityData.Component serialize(T component) {
        return serialize(component, FieldSerializeCheck.NullCheck.newInstance());
    }

    /**
     * Serializes a component.
     *
     * @param check     A check to use to see if each field should be serialized.
     * @return The serialized component, or null if it could not be serialized.
     */
    public <T extends Component<T>> EntityData.Component serialize(T component, FieldSerializeCheck<T> check) {
        @SuppressWarnings("unchecked") ComponentMetadata<T> componentMetadata = (ComponentMetadata<T>)
                componentLibrary.getMetadata(component.getClass());
        if (componentMetadata == null) {
            logger.error("Unregistered component type: {}", component.getClass());
            return null;
        }
        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        serializeComponentType(componentMetadata, componentMessage);

        Serializer<T> serializer = typeHandlerLibrary.getSerializerFor(componentMetadata);
        componentMetadata.getFields().stream()
                .filter(field -> check.shouldSerializeField(field, component))
                .forEach(field -> {
            PersistedData result = serializer.serialize(field, component, serializationContext);
            if (!result.isNull()) {
                EntityData.Value itemValue = ((ProtobufPersistedData) result).getValue();
                if (usingFieldIds) {
                    componentMessage.addField(EntityData.NameValue.newBuilder().setNameIndex(field.getId()).setValue(itemValue));
                } else {
                    componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(itemValue));
                }
            }
        });

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
    public <T extends Component<T>> EntityData.Component serialize(T base, T delta) {
        return serialize(base, delta, FieldSerializeCheck.NullCheck.newInstance());
    }

    /**
     * Serializes the differences between two components.
     *
     * @param base  The base component to compare against.
     * @param delta The component whose differences will be serialized
     * @param check A check to use to see if each field should be serialized.
     * @return The serialized component, or null if it could not be serialized
     */
    public <T extends Component<T>> EntityData.Component serialize(T base, T delta, FieldSerializeCheck<T> check) {
        @SuppressWarnings("unchecked") ComponentMetadata<T> componentMetadata = (ComponentMetadata<T>) componentLibrary.getMetadata(base.getClass());
        if (componentMetadata == null) {
            logger.error("Unregistered component type: {}", base.getClass());
            return null;
        }

        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        serializeComponentType(componentMetadata, componentMessage);

        Serializer<T> serializer = typeHandlerLibrary.getSerializerFor(componentMetadata);
        var changedFields = Flux.fromIterable(componentMetadata.getFields())
                .filter(field -> check.shouldSerializeField(field, delta) && serializer.getHandlerFor(field) != null)
                .mapNotNull(field -> extracted(base, delta, serializer, field))
                .collectList()
                .block();
        if (changedFields == null || changedFields.isEmpty()) {
            return null;
        }
        componentMessage.addAllField(changedFields);
        return componentMessage.build();
    }

    private <T extends Component<T>, F> EntityData.NameValue extracted(T base, T delta, Serializer<T> serializer,
                                                                       ComponentFieldMetadata<T, F> field) {
        F origValue = field.getValue(base);
        F deltaValue = field.getValue(delta);

        if (!Objects.equal(origValue, deltaValue)) {
            PersistedData value = serializer.serializeValue(field, deltaValue, serializationContext);
            if (!value.isNull()) {
                EntityData.Value dataValue = ((ProtobufPersistedData) value).getValue();
                EntityData.NameValue.Builder nameValueBuilder = getNameValueBuilder(field);
                nameValueBuilder.setValue(dataValue);
                return nameValueBuilder.build();
            }
        }
        return null;
    }

    private <T extends Component<T>, F> EntityData.NameValue.Builder getNameValueBuilder(ComponentFieldMetadata<T, F> field) {
        var nameValueBuilder = EntityData.NameValue.newBuilder();
        if (usingFieldIds) {
            nameValueBuilder.setNameIndex(field.getId());
        } else {
            nameValueBuilder.setName(field.getName());
        }
        return nameValueBuilder;
    }

    /**
     * Determines the component class that the serialized component is for.
     *
     * @return The component class the given componentData describes, or null if it is unknown.
     */
    public <T extends Component<T>> ComponentMetadata<T> getComponentMetadata(EntityData.Component componentData) {
        return getComponentMetadata(componentData, null);
    }

    /**
     * Determines the component class that the serialized component is for.
     *
     * @param context       the module this component is being loaded from
     * @return The component class the given componentData describes, or null if it is unknown.
     */
    public <T extends Component<T>> ComponentMetadata<T> getComponentMetadata(EntityData.Component componentData, Module context) {
        if (componentData.hasTypeIndex()) {
            ComponentMetadata<T> metadata = null;
            if (!idTable.isEmpty()) {
                @SuppressWarnings("unchecked") Class<T> componentClass = (Class<T>) idTable.inverse().get(componentData.getTypeIndex());
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
            ComponentMetadata<T> metadata;
            if (context != null) {
                //noinspection unchecked
                metadata = (ComponentMetadata<T>) componentLibrary.resolve(componentData.getType(), context);
            } else {
                //noinspection unchecked
                metadata = (ComponentMetadata<T>) componentLibrary.resolve(componentData.getType());
            }
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
