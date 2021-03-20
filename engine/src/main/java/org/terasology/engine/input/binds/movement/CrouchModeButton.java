// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.binds.movement;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

/**
 */
@RegisterBindButton(id = "crouchMode", description = "${engine:menu#binding-crouch-mode}")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.X)
public class CrouchModeButton extends BindButtonEvent {
}
