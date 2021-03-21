// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.movement;

import org.terasology.engine.input.BindAxisEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterRealBindAxis;
import org.terasology.engine.input.SendEventMode;
import org.terasology.input.ControllerId;
import org.terasology.input.InputType;

/**
 * Relates to the vertical screen axis, i.e. look up/down.
 */
@RegisterRealBindAxis(id = "rotationPitch", eventMode = SendEventMode.WHEN_NON_ZERO)
@DefaultBinding(type = InputType.CONTROLLER_AXIS, id = ControllerId.RY_AXIS)
public class RotationPitchAxis extends BindAxisEvent {
}
