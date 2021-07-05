// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.input.ActivateMode;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

@RegisterBindButton(id = "decreaseViewDistance", description = "${engine:menu#binding-decrease-view-distance}",
        mode = ActivateMode.PRESS, category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.END)
public class DecreaseViewDistanceButton extends BindButtonEvent {
}
