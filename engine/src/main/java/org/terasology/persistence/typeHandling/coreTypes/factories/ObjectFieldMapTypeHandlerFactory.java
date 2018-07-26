/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes.factories;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class ObjectFieldMapTypeHandlerFactory implements TypeHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectFieldMapTypeHandlerFactory.class);

    private ConstructorLibrary constructorLibrary;

    public ObjectFieldMapTypeHandlerFactory(ConstructorLibrary constructorLibrary) {
        this.constructorLibrary = constructorLibrary;
    }

    @Override
    public <T> Optional<TypeHandler<T>> create(TypeInfo<T> typeInfo, TypeSerializationLibrary typeSerializationLibrary) {
        Class<? super T> typeClass = typeInfo.getRawType();

        if (!Modifier.isAbstract(typeClass.getModifiers())
                && !typeClass.isLocalClass()
                && !(typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
            Map<Field, TypeHandler<?>> fieldTypeHandlerMap = Maps.newHashMap();

            getResolvedFields(typeInfo).forEach(
                    (field, fieldType) ->
                            fieldTypeHandlerMap.put(
                                    field,
                                    new RuntimeDelegatingTypeHandler(
                                            typeSerializationLibrary.getTypeHandler(fieldType),
                                            TypeInfo.of(fieldType),
                                            typeSerializationLibrary
                                    )
                            )
            );

            ObjectFieldMapTypeHandler<T> mappedHandler =
                    new ObjectFieldMapTypeHandler<>(constructorLibrary.get(typeInfo), fieldTypeHandlerMap);

            return Optional.of(mappedHandler);
        }

        return Optional.empty();
    }

    private <T> Map<Field, Type> getResolvedFields(TypeInfo<T> typeInfo) {
        Map<Field, Type> fields = Maps.newLinkedHashMap();

        Type type = typeInfo.getType();
        Class<? super T> rawType = typeInfo.getRawType();

        while (!Object.class.equals(rawType)) {
            for (Field field : rawType.getDeclaredFields()) {
                field.setAccessible(true);
                Type fieldType = ReflectionUtil.resolveType(type, field.getGenericType());
                fields.put(field, fieldType);
            }

            rawType = rawType.getSuperclass();
        }

        return fields;
    }
}
