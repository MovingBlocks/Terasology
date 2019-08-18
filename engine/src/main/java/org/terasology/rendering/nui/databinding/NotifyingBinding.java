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
 * Notifies implementors when a value is retrieved from or set to the delegate {@link Binding}
 * via the {@link #onGet()} and {@link #onSet()} methods respectively.
 *
 * @param <T> The type of value bound by the {@link Binding}.
 */
public abstract class NotifyingBinding<T> implements Binding<T> {
    private Binding<T> delegate;

    public NotifyingBinding() {
        this(new DefaultBinding<>());
    }

    public NotifyingBinding(T value) {
        this();
        set(value);
    }

    public NotifyingBinding(Binding<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <C> Binding<C> makeChildBinding(Binding<C> binding) {return delegate.makeChildBinding(binding);}

    @Override
    public final T get() {
        onGet();
        return delegate.get();
    }

    /**
     * Called when {@link Binding#get()} is called on this {@link NotifyingBinding}.
     */
    protected void onGet() { }

    @Override
    public final void set(T value) {
        delegate.set(value);
        onSet();
    }

    /**
     * Called after the value is set in this {@link NotifyingBinding}.
     */
    protected void onSet() { }
}
