package org.terasology.rendering.gui.dialogs;

import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIInput;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

import javax.vecmath.Vector2f;

public class UIDialogCreateNewWorld extends UIDisplayWindow{
    private UIButton _okButton;
    private UIButton _cancelButton;
    private UIInput  _inputSeed;
    private UIInput  _inputWorldName;

    public UIDialogCreateNewWorld(String title, Vector2f size){
        super(title, size);
        _okButton     = new UIButton(new Vector2f(256f, 32f));
        _okButton.getLabel().setText("Play");
        _cancelButton = new UIButton(new Vector2f(256f, 32f));
        _okButton.getLabel().setText("Cancel");
    }
}
