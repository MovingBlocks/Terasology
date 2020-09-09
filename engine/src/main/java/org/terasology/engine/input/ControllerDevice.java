// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.nui.input.device.ControllerAction;
import org.terasology.nui.input.device.InputDevice;

import java.util.List;
import java.util.Queue;

/**
 * Represents <b>all</b> connected controllers (e.g. gamepads, but also some keyboards). Unfortunately, it is impossible
 * to separate events in LWJGL2 based on controllers, so they have to be processes all at once.
 */
public interface ControllerDevice extends InputDevice {

    @Override
    Queue<ControllerAction> getInputQueue();

    /**
     * @return a list of currently connected controller IDs.
     */
    List<String> getControllers();
}
