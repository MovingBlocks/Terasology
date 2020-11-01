/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling;

import java.lang.reflect.Type;

/**
 * Creates new instances of the type {@link T} to be used during deserialization.
 * @param <T> The type of which new instances are to be created.
 */
public interface InstanceCreator<T> {
    /**
     * This method is called during deserialization to create an instance of the
     * specified type. The fields of the returned instance are overwritten with the deserialized data.
     * Since the prior contents of the object are destroyed and overwritten, always return different instances. In particular, do not return a common instance,
     * always use {@code new} to create a new instance.
     *
     * @param type the parameterized type {@link T} represented as a {@link Type}.
     * @return a default object instance of type {@link T}.
     */
    T createInstance(Type type);
}
