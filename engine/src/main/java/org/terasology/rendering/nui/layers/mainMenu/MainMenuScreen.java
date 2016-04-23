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
import org.terasology.registry.In;
import org.terasology.rendering.animation.Animation;
import org.terasology.rendering.animation.HueInterpolator;
import org.terasology.rendering.animation.Interpolator;
import org.terasology.rendering.animation.RepeatMode;
import org.terasology.rendering.animation.TimeModifier;
import org.terasology.rendering.animation.TimeModifiers;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.version.TerasologyVersion;

/**
 */
public class MainMenuScreen extends CoreScreenLayer {

    @In
    private GameEngine engine;
    private Animation anim;
    private Animation anim2;

    @Override
    public void initialise() {

        UIImage title = find("title", UIImage.class);
        UILabel versionLabel = find("version", UILabel.class);

        Interpolator interpolator = new HueInterpolator(color -> title.setTint(color));
        Interpolator textInterpolator = v -> versionLabel.setText(String.format("Time: %.2f", v));

        TimeModifier timeMod = TimeModifiers
                .mirror()
                .andThen(TimeModifiers.smooth())
                .andThen(TimeModifiers.sub(0.2f, 0.6f));

        anim = new Animation(interpolator, 5.0f, RepeatMode.REPEAT_INFINITE, timeMod);
        anim.start();

        anim2 = new Animation(textInterpolator, 5.0f, RepeatMode.REPEAT_INFINITE, timeMod);
        anim2.start();

        versionLabel.setText(TerasologyVersion.getInstance().getHumanVersion());
        WidgetUtil.trySubscribe(this, "singleplayer", button -> {
            getManager().pushScreen("engine:selectGameScreen", SelectGameScreen.class).setLoadingAsServer(false);
        });
        WidgetUtil.trySubscribe(this, "multiplayer", button -> {
            getManager().pushScreen("engine:selectGameScreen", SelectGameScreen.class).setLoadingAsServer(true);
        });
        WidgetUtil.trySubscribe(this, "join", button -> getManager().pushScreen("engine:joinGameScreen"));
        WidgetUtil.trySubscribe(this, "settings", button -> getManager().pushScreen("engine:settingsMenuScreen"));
        WidgetUtil.trySubscribe(this, "exit", button -> engine.shutdown());
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        anim.update(delta);
        anim2.update(delta);
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


