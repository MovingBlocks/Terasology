// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.device;

import org.terasology.gestalt.module.sandbox.API;
import org.terasology.nui.input.device.InputDevice;
import org.terasology.nui.input.device.KeyboardAction;

import java.util.Queue;

/**
 *
 */
@API
public interface KeyboardDevice extends InputDevice {

    @Override
    Queue<KeyboardAction> getInputQueue();

    /**
     * @param key
     * @return The current state of the given key
     */
    boolean isKeyDown(int key);
}
