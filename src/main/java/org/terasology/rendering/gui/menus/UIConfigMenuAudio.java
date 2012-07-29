package org.terasology.rendering.gui.menus;

import org.terasology.asset.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UISlider;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.IChangedListener;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIConfigMenuAudio extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;
    final UIText _version;
    
    private final UISlider _soundOptionSlider;
    private final UISlider _musicOptionSlider;
    private final UIButton _backToConfigMenuButton;

    public UIConfigMenuAudio() {
        maximize();

        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Audio Settings");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _soundOptionSlider = new UISlider(new Vector2f(256f, 32f), 0, 100);
        _soundOptionSlider.addChangedListener(new IChangedListener() {
			@Override
			public void changed(UIDisplayElement element) {
				UISlider slider = (UISlider)element;
				if (slider.getValue() > 0)
					slider.setText("Sound Volume: " + String.valueOf(slider.getValue()));
				else
					slider.setText("Sound Volume: Off");
				
				Config.getInstance().setSoundVolume(slider.getValue());
			}
		});
        _soundOptionSlider.setVisible(true);

        _musicOptionSlider = new UISlider(new Vector2f(256f, 32f), 0, 100);
        _musicOptionSlider.addChangedListener(new IChangedListener() {
			@Override
			public void changed(UIDisplayElement element) {
				UISlider slider = (UISlider)element;
				if (slider.getValue() > 0)
					slider.setText("Music Volume: " + String.valueOf(slider.getValue()));
				else
					slider.setText("Music Volume: Off");
				
				Config.getInstance().setMusicVolume(slider.getValue());
			}
		});
        _musicOptionSlider.setVisible(true);

        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_soundOptionSlider, "soundVolumeSlider");
        addDisplayElement(_musicOptionSlider, "musicVolumeSlider");
        addDisplayElement(_backToConfigMenuButton, "backToConfigMenuButton");
        update();
    }

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _soundOptionSlider.centerHorizontally();
        _soundOptionSlider.getPosition().y = 300f;

        _musicOptionSlider.centerHorizontally();
        _musicOptionSlider.getPosition().y = 300f + 40f;

        _backToConfigMenuButton.centerHorizontally();
        _backToConfigMenuButton.getPosition().y = 300f + 7 * 40f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
    }
}
