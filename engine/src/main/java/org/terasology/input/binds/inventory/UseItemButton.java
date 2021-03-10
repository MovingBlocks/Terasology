// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.inventory;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.ControllerId;
import org.terasology.module.sandbox.API;

/**
 */
@RegisterBindButton(id = "useItem", description = "${engine:menu#binding-use-item}", repeating = true, category = "interaction")
@DefaultBinding(type = InputType.MOUSE_BUTTON, id = 1)
@DefaultBinding(type = InputType.CONTROLLER_BUTTON, id = ControllerId.THREE)
@API
public class UseItemButton extends BindButtonEvent {
}
