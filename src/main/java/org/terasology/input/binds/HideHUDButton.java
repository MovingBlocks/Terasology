package org.terasology.input.binds;

import org.lwjgl.input.Keyboard;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.DefaultBinding;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;

/**
 * @author Immortius
 */
@RegisterBindButton(id = "hideHUD", description = "Hide HDU")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_H)
public class HideHUDButton extends BindButtonEvent {
}
