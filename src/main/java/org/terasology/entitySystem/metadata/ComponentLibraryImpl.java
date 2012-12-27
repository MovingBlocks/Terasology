/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.metadata;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.core.BooleanTypeHandler;
import org.terasology.entitySystem.metadata.core.ByteTypeHandler;
import org.terasology.entitySystem.metadata.core.DoubleTypeHandler;
import org.terasology.entitySystem.metadata.core.EnumTypeHandler;
import org.terasology.entitySystem.metadata.core.FloatTypeHandler;
import org.terasology.entitySystem.metadata.core.IntTypeHandler;
import org.terasology.entitySystem.metadata.core.ListTypeHandler;
import org.terasology.entitySystem.metadata.core.LongTypeHandler;
import org.terasology.entitySystem.metadata.core.MappedContainerTypeHandler;
import org.terasology.entitySystem.metadata.core.NumberTypeHandler;
import org.terasology.entitySystem.metadata.core.SetTypeHandler;
import org.terasology.entitySystem.metadata.core.StringMapTypeHandler;
import org.terasology.entitySystem.metadata.core.StringTypeHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class ComponentLibraryImpl implements ComponentLibrary {
    private static final int MAX_SERIALIZATION_DEPTH = 1;
    private static final Logger logger = LoggerFactory.getLogger(ComponentLibraryImpl.class);

    private Map<Class<? extends Component>, ComponentMetadata> componentSerializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends Component>> componentTypeLookup = Maps.newHashMap();
    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    public ComponentLibraryImpl() {
        registerTypeHandler(Boolean.class, new BooleanTypeHandler());
        registerTypeHandler(Boolean.TYPE, new BooleanTypeHandler());
        registerTypeHandler(Byte.class, new ByteTypeHandler());
        registerTypeHandler(Byte.TYPE, new ByteTypeHandler());
        registerTypeHandler(Double.class, new DoubleTypeHandler());
        registerTypeHandler(Double.TYPE, new DoubleTypeHandler());
        registerTypeHandler(Float.class, new FloatTypeHandler());
        registerTypeHandler(Float.TYPE, new FloatTypeHandler());
        registerTypeHandler(Integer.class, new IntTypeHandler());
        registerTypeHandler(Integer.TYPE, new IntTypeHandler());
        registerTypeHandler(Long.class, new LongTypeHandler());
        registerTypeHandler(Long.TYPE, new LongTypeHandler());
        registerTypeHandler(String.class, new StringTypeHandler());
        registerTypeHandler(Number.class, new NumberTypeHandler());
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
    }

    public <T extends Component> void registerComponentClass(Class<T> componentClass) {
        try {
            // Check if constructor exists
            componentClass.getConstructor();
        } catch (NoSuchMethodException e) {
            logger.error("Unable to register component class {}: Default Constructor Required", componentClass.getSimpleName());
            return;
        }

        ComponentMetadata<T> info = new ComponentMetadata<T>(componentClass);
        for (Field field : componentClass.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                continue;
            field.setAccessible(true);
            TypeHandler typeHandler = getHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.error("Unsupported field type in component type {}, {} : {}", componentClass.getSimpleName(), field.getName(), field.getGenericType());
            } else {
                info.addField(new FieldMetadata(field, componentClass, typeHandler));
            }
        }
        componentSerializationLookup.put(componentClass, info);
        componentTypeLookup.put(ComponentUtil.getComponentClassName(componentClass).toLowerCase(Locale.ENGLISH), componentClass);
    }

    public <T extends Component> ComponentMetadata<T> getMetadata(Class<T> componentClass) {
        if (componentClass == null) {
            return null;
        }
        return componentSerializationLookup.get(componentClass);
    }

    @Override
    public <T extends Component> ComponentMetadata<? extends T> getMetadata(T component) {
        if (component != null) {
            return componentSerializationLookup.get(component.getClass());
        }
        return null;
    }

    @Override
    public <T extends Component> T copy(T component) {
        ComponentMetadata info = getMetadata(component);
        if (info != null) {
            return (T) info.clone(component);
        }
        return null;
    }

    public ComponentMetadata<?> getMetadata(String componentName) {
        return getMetadata(componentTypeLookup.get(componentName.toLowerCase(Locale.ENGLISH)));
    }

    public Iterator<ComponentMetadata> iterator() {
        return componentSerializationLookup.values().iterator();
    }

    // TODO: Refactor
    private TypeHandler getHandlerFor(Type type, int depth) {
        Class typeClass;
        if (type instanceof Class) {
            typeClass = (Class) type;
        } else if (type instanceof ParameterizedType) {
            typeClass = (Class) ((ParameterizedType) type).getRawType();
        } else {
            logger.error("Cannot obtain class for type {}", type);
            return null;
        }

        if (Enum.class.isAssignableFrom(typeClass)) {
            return new EnumTypeHandler(typeClass);
        }
        // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
        else if (List.class.isAssignableFrom(typeClass)) {
            Type parameter = getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new ListTypeHandler(innerHandler);
                }
            }
            logger.error("List field is not parameterized, or holds unsupported type");
            return null;
        }
        // For sets:
        else if (Set.class.isAssignableFrom(typeClass)) {
            Type parameter = getTypeParameter(type, 0);
            if (parameter != null) {
                TypeHandler innerHandler = getHandlerFor(parameter, depth);
                if (innerHandler != null) {
                    return new SetTypeHandler(innerHandler);
                }
            }
            logger.error("Set field is not parameterized, or holds unsupported type");
            return null;
        }
        // For Maps, createEntityRef the handler for the value type (and maybe key too?)
        else if (Map.class.isAssignableFrom(typeClass)) {
            Type keyParameter = getTypeParameter(type, 0);
            Type contentsParameter = getTypeParameter(type, 1);
            if (keyParameter != null && contentsParameter != null && String.class == keyParameter) {
                TypeHandler valueHandler = getHandlerFor(contentsParameter, depth);
                if (valueHandler != null) {
                    return new StringMapTypeHandler(valueHandler);
                }
            }
            logger.error("Map field is not parameterized, does not have a String key, or holds unsupported values");
        }
        // For know types, just use the handler
        else if (typeHandlers.containsKey(typeClass)) {
            return typeHandlers.get(typeClass);
        }
        // For unknown types of a limited depth, assume they are data holders and use them
        else if (depth <= MAX_SERIALIZATION_DEPTH && !Modifier.isAbstract(typeClass.getModifiers()) && !typeClass.isLocalClass() && !(typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
            try {
                // Check if constructor exists
                typeClass.getConstructor();
            } catch (NoSuchMethodException e) {
                logger.error("Unable to register field of type {}: no publicly accessible default constructor", typeClass.getSimpleName());
                return null;
            }

            logger.warn("Handling serialization of type {} via MappedContainer", typeClass);
            MappedContainerTypeHandler mappedHandler = new MappedContainerTypeHandler(typeClass);
            for (Field field : typeClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers()))
                    continue;

                field.setAccessible(true);
                TypeHandler handler = getHandlerFor(field.getGenericType(), depth + 1);
                if (handler == null) {
                    logger.error("Unsupported field type in component type {}, {} : {}", typeClass.getSimpleName(), field.getName(), field.getGenericType());
                } else {
                    mappedHandler.addField(new FieldMetadata(field, typeClass, handler));
                }
            }
            return mappedHandler;
        }

        return null;
    }

    // TODO - Improve parameter lookup to go up the inheritance tree more
    private Type getTypeParameter(Type type, int parameter) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length < parameter + 1) {
            return null;
        }
        return parameterizedType.getActualTypeArguments()[parameter];
    }
}
