/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.context;

import org.terasology.module.sandbox.API;
import org.terasology.registry.DynamicInstanceProvider;

/**
 * Provides classes with the utility objects that belong to the context they are running in.
 * <p>
 * Use dependency injection or this interface to get at the objects you want to use.
 * <p>
 * <p>
 * From this class there can be multiple instances. For example we have the option of letting a client and server run
 * concurrently in one VM, by letting them work with two separate context objects.
 * <p>
 * This class is intended to replace the CoreRegistry and other static means to get utility objects.
 * <p>
 * Contexts must be thread safe!
 */
@API
public interface Context {

    /**
     * @return the object that is known in this context for this type.
     */
    <T> T get(Class<? extends T> type);

    /**
     * Makes the object known in this context to be the object to work with for the given type.
     */
    <T, U extends T> void put(Class<T> type, U object);

    /**
     * Registers an object provider.
     *
     * @param type     The object type.
     * @param provider The object provider.
     * @param <T>      The base type, the type used in @In.
     * @param <U>      The instance type, the type returned by the provider.
     */
    <T, U extends T> void putInstanceProvider(Class<T> type, DynamicInstanceProvider<U> provider);

    <T> DynamicInstanceProvider<T> getInstanceProvider(Class<? extends T> type);
}
