// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.nui;

import org.lwjgl.input.Keyboard;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;

/**
 *
 */
@RegisterBindButton(id = "behavior_editor", description = "${engine:menu#binding-behavior-editor}", category = 
        "behavior")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_F5)
public class BTEditorButton extends BindButtonEvent {
}
