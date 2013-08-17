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
import org.terasology.editor.EditorRange;

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
public class ReflectionProvider implements PropertyProvider {
    private List<Property<?>> properties = Lists.newArrayList();

    public ReflectionProvider(Object target) {
        for (Field field : getAllFields(target.getClass(), and(withAnnotation(EditorRange.class), or(withType(Float.TYPE), withType(Float.class))))) {
            EditorRange range = field.getAnnotation(EditorRange.class);
            field.setAccessible(true);
            Property property = new FloatProperty(target, field, field.getName(), range.min(), range.max());
            properties.add(property);
        }
    }

    @Override
    public List<Property<?>> getProperties() {
        return properties;
    }
}
