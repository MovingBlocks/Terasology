/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.windows;

import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.logic.drowning.DrowningComponent;
import org.terasology.logic.drowning.DrownsComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 *         <p/>
 *         TODO clean up -> remove debug stuff, move to debug window together with metrics
 */
public class UIScreenHUD extends UIWindow implements ComponentSystem {

    protected EntityManager entityManager;
    private Time time;

    /* DISPLAY ELEMENTS */
    private final UIImage crosshair;

    private final Config config = CoreRegistry.get(Config.class);

    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public UIScreenHUD() {
        setId("hud");
        maximize();
        time = CoreRegistry.get(Time.class);

        crosshair = new UIImage(Assets.getTexture("engine:gui"));
        crosshair.setId("crosshair");
        crosshair.setTextureSize(new Vector2f(20f, 20f));
        crosshair.setTextureOrigin(new Vector2f(24f, 24f));
        crosshair.setSize(new Vector2f(40f, 40f));
        crosshair.setHorizontalAlign(EHorizontalAlign.CENTER);
        crosshair.setVerticalAlign(EVerticalAlign.CENTER);
        crosshair.setVisible(true);

        addDisplayElement(crosshair);

        localPlayer = CoreRegistry.get(LocalPlayer.class);

        update();
        layout();
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {

    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {

    }

}
