// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.binds.general;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

/**
 */
@RegisterBindButton(id = "hideHUD", description = "${engine:menu#binding-hide-hud}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.H)
public class HideHUDButton extends BindButtonEvent {
}
