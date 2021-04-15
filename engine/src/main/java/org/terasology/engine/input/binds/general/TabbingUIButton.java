// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.binds.general;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

/**
 * Tabs between widgets in order.
 * Selects but does not activate the widget.
 */
@RegisterBindButton(id = "tabbingUI", description = "${engine:menu#binding-tabbing-ui}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.TAB)
public class TabbingUIButton extends BindButtonEvent {
}
