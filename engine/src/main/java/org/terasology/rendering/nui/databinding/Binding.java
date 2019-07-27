/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.databinding;

/**
 */
public interface Binding<T> {

    T get();

    void set(T value);

    /**
     * Makes {@code binding} a child of this {@link Binding}. Child bindings are bound
     * to the same value as the original binding ({@code binding}), but also handle
     * propagation of changes in their value to parent bindings if needed.
     *
     * @param binding The {@link Binding} that must be made a child.
     * @param <C> The type of value bound by {@code binding}.
     * @return The child {@link Binding} that is bound to the same value as {@code binding}.
     */
    default <C> Binding<C> makeChildBinding(Binding<C> binding) {
        return binding;
    }
}
