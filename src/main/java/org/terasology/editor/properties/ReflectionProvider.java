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
package org.terasology.editor.properties;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.editor.EditorRange;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.internal.ClassMetadataImpl;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;
import org.terasology.entitySystem.metadata.reflect.ReflectionReflectFactory;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withType;


/**
 * @author Immortius
 */
public class ReflectionProvider<T> implements PropertyProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionProvider.class);

    private List<Property<T>> properties = Lists.newArrayList();
    private ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
    private CopyStrategyLibrary copyStrategies = CopyStrategyLibrary.create(reflectFactory);

    public ReflectionProvider(T target) {
        try {
            ClassMetadataImpl<T> classMetadata = new ClassMetadataImpl(target.getClass(), copyStrategies, reflectFactory, "");
            for (Field field : getAllFields(target.getClass(), and(withAnnotation(EditorRange.class), or(withType(Float.TYPE), withType(Float.class))))) {
                EditorRange range = field.getAnnotation(EditorRange.class);
                FieldMetadata<T, Float> fieldMetadata = classMetadata.getField(field.getName(), Float.class);
                Property property = new FloatProperty(target, fieldMetadata, range.min(), range.max());
                properties.add(property);
            }
        } catch (NoSuchMethodException e) {
            logger.error("Cannot provide provide inspection for {}, does not have a default constructor", target.getClass());
        }

    }

    @Override
    public List<Property<T>> getProperties() {
        return properties;
    }
}
