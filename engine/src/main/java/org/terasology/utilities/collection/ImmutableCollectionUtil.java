// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.collection;

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
