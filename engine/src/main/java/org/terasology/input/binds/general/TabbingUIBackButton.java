package org.terasology.input.binds.general;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.RegisterBindButton;

/**
 * Enables tabbing between widgets in reverse
 */
@RegisterBindButton(id = "tabbingUIBack", description = "${engine:menu#binding-tabbing-ui-back}", category = "general")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.LEFT_SHIFT)
public class TabbingUIBackButton extends BindButtonEvent {
}
