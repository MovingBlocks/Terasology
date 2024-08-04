// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.terasology.context.annotation.API;

/**
 * A general interface for observers
 * @param <T> the target object type
 */
@API
@FunctionalInterface
public interface Observer<T> {

    void update(T layer);
}
