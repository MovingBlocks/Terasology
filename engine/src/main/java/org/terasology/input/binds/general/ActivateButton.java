package org.terasology.input.binds.general;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.RegisterBindButton;

/**
 * Enables activation of selected widget.
 */
@RegisterBindButton(id = "activate", description = "${engine:menu#binding-activate}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.ENTER)
public class ActivateButton extends BindButtonEvent {
}
