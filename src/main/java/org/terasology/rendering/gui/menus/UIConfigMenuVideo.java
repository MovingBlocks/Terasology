package org.terasology.rendering.gui.menus;

import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UISlider;
import org.terasology.rendering.gui.components.UIStateButton;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.IChangedListener;
import org.terasology.rendering.gui.framework.IClickListener;
import org.terasology.rendering.gui.framework.IStateButtonAction;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIConfigMenuVideo extends UIDisplayWindow {

    final UIImageOverlay _overlay;
    final UIGraphicsElement _title;

    private final UIStateButton _graphicsQualityButton;
    private final UIStateButton _viewingDistanceButton;
    private final UISlider _fovButton;
    private final UIStateButton _animateGrassButton;
    private final UIStateButton _reflectiveWaterButton;
    private final UIStateButton _blurIntensityButton;
    private final UIStateButton _bobbingButton;
    private final UIButton _backToConfigMenuButton;

    final UIText _version;
    
    private final IClickListener clickAction = new IClickListener() {
        @Override
        public void clicked(UIDisplayElement element) {
        	UIStateButton button = (UIStateButton) element;
            button.nextState();
        }
    };

    public UIConfigMenuVideo() {
        maximize();
        _title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        _title.setVisible(true);
        _title.setSize(new Vector2f(512f, 128f));

        _version = new UIText("Video Settings");
        _version.setVisible(true);

        _overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        _overlay.setVisible(true);

        _graphicsQualityButton = new UIStateButton(new Vector2f(256f, 32f));
        IStateButtonAction graphicsQualityStateAction = new IStateButtonAction() {
			@Override
			public void action(UIDisplayElement element) {
				UIStateButton button = (UIStateButton)element;
				switch (button.getState()) {
				case 0:
					Config.getInstance().setEnablePostProcessingEffects(false);
					Config.getInstance().setFlickeringLight(false);
					break;
				case 1:
					Config.getInstance().setEnablePostProcessingEffects(false);
					Config.getInstance().setFlickeringLight(true);
					break;
				case 2:
					Config.getInstance().setEnablePostProcessingEffects(true);
					Config.getInstance().setFlickeringLight(true);
					break;
				}
				
		        ShaderManager.getInstance().recompileAllShaders();
			}
		};
		_graphicsQualityButton.addState("Graphics Quality: Ugly", graphicsQualityStateAction);
        _graphicsQualityButton.addState("Graphics Quality: Nice", graphicsQualityStateAction);
        _graphicsQualityButton.addState("Graphics Quality: Epic", graphicsQualityStateAction);
        _graphicsQualityButton.addClickListener(clickAction);
        _graphicsQualityButton.setVisible(true);

        _viewingDistanceButton = new UIStateButton(new Vector2f(256f, 32f));
        IStateButtonAction viewingDistanceStateAction = new IStateButtonAction() {
			@Override
			public void action(UIDisplayElement element) {
				UIStateButton button = (UIStateButton)element;
				Config.getInstance().setViewingDistanceById(button.getState());
			}
		};
        _viewingDistanceButton.addState("Viewing Distance: Near", viewingDistanceStateAction);
		_viewingDistanceButton.addState("Viewing Distance: Moderate", viewingDistanceStateAction);
        _viewingDistanceButton.addState("Viewing Distance: Far", viewingDistanceStateAction);
        _viewingDistanceButton.addState("Viewing Distance: Ultra", viewingDistanceStateAction);
        _viewingDistanceButton.addClickListener(clickAction);
        _viewingDistanceButton.setVisible(true);

        _fovButton = new UISlider(new Vector2f(256f, 32f), 75, 120);
        _fovButton.addChangedListener(new IChangedListener() {
			@Override
			public void changed(UIDisplayElement element) {
				UISlider slider = (UISlider)element;
				slider.setText("FOV: " + String.valueOf(slider.getValue()));
				Config.getInstance().setFov(slider.getValue());
			}
		});
        _fovButton.setVisible(true);

        _animateGrassButton = new UIStateButton(new Vector2f(256f, 32f));
        IStateButtonAction animateGrassStateAction = new IStateButtonAction() {
			@Override
			public void action(UIDisplayElement element) {
				UIStateButton button = (UIStateButton)element;
				if (button.getState() == 0)
					Config.getInstance().setAnimatedGrass(false);
				else
					Config.getInstance().setAnimatedGrass(true);
			}
		};
		_animateGrassButton.addState("Animate Grass: Off", animateGrassStateAction);
        _animateGrassButton.addState("Animate Grass: On", animateGrassStateAction);
        _animateGrassButton.addClickListener(clickAction);
        _animateGrassButton.setVisible(true);

        _reflectiveWaterButton = new UIStateButton(new Vector2f(256f, 32f));
        IStateButtonAction reflectiveWaterStateAction = new IStateButtonAction() {
			@Override
			public void action(UIDisplayElement element) {
				UIStateButton button = (UIStateButton)element;
				if (button.getState() == 0)
					Config.getInstance().setComplexWater(false);
				else
					Config.getInstance().setComplexWater(true);
			}
		};
        _reflectiveWaterButton.addState("Reflective water: Off", reflectiveWaterStateAction);
		_reflectiveWaterButton.addState("Reflective water: On", reflectiveWaterStateAction);
        _reflectiveWaterButton.addClickListener(clickAction);
        _reflectiveWaterButton.setVisible(true);

        _blurIntensityButton = new UIStateButton(new Vector2f(256f, 32f));
        IStateButtonAction blurIntensityStateAction = new IStateButtonAction() {
			@Override
			public void action(UIDisplayElement element) {
				UIStateButton button = (UIStateButton)element;
				Config.getInstance().setBlurIntensity(button.getState());
			}
		};
		_blurIntensityButton.addState("Blur intensity: Off", blurIntensityStateAction);
        _blurIntensityButton.addState("Blur intensity: Some", blurIntensityStateAction);
        _blurIntensityButton.addState("Blur intensity: Normal", blurIntensityStateAction);
        _blurIntensityButton.addState("Blur intensity: Max",blurIntensityStateAction);
        _blurIntensityButton.addClickListener(clickAction);
        _blurIntensityButton.setVisible(true);

        _bobbingButton = new UIStateButton(new Vector2f(256f, 32f));
        IStateButtonAction bobbingStateAction = new IStateButtonAction() {
			@Override
			public void action(UIDisplayElement element) {
				UIStateButton button = (UIStateButton)element;
				if (button.getState() == 0)
					Config.getInstance().setCameraBobbing(false);
				else
					Config.getInstance().setCameraBobbing(true);
			}
		};
		_bobbingButton.addState("Bobbing: Off", bobbingStateAction);
        _bobbingButton.addState("Bobbing: On", bobbingStateAction);
        _bobbingButton.addClickListener(clickAction);
        _bobbingButton.setVisible(true);
        
        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setVisible(true);

        addDisplayElement(_overlay);
        addDisplayElement(_title);
        addDisplayElement(_version);

        addDisplayElement(_graphicsQualityButton, "graphicsQualityButton");
        addDisplayElement(_fovButton, "fovSlider");
        addDisplayElement(_viewingDistanceButton, "viewingDistanceButton");
        addDisplayElement(_animateGrassButton, "animateGrassButton");
        addDisplayElement(_reflectiveWaterButton, "reflectiveWaterButton");
        addDisplayElement(_blurIntensityButton, "blurIntensityButton");
        addDisplayElement(_bobbingButton, "bobbingButton");
        addDisplayElement(_backToConfigMenuButton, "backToConfigMenuButton");

        update();
    }

    // blur =  off, little bit, medium and blind ferret

    @Override
    public void update() {
        super.update();

        _version.centerHorizontally();
        _version.getPosition().y = 230f;

        _title.centerHorizontally();
        _title.getPosition().y = 128f;
        
        //row 1
        _graphicsQualityButton.getPosition().x = Display.getWidth() / 2 - _graphicsQualityButton.getSize().x - 10;
        _graphicsQualityButton.getPosition().y = 300f;

        _viewingDistanceButton.getPosition().x = Display.getWidth() / 2 - _viewingDistanceButton.getSize().x - 10;
        _viewingDistanceButton.getPosition().y = 300f + 40f;

        _fovButton.getPosition().x = Display.getWidth() / 2 - _fovButton.getSize().x - 10;
        _fovButton.getPosition().y = 300f + 2 * 40f;
        
        _bobbingButton.getPosition().x = Display.getWidth() / 2 - _bobbingButton.getSize().x - 10;
        _bobbingButton.getPosition().y = 300f + 3 * 40f;

        //row 2
        _animateGrassButton.getPosition().x = Display.getWidth() / 2 + 10;
        _animateGrassButton.getPosition().y = 300f;

        _reflectiveWaterButton.getPosition().x = Display.getWidth() / 2 + 10;
        _reflectiveWaterButton.getPosition().y = 300f + 40f;

        _blurIntensityButton.getPosition().x = Display.getWidth() / 2 + 10;
        _blurIntensityButton.getPosition().y = 300f + 2 * 40f;

        //back
        _backToConfigMenuButton.centerHorizontally();
        _backToConfigMenuButton.getPosition().y = 300f + 7 * 40f;
    }
}
