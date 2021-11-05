// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.general;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "console", description = "${engine:menu#binding-console}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.GRAVE)
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.F1)
public class ConsoleButton extends BindButtonEvent {
}
