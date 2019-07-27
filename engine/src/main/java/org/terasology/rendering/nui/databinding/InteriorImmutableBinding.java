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
package org.terasology.rendering.nui.databinding;

/**
 * Binds an object that is interior immutable. Since Java does not natively support interior
 * immutability, this {@link Binding} ensures that the source of parent interior immutable values
 * are appropriately updated if the bound value is changed. However, the wrapped {@link #binding} is
 * responsible for enforcing exterior immutability of the object bound to this
 * {@link InteriorImmutableBinding}.
 * <p>
 * If the object being bound to came from another interior immutable object (parent), its binding is
 * stored in {@link #parent}. The {@link #parent} {@link Binding} is used to update the
 * parent object at <i>it's</i> source (through {@link Binding#set(Object) parent.set()}) since
 * it also is interior immutable.
 *
 * @param <T> The type of interior immutable object to override.
 */
public class InteriorImmutableBinding<T> implements Binding<T> {
    private final Binding<T> binding;
    private final InteriorImmutableBinding<?> parent;

    public InteriorImmutableBinding(Binding<T> binding) {this(binding, null);}

    private InteriorImmutableBinding(Binding<T> binding, InteriorImmutableBinding<?> parent) {
        this.binding = binding;
        this.parent = parent;
    }

    /**
     * Creates a new interior immutable binding wrapping the given binding.
     */
    public static <T> InteriorImmutableBinding<T> of(Binding<T> binding) {
        return new InteriorImmutableBinding<>(binding);
    }

    private static <U> void updateParentAtSource(Binding<U> binding) {
        binding.set(binding.get());
    }

    @Override
    public T get() {
        return binding.get();
    }

    @Override
    public void set(T value) {
        binding.set(value);

        // Update the immutable parent at it's source so that a new object can be generated
        if (parent != null) {
            updateParentAtSource(parent);
        }
    }

    @Override
    public <C> Binding<C> makeChildBinding(Binding<C> binding) {
        return new InteriorImmutableBinding<>(binding, this);
    }
}
