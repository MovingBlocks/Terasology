// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.interaction;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.ControllerId;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

/**
 */
@RegisterBindButton(id = "frob", description = "${engine:menu#binding-frob}")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.E)
@DefaultBinding(type = InputType.CONTROLLER_BUTTON, id = ControllerId.FOUR)
public class FrobButton extends BindButtonEvent {
}
