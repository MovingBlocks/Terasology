// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.properties;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.nui.properties.Range;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

import java.lang.reflect.Field;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withType;

public class ReflectionProvider<T> implements PropertyProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionProvider.class);

    private List<Property<T>> properties = Lists.newArrayList();

    public ReflectionProvider(T target, Context context) {
        try {
            ReflectFactory reflectFactory = context.get(ReflectFactory.class);
            CopyStrategyLibrary copyStrategies = context.get(CopyStrategyLibrary.class);
            ClassMetadata<T, ?> classMetadata = new DefaultClassMetadata<>("engine:empty", (Class<T>) target.getClass(),
                    reflectFactory, copyStrategies);
            for (Field field : getAllFields(target.getClass(),
                    and(withAnnotation(Range.class), or(withType(Float.TYPE), withType(Float.class))))) {
                Range range = field.getAnnotation(Range.class);
                FieldMetadata<T, Float> fieldMetadata = (FieldMetadata<T, Float>) classMetadata.getField(field.getName());
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
