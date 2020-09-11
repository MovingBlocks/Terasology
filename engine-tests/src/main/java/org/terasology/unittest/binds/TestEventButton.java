// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.unittest.binds;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;

@RegisterBindButton(id = "testEvent", description = "${engine-tests:menu#theTestEvent}", repeating = false,
        category = "tests")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.UNLABELED)
public class TestEventButton extends BindButtonEvent {
}
