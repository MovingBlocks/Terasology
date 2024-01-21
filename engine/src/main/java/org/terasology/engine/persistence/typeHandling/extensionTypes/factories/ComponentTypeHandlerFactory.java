// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes.factories;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.reflection.ReflectionUtil;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link TypeHandlerFactory} for serialising component types.
 * It is similar to {@link org.terasology.persistence.typeHandling.coreTypes.factories.ObjectFieldMapTypeHandlerFactory}
 * but does not allow serialising private fields.
 */
public class ComponentTypeHandlerFactory implements TypeHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(ComponentTypeHandlerFactory.class);

    private ConstructorLibrary constructorLibrary;

    public ComponentTypeHandlerFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        Class<? super T> typeClass = typeInfo.getRawType();
        if (!Component.class.isAssignableFrom(typeClass)) {
            return Optional.empty();
        }

        if (!Modifier.isAbstract(typeClass.getModifiers())
                && !typeClass.isLocalClass()
                && !(typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
            Map<Field, TypeHandler<?>> fieldTypeHandlerMap = Maps.newLinkedHashMap();

            getResolvedFields(typeInfo).forEach(
                    (field, fieldType) -> {
                        Optional<TypeHandler<?>> declaredFieldTypeHandler =
                                context.getTypeHandlerLibrary().getTypeHandler(fieldType);

                        TypeInfo<?> fieldTypeInfo = TypeInfo.of(fieldType);

                        fieldTypeHandlerMap.put(
                                field,
                                new RuntimeDelegatingTypeHandler(
                                        declaredFieldTypeHandler.orElse(null),
                                        fieldTypeInfo,
                                        context
                                )
                        );
                    }
            );

            ObjectFieldMapTypeHandler<T> mappedHandler =
                    new ObjectFieldMapTypeHandler<>(constructorLibrary.get(typeInfo), fieldTypeHandlerMap);

            return Optional.of(mappedHandler);
        }

        return Optional.empty();
    }

    private <T> Map<Field, Type> getResolvedFields(TypeInfo<T> typeInfo) {
        return AccessController.doPrivileged((PrivilegedAction<Map<Field, Type>>) () -> {
            Map<Field, Type> fields = Maps.newLinkedHashMap();

            Type type = typeInfo.getType();
            Class<? super T> rawType = typeInfo.getRawType();

            while (!Object.class.equals(rawType)) {
                for (Field field : rawType.getDeclaredFields()) {
                    if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    if (!Modifier.isPublic(field.getModifiers())) {
                        logger.warn("Field {}#{} will not be serialised. Terasology no longer supports serialising private fields.", field.getName(), rawType.getTypeName());
                        continue;
                    }

                    Type fieldType = ReflectionUtil.resolveType(type, field.getGenericType());
                    fields.put(field, fieldType);
                }

                rawType = rawType.getSuperclass();
            }

            return fields;
        });
    }
}
