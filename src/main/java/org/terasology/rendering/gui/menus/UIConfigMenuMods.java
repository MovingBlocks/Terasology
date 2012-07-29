package org.terasology.rendering.gui.menus;

import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIConfigMenuMods extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _minionsButton,
            _minionOptionsButton,
            _backToConfigMenuButton;

    public UIConfigMenuMods() {
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

        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);

        addDisplayElement(_minionsButton, "minionsButton");
        addDisplayElement(_minionOptionsButton, "minionOptionsButton");
        addDisplayElement(_backToConfigMenuButton, "backToConfigMenuButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _minionsButton.centerHorizontally();
        _minionsButton.getPosition().y = 300f;

        _minionOptionsButton.centerHorizontally();
        _minionOptionsButton.getPosition().y = 300f + 40f;

        _backToConfigMenuButton.centerHorizontally();
        _backToConfigMenuButton.getPosition().y = 300f + 7 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
