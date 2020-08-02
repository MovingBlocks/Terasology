// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.device;

import org.terasology.module.sandbox.API;

import java.util.Queue;

@API
public interface KeyboardDevice extends InputDevice {

    @Override
    Queue<RawKeyboardAction> getInputQueue();

    Queue<CharKeyboardAction> getCharInputQueue();

    /**
     * @param key
     * @return The current state of the given key
     */
    boolean isKeyDown(int key);
}
