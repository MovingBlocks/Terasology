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
import org.terasology.protobuf.EntityData;

/**
 * @param <T> The type of the object this field belongs to
 * @param <U> The type of the field itself
 */
public interface FieldMetadata<T, U> {

    /**
     * @return The ClassMetadata that owns this field
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
     * @param obj The object to obtain the value of this field from
     * @return The value of the field
     */
    U getValue(T obj);

    /**
     * For types that need to be copied (e.g. Vector3f) for safe usage, this method will create a new copy of a field from an object.
     * Otherwise it behaves the same as getValue
     * @param obj The object to copy the field from
     * @return A safe to use copy of the value of this field in the given object
     */
    U getCopyOfValue(T obj);

    /**
     * Sets the value of this field in a target object
     * @param target The object to set the field of
     * @param value The value to set the field to
     */
    void setValue(T target, U value);

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
     * Serializes the given value, that was originally obtained from this field.
     * <p/>
     * This is provided for performance, to avoid obtaining the same value twice via reflection.
     *
     * @param rawValue The value to serialize
     * @return The serialized value
     */
    @SuppressWarnings("unchecked")
    EntityData.Value serializeValue(U rawValue);

    /**
     * Serializes this field in container, returning the serialized value
     * @param container The object to serialize this field for
     * @return The serialized value
     */
    @SuppressWarnings("unchecked")
    EntityData.Value serialize(T container);

    /**
     * Serializes the field for the given object
     *
     * @param container The object containing this field
     * @return The Name-Value pair holding this field
     */
    EntityData.NameValue serializeNameValue(T container, boolean usingFieldIds);

    /**
     * @param value The serialized value
     * @return The resultant deserialized value
     */
    U deserialize(EntityData.Value value);

    /**
     * @param target The object to deserialize the value onto
     * @param value The serialized value
     */
    void deserializeOnto(T target, EntityData.Value value);
}
