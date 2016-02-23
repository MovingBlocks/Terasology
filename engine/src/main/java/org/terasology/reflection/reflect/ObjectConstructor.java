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
 * Providers the ability to construct an instance of a type.
 * <br><br>
 * These types must provide a default constructor, which will be invoked.
 *
 * @param <T> The type of the class to construct instances of
 */
@FunctionalInterface
public interface ObjectConstructor<T> {

    /**
     * @return A new instance of the object type
     */
    T construct();
}
