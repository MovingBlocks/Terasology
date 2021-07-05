// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.general;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

/**
 * Toggles the display of ChatScreen (through ChatSystem)
 */
@RegisterBindButton(id = "chat", description = "${engine:menu#binding-chat}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.T)
public class ChatButton extends BindButtonEvent {
}
