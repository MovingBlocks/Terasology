// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.movement;

import org.terasology.engine.input.BindAxisEvent;
import org.terasology.engine.input.RegisterBindAxis;
import org.terasology.engine.input.SendEventMode;

@RegisterBindAxis(id = "verticalMovement", positiveButton = "engine:jump", negativeButton = "engine:crouch", eventMode = SendEventMode.WHEN_CHANGED)
public class VerticalMovementAxis extends BindAxisEvent {
}
