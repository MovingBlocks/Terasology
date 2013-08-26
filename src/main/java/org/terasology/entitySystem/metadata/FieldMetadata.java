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

import org.terasology.network.Replicate;

import java.lang.reflect.Field;

/**
 * Provides information on a field, and the ability to set and get that field.
 *
 * @param <T> The type of the object this field belongs to
 * @param <U> The type of the field itself
 */
public interface FieldMetadata<T, U> {

    /**
     * @return The class that owns this field
     */
    ClassMetadata<T> getOwner();

    /**
     * @return The name of the field
     */
    String getName();

    /**
     * @return The type of the field
     */
    Class<U> getType();

    /**
     * @return The assigned id for this field, if any
     */
    byte getId();

    /**
     * @param id The id to assign for this field
     */
    void setId(byte id);

    /**
     * Obtains the value of the field from a object which is an instance of the owning type.
     *
     * @param from The object to obtain the value of this field from
     * @return The value of the field
     */
    Object getValue(Object from);

    /**
     * Obtains the value of the field from a object which is an instance of the owning type.
     * This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param from The object to obtain the value of this field from
     * @return The value of the field
     */
    U getValueChecked(T from);

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field from an object.
     * Otherwise it behaves the same as getValue
     *
     * @param from The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    Object getCopyOfValue(Object from);

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field from an object.
     * Otherwise it behaves the same as getValue
     * This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param from The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    U getCopyOfValueChecked(T from);

    /**
     * Sets the value of this field in a target object
     *
     * @param target The object to set the field of
     * @param value  The value to set the field to
     */
    void setValue(Object target, Object value);

    /**
     * Sets the value of this field in a target object
     * This method is checked to conform to the generic parameters of the FieldMetadata
     *
     * @param target The object to set the field of
     * @param value  The value to set the field to
     */
    void setValueChecked(T target, U value);

    /**
     * @return Whether this field should be replicated on the network
     */
    boolean isReplicated();

    /**
     * @return Whether this field is marked with the @Owned annotation
     */
    boolean isOwnedReference();

    /**
     * @return The replication information for this field, or null if it isn't marked with the Replicate annotation
     */
    Replicate getReplicationInfo();

    /**
     * @return The underlying java field
     */
    Field getField();
}
