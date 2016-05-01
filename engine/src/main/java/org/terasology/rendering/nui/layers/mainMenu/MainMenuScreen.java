/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.engine.GameEngine;
import org.terasology.math.geom.Rect2i;
import org.terasology.registry.In;
import org.terasology.rendering.animation.Animation;
import org.terasology.rendering.animation.AnimationListener;
import org.terasology.rendering.animation.Animator;
import org.terasology.rendering.animation.AnimatorGroup;
import org.terasology.rendering.animation.ColorHueAnimator;
import org.terasology.rendering.animation.TimeModifier;
import org.terasology.rendering.animation.TimeModifiers;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.MenuAnimationSystem;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.version.TerasologyVersion;

/**
 */
public class MainMenuScreen extends CoreScreenLayer {

    @In
    private GameEngine engine;
    private Animation colorAnimation;
    private MenuAnimationSystem switchAnimation = new MenuAnimationSystem();

    @Override
    public void initialise() {

        UIImage title = find("title", UIImage.class);
        UILabel versionLabel = find("version", UILabel.class);

        Animator colorAnim = new ColorHueAnimator(color -> title.setTint(color));
        Animator textAnim = v -> versionLabel.setText(String.format("Color Hue: %.2f", v));
        Animator animGroup = new AnimatorGroup(colorAnim, textAnim);

        TimeModifier colorTimeMod = TimeModifiers
                .mirror()
                .andThen(TimeModifiers.smooth())
                .andThen(TimeModifiers.sub(0.2f, 0.4f));

        colorAnimation = Animation.infinite(animGroup, 3.0f, colorTimeMod).start();

        versionLabel.setText(TerasologyVersion.getInstance().getHumanVersion());
        subscribeAnimated("singleplayer", button -> {
            getManager().pushScreen("engine:selectGameScreen", SelectGameScreen.class).setLoadingAsServer(false);
        });
        subscribeAnimated("multiplayer", button -> {
            getManager().pushScreen("engine:selectGameScreen", SelectGameScreen.class).setLoadingAsServer(true);
        });
        subscribeAnimated("join", button -> getManager().pushScreen("engine:joinGameScreen"));
        subscribeAnimated("settings", button -> getManager().pushScreen("engine:settingsMenuScreen"));
        WidgetUtil.trySubscribe(this, "exit", button -> engine.shutdown());
    }

    private void subscribeAnimated(String id, ActivateEventListener listener) {
        WidgetUtil.trySubscribe(this, id, new ActivateEventListener() {

            @Override
            public void onActivated(UIWidget widget) {
                switchAnimation.onEnd(() -> listener.onActivated(widget));
                switchAnimation.triggerEnd();
            }
        });
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect2i region = switchAnimation.getRenderRegion(canvas.getRegion());
        if (isModal()) {
            canvas.addInteractionRegion(getScreenListener(), region);
        }
        if (getContents() != null) {
            canvas.drawWidget(getContents(), region);
        }
    }

    @Override
    public void onShow() {
        switchAnimation.triggerStart();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        colorAnimation.update(delta);
        switchAnimation.update(delta);
    }

    @Override
    public boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}


