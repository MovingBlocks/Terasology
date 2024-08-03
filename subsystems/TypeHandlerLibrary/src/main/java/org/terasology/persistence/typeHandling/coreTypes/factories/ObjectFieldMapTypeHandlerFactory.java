// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObjectFieldMapTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFieldMapTypeHandlerFactory.class);
    private static final List<String> PRIVATE_OVERRIDE_ANNOTATIONS = List.of("org.terasology.nui.LayoutConfig");

    private ConstructorLibrary constructorLibrary;

    public ObjectFieldMapTypeHandlerFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeHandlerContext context) {
        Class<? super T> typeClass = typeInfo.getRawType();

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

                    if (Arrays.stream(field.getAnnotations())
                            .anyMatch(annotation -> PRIVATE_OVERRIDE_ANNOTATIONS.contains(annotation.getClass().getCanonicalName()))) {
                        field.setAccessible(true);
                    } else {
                        if (Modifier.isPrivate(field.getModifiers()) && ReflectionUtil.findGetter(field) == null
                                && ReflectionUtil.findSetter(field) == null) {
                            logger.atWarn().addArgument(field.getName()).addArgument(rawType.getTypeName()).
                                    log("Field {}#{} will not be serialised. Terasology no longer supports serialising private fields.");
                             continue;
                        }
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
