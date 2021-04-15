// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.editor.binds;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.engine.input.RegisterBindButton;

/**
 * Button that opens the NUI skin editor, or closes it if it's active.
 */
@RegisterBindButton(id = "nuiSkinEditor", description = "${engine:menu#binding-nui-skin-editor}", category = "nui")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.F11)
public class NUISkinEditorButton extends BindButtonEvent {
}
