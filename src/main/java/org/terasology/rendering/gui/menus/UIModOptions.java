package org.terasology.rendering.gui.menus;

import org.terasology.logic.manager.AssetManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 *         Date: 1/07/12
 */
public class UIModOptions extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _minionsButton,
            _minionOptionsButton,
            _backToSettingsMenuButton;

    public UIModOptions() {
        maximize();
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _minionsButton = new UIButton(new Vector2f(256f, 32f));
        _minionsButton.getLabel().setText("Minions enabled : false");
        _minionsButton.setVisible(true);

        _minionOptionsButton = new UIButton(new Vector2f(256f, 32f));
        _minionOptionsButton.getLabel().setText("Minion Options...");
        _minionOptionsButton.setVisible(true);

        _backToSettingsMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToSettingsMenuButton.getLabel().setText("Return to Settings Menu");
        _backToSettingsMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);

        addDisplayElement(_minionsButton, "minionsButton");
        addDisplayElement(_minionOptionsButton, "minionOptionsButton");
        addDisplayElement(_backToSettingsMenuButton, "backToSettingsMenuButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _minionsButton.getPosition().x = 300f;
        _minionsButton.getPosition().y = 300f;

        _minionOptionsButton.getPosition().x = 300f + 270f;
        _minionOptionsButton.getPosition().y = 300f;

        _backToSettingsMenuButton.centerHorizontally();
        _backToSettingsMenuButton.getPosition().y = 300f + 6 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
