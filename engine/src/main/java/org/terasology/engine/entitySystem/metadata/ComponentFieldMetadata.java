// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.reflection.copy.strategy.EntityCopyStrategy;
import org.terasology.engine.utilities.ReflectionUtil;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Field Metadata for the fields of components.
 * In addition to the standard and replication metadata, has information on whether the field declares ownership over an entity.
 *
 */
public class ComponentFieldMetadata<T extends Component, U> extends ReplicatedFieldMetadata<T, U> {

    private final boolean ownedReference;

    private final CopyStrategy<U> copyWithOwnedEntitiesStrategy;

    public ComponentFieldMetadata(ClassMetadata<T, ?> owner, Field field, CopyStrategyLibrary copyStrategyLibrary,
                                  ReflectFactory factory, boolean replicatedByDefault)
            throws InaccessibleFieldException {
        super(owner, field, copyStrategyLibrary, factory, replicatedByDefault);
        ownedReference = field.getAnnotation(Owns.class) != null && (EntityRef.class.isAssignableFrom(field.getType())
                || isCollectionOf(EntityRef.class, field.getGenericType()));
        if (ownedReference) {
            copyWithOwnedEntitiesStrategy = (CopyStrategy<U>) copyStrategyLibrary
                            .createCopyOfLibraryWithStrategy(EntityRef.class, EntityCopyStrategy.INSTANCE)
                            .getStrategy(field.getGenericType());
        } else {
            copyWithOwnedEntitiesStrategy = copyStrategy;
        }
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

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field
     * from an object, and if the field is marked @Owns, any EntityRefs in the value are copied too. Otherwise it
     * behaves the same as getValue
     *
     * @param from The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    public U getCopyOfValueWithOwnedEntities(Object from) {
        U value = getValue(from);
        return (value != null) ? copyWithOwnedEntitiesStrategy.copy(value) : null;
    }

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field
     * from an object, and if the field is marked @Owns, any EntityRefs in the value are copied too. Otherwise it
     * behaves the same as getValue This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param from The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    public U getCopyOfValueWithOwnedEntitiesChecked(T from) {
        return getCopyOfValueWithOwnedEntities(from);
    }
}
