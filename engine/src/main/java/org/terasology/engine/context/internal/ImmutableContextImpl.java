// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.context.internal;

import org.terasology.engine.context.Context;
import org.terasology.gestalt.di.ServiceRegistry;


/**
 * The intent of this class is purely to act as a forwarder to the underlying {@link org.terasology.gestalt.di.BeanContext}.
 */
public class ImmutableContextImpl extends ContextImpl {
    public ImmutableContextImpl(Context parent, ServiceRegistry... registries) {
        super(parent, registries);
    }

    public ImmutableContextImpl(ContextImpl parent, ServiceRegistry... registries) {
        super(parent, registries);
    }

    public ImmutableContextImpl(ServiceRegistry... registries) {
        super(registries);
    }

    public ImmutableContextImpl(Context parent) {
        super(parent);
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object) {
        throw new UnsupportedOperationException("The current context is immutable.");
    }
}
