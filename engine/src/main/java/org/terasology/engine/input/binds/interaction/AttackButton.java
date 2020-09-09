// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.interaction;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.ControllerId;
import org.terasology.nui.input.InputType;

/**
 *
 */
@RegisterBindButton(id = "attack", description = "${engine:menu#binding-attack}", repeating = true)
@DefaultBinding(type = InputType.MOUSE_BUTTON, id = 0)
@DefaultBinding(type = InputType.CONTROLLER_BUTTON, id = ControllerId.ZERO)
public class AttackButton extends BindButtonEvent {
}
