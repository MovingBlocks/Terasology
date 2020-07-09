// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.input.device.nulldevices;

import com.google.common.collect.Queues;
import org.terasology.input.device.CharKeyboardAction;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.RawKeyboardAction;

import java.util.Queue;

public class NullKeyboardDevice implements KeyboardDevice {

    @Override
    public boolean isKeyDown(int button) {
        return false;
    }

    @Override
    public Queue<RawKeyboardAction> getInputQueue() {
        return Queues.newArrayDeque();
    }

    @Override
    public Queue<CharKeyboardAction> getCharInputQueue() {
        return Queues.newArrayDeque();
    }
}
