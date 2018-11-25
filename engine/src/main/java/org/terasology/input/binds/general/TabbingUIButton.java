package org.terasology.input.binds.general;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.RegisterBindButton;

/**
 * Enables tabbing between widgets.
 */
@RegisterBindButton(id = "tabbingUI", description = "${engine:menu#binding-tabbing-ui}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.RIGHT_SHIFT)
public class TabbingUIButton extends BindButtonEvent {
}
