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
 * A {@link Binding} that calls the {@link #callback} when the objects inside it is updated.
 * It relies on {@link org.terasology.rendering.nui.widgets.types.TypeWidgetFactory TypeWidgetFactories}
 * using {@link Binding#makeChildBinding(Binding)} to create bindings for objects inside the object in
 * this binding.
 */
public class InteriorMutationNotifyingBinding<T> implements Binding<T> {
    private final Binding<T> binding;
    private final Callback callback;

    public InteriorMutationNotifyingBinding(Binding<T> binding, Callback callback) {
        this.binding = binding;
        this.callback = callback;
    }

    @Override
    public T get() {
        return binding.get();
    }

    @Override
    public void set(T value) {
        binding.set(value);
    }

    @Override
    public <C> Binding<C> makeChildBinding(Binding<C> binding) {
        return new InteriorMutationNotifyingBinding<>(
            new NotifyingBinding<C>(binding) {
                @Override
                protected void onSet() {
                    callback.onInteriorValueMutated();
                }
            },
            callback
        );
    }

    public interface Callback {
        void onInteriorValueMutated();
    }
}
