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
 *         Date: 1/07/12
 */
public class UIVideoOptions extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIButton _graphicsQualityButton,
            _videoToSettingsMenuButton,
            _viewingDistanceButton,
            _fovButton,
            _animateGrassButton,
            _reflectiveWaterButton,
            _blurIntensityButton,
            _bobbingButton;

    final UIText _version;

    public UIVideoOptions() {
        maximize();
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Graphical options");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _graphicsQualityButton = new UIButton(new Vector2f(256f, 32f));
        _graphicsQualityButton.getLabel().setText("Graphics Quality: Ugly");
        _graphicsQualityButton.setVisible(true);

        _viewingDistanceButton = new UIButton(new Vector2f(256f, 32f));
        _viewingDistanceButton.getLabel().setText("Viewing Distance: Near");
        _viewingDistanceButton.setVisible(true);

        // TODO: Replace with a slider later on
        _fovButton = new UIButton(new Vector2f(256f, 32f));
        _fovButton.getLabel().setText("Field of View: 80");
        _fovButton.setVisible(true);

        _videoToSettingsMenuButton = new UIButton(new Vector2f(256f, 32f));
        _videoToSettingsMenuButton.getLabel().setText("Return to Main Menu");
        _videoToSettingsMenuButton.setVisible(true);

        _animateGrassButton = new UIButton(new Vector2f(256f, 32f));
        _animateGrassButton.getLabel().setText("Animated grass: false");
        _animateGrassButton.setVisible(true);

        _reflectiveWaterButton = new UIButton(new Vector2f(256f, 32f));
        _reflectiveWaterButton.getLabel().setText("Reflective water: false");
        _reflectiveWaterButton.setVisible(true);

        _blurIntensityButton = new UIButton(new Vector2f(256f, 32f));
        _blurIntensityButton.getLabel().setText("Blur intensity: Normal");
        _blurIntensityButton.setVisible(true);

        _bobbingButton = new UIButton(new Vector2f(256f, 32f));
        _bobbingButton.getLabel().setText("Bobbing: true");
        _bobbingButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_graphicsQualityButton, "graphicsQualityButton");
        addDisplayElement(_fovButton, "fovButton");
        addDisplayElement(_videoToSettingsMenuButton, "backToSettingsMenuButton");
        addDisplayElement(_viewingDistanceButton, "viewingDistanceButton");
        addDisplayElement(_animateGrassButton, "animateGrassButton");
        addDisplayElement(_reflectiveWaterButton, "reflectiveWaterButton");
        addDisplayElement(_blurIntensityButton, "blurIntensityButton");
        addDisplayElement(_bobbingButton, "bobbingButton");

        update();
    }

    // blur =  off, little bit, medium and blind ferret

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _graphicsQualityButton.getPosition().x = 40;
        _graphicsQualityButton.getPosition().y = 300f;

        _viewingDistanceButton.getPosition().x = 40;
        _viewingDistanceButton.getPosition().y = 300f + 40f;

        _fovButton.getPosition().x = 40;
        _fovButton.getPosition().y = 300f + 2 * 40f;

        _animateGrassButton.getPosition().x = 40 + 270;
        _animateGrassButton.getPosition().y = 300f;

        _reflectiveWaterButton.getPosition().x = 40 + 270;
        _reflectiveWaterButton.getPosition().y = 300f + 40f;

        _blurIntensityButton.getPosition().x = 40 + 270;
        _blurIntensityButton.getPosition().y = 300f + 2 * 40f;

        _bobbingButton.getPosition().x = 40 + 2 * 270;
        _bobbingButton.getPosition().y = 300f;

        _videoToSettingsMenuButton.centerHorizontally();
        _videoToSettingsMenuButton.getPosition().y = 300f + 7 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
