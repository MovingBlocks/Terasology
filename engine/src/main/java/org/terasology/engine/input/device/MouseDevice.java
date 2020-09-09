// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.device;

import org.terasology.gestalt.module.sandbox.API;
import org.terasology.math.geom.Vector2i;
import org.terasology.nui.input.device.InputDevice;
import org.terasology.nui.input.device.MouseAction;

import java.util.Queue;

/**
 *
 */
@API
public interface MouseDevice extends InputDevice {

    @Override
    Queue<MouseAction> getInputQueue();

    /**
     * @return The current position of the mouse in screen space
     */
    Vector2i getPosition();

    /**
     * TODO: Remove when nui-input is fully integrated
     *
     * @return The current position of the mouse in screen space
     */
    org.joml.Vector2i getMousePosition();

    /**
     * @return The change in mouse position over the last update
     */
    Vector2i getDelta();

    /**
     * @param button
     * @return The current state of the given button
     */
    boolean isButtonDown(int button);

    /**
     * @return Whether the mouse cursor is visible
     */
    boolean isVisible();

    /**
     * Specifies if the mouse is grabbed and there is thus no mouse cursor that can get to a border.
     */
    void setGrabbed(boolean grabbed);
}
