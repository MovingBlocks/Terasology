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
package org.terasology.reflection.reflect;

/**
 * Provider get and set access to a field. Where possible this will use getter and setter methods (following the Java Bean standards), but otherwise it will
 * access the field directly.
 *
 * @param <T> The type of the object that holds this field
 * @param <U> The type of the field itself
 */
public interface FieldAccessor<T, U> {

    /**
     * Gets the value from the target object
     *
     * @param target The object to access the field of
     * @return The value of the field for the given target
     */
    U getValue(T target);

    /**
     * Sets the value for the target object
     *
     * @param target The object to access the field of
     * @param value  The value to set the field to
     */
    void setValue(T target, U value);
}
