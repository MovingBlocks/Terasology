// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.systems;

/**
 * Abstract class implementing the {@link org.terasology.engine.entitySystem.systems.ComponentSystem} interface.
 * All interface method implementations are empty/NO-OP.
 *
 */
public abstract class BaseComponentSystem implements ComponentSystem {

    @Override
    public void initialise() {
    }

    @Override
    public void preBegin() {
    }

    @Override
    public void postBegin() {
    }

    @Override
    public void preSave() {
    }

    @Override
    public void postSave() {
    }

    @Override
    public void shutdown() {
    }
}
