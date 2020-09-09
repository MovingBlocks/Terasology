// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.movement;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard;

/**
 *
 */
@RegisterBindButton(id = "toggleSpeedTemporarily", description = "${engine:menu#binding-toggle-speed-temporarily}")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.LEFT_SHIFT)
public class ToggleSpeedTemporarilyButton extends BindButtonEvent {
}
