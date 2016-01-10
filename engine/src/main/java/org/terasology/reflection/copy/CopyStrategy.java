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
package org.terasology.reflection.copy;

/**
 * A strategy for copying an object/type.
 * This may be returning the object unchanged for immutable or otherwise safe to share types.
 * Copy strategies are deep-copies - contents should also be copied where appropriate.
 */
@FunctionalInterface
public interface CopyStrategy<T> {

    /**
     * @param value The value to copy
     * @return A safe to use copy of the given value.
     */
    T copy(T value);

}
