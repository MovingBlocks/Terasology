/*
 * Copyright 2019 MovingBlocks
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

import java.util.Collection;

/**
 * Provides the ability to construct a collection with the given items.
 *
 * @param <T> The type of {@link Collection} to construct.
 * @param <E> The elements in the collection.
 */
public interface CollectionCopyConstructor<T extends Collection<E>, E> {
    /**
     * Constructs a collection with the given items.
     */
    T construct(Collection<E> items);
}
