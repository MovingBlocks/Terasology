// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
