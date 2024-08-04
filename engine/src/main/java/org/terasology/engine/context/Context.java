// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.context;

import org.terasology.context.annotation.API;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides classes with the utility objects that belong to the context they are running in.
 *
 * Use dependency injection or this interface to get at the objects you want to use.
 *
 *
 * From this class there can be multiple instances. For example we have the option of letting a client and server run
 * concurrently in one VM, by letting them work with two separate context objects.
 *
 * This class is intended to replace the CoreRegistry and other static means to get utility objects.
 *
 * Contexts must be thread safe!
 */
@API
public interface Context {

    /**
     * Get the object that is known in this context for this type.
     */
    <T> T get(Class<T> type);

    /**
     * Get the object that is known in this context for this type. Never null.
     *
     * @throws NoSuchElementException No instance was registered with that type.
     */
    @SuppressWarnings("unused")
    default <T> T getValue(Class<T> type) {
        T value = get(type);
        if (value == null) {
            throw new NoSuchElementException(
                    String.format("%s has no %s", this, type.getName()));
        }
        return value;
    }

    /**
     * Get the object that is known in this context for this type.
     */
    @SuppressWarnings("unused")
    default <T> Optional<T> getMaybe(Class<T> type) {
        return Optional.ofNullable(get(type));
    }

    /**
     * Makes the object known in this context to be the object to work with for the given type.
     */
    <T, U extends T> void put(Class<T> type, U object);

}
