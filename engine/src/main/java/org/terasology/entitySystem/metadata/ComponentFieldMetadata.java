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
package org.terasology.entitySystem.metadata;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Field Metadata for the fields of components. In addition to the standard and replication metadata, has information on whether the field declares ownership over an entity.
 *
 */
public class ComponentFieldMetadata<T extends Component, U> extends ReplicatedFieldMetadata<T, U> {

    private boolean ownedReference;

    public ComponentFieldMetadata(ClassMetadata<T, ?> owner, Field field, CopyStrategy<U> copyStrategy, ReflectFactory factory, boolean replicatedByDefault)
            throws InaccessibleFieldException {
        super(owner, field, copyStrategy, factory, replicatedByDefault);
        ownedReference = field.getAnnotation(Owns.class) != null && (EntityRef.class.isAssignableFrom(field.getType())
                || isCollectionOf(EntityRef.class, field.getGenericType()));
    }

    /**
     * @return Whether this field is marked with the @Owned annotation
     */
    public boolean isOwnedReference() {
        return ownedReference;
    }

    private boolean isCollectionOf(Class<?> targetType, Type genericType) {
        return (Collection.class.isAssignableFrom(getType()) && ReflectionUtil.getTypeParameter(genericType, 0).equals(targetType))
                || (Map.class.isAssignableFrom(getType()) && ReflectionUtil.getTypeParameter(genericType, 1).equals(targetType));
    }
}
