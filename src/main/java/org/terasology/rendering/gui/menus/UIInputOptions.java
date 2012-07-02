package org.terasology.rendering.gui.menus;

import org.terasology.logic.manager.AssetManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 *         Date: 2/07/12
 */
public class UIInputOptions extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _inputToSettingsMenuButton;

    final UIText _version;

    public UIInputOptions() {
        maximize();

        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Input options");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _inputToSettingsMenuButton = new UIButton(new Vector2f(256f, 32f));
        _inputToSettingsMenuButton.getLabel().setText("Return to Settings Menu");
        _inputToSettingsMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_inputToSettingsMenuButton, "inputToSettingsMenuButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _inputToSettingsMenuButton.centerHorizontally();
        _inputToSettingsMenuButton.getPosition().y = 300f + 7 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
