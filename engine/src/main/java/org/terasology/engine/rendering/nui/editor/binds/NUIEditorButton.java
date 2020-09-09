// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.binds;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

/**
 * Button that opens the NUI screen editor, or closes it if it's active.
 */
@RegisterBindButton(id = "nuiEditor", description = "${engine:menu#binding-nui-editor}", category = "nui")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.F10)
public class NUIEditorButton extends BindButtonEvent {
}
