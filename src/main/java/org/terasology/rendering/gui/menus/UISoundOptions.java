package org.terasology.rendering.gui.menus;

import org.terasology.asset.AssetManager;
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
public class UISoundOptions extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _soundToSettingsMenuButton,
            _soundOptionButton,
            _musicOptionButton;

    final UIText _version;

    public UISoundOptions() {
        maximize();

        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("audio options");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _soundOptionButton = new UIButton(new Vector2f(256f, 32f));
        _soundOptionButton.getLabel().setText("Sound: On");
        _soundOptionButton.setVisible(true);

        _musicOptionButton = new UIButton(new Vector2f(256f, 32f));
        _musicOptionButton.getLabel().setText("Music: On");
        _musicOptionButton.setVisible(true);

        _soundToSettingsMenuButton = new UIButton(new Vector2f(256f, 32f));
        _soundToSettingsMenuButton.getLabel().setText("Return to Settings Menu");
        _soundToSettingsMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_soundOptionButton, "soundOptionButton");
        addDisplayElement(_musicOptionButton, "musicOptionButton");
        addDisplayElement(_soundToSettingsMenuButton, "soundToSettingsMenuButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _soundOptionButton.centerHorizontally();
        _soundOptionButton.getPosition().y = 300f;

        _musicOptionButton.centerHorizontally();
        _musicOptionButton.getPosition().y = 300f + 2 * 40f;

        _soundToSettingsMenuButton.centerHorizontally();
        _soundToSettingsMenuButton.getPosition().y = 300f + 7 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
