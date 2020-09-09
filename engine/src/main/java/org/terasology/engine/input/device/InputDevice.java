// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.device;

import org.terasology.gestalt.module.sandbox.API;

import java.util.Queue;

@API
@FunctionalInterface
public interface InputDevice {

    /**
     * @return A queue of all input actions that have occurred over the last update for this device
     */
    Queue<?> getInputQueue();
}
