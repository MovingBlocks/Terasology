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
package org.terasology.utilities.collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.terasology.reflection.TypeInfo;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class ImmutableCollectionUtil {
    /**
     * Creates a guava {@link ImmutableCollection} of the given type containing the given items.
     * Note that guava does not allow null items in the collection, so the null items are filtered out.
     */
    public static <T extends Collection<E>, E> T copyOf(TypeInfo<T> collectionType, Collection<E> items) {
        Class<T> rawType = collectionType.getRawType();

        // Guava does not support null elements

        Collection<E> nonNullItems = items.stream().filter(Objects::nonNull).collect(Collectors.toList());

        // TODO: Support more Guava types?

        if (SortedSet.class.isAssignableFrom(rawType)) {
            return (T) ImmutableSortedSet.copyOf(nonNullItems);
        }

        if (Set.class.isAssignableFrom(rawType)) {
            return (T) ImmutableSet.copyOf(nonNullItems);
        }

        return (T) ImmutableList.copyOf(nonNullItems);
    }
}
