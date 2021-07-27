// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.bindings;

import org.terasology.nui.databinding.Binding;

import java.util.function.Function;

public class MappingBinding<T, R> implements Binding<T> {
    private final Binding<R> internalBinging;
    private final Function<T, R> setMapping;
    private final Function<R, T> getMapping;

    public MappingBinding(Binding<R> internalBinging, Function<T, R> setMapping, Function<R, T> getMapping) {
        this.internalBinging = internalBinging;
        this.setMapping = setMapping;
        this.getMapping = getMapping;
    }

    @Override
    public T get() {
        return getMapping.apply(internalBinging.get());
    }

    @Override
    public void set(T value) {
        internalBinging.set(setMapping.apply(value));
    }
}
