// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.movement;

import org.terasology.engine.input.BindAxisEvent;
import org.terasology.input.ControllerId;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.engine.input.RegisterRealBindAxis;
import org.terasology.engine.input.SendEventMode;

/**
 * Relates to the horizontal screen axis, i.e. look left/right.
 */
@RegisterRealBindAxis(id = "rotationYaw", eventMode = SendEventMode.WHEN_NON_ZERO)
@DefaultBinding(type = InputType.CONTROLLER_AXIS, id = ControllerId.RX_AXIS)
public class RotationYawAxis extends BindAxisEvent {
}
