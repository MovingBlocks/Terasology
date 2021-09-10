// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.general;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

/**
 * Modifies the behaviour of tabbing between widgets
 * Currently this reverses the tab order
 */
@RegisterBindButton(id = "tabbingModifier", description = "${engine:menu#binding-tabbing-modifier}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.LEFT_SHIFT)
public class TabbingModifierButton extends BindButtonEvent {
}
