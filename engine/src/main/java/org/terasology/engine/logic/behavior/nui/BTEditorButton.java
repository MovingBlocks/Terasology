// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

/**
 */
@RegisterBindButton(id = "behavior_editor", description = "${engine:menu#binding-behavior-editor}", category = "behavior")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.F5)
public class BTEditorButton extends BindButtonEvent {
}
