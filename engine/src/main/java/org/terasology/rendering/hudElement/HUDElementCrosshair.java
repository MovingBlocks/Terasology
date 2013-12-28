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
package org.terasology.rendering.hudElement;

import javax.vecmath.Vector2f;

import org.terasology.asset.Assets;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.logic.manager.HUDElement;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class HUDElementCrosshair extends UIDisplayContainer implements HUDElement {

    private UIImage crosshair;

    /**
     * Init. the HUD.
     */
    public HUDElementCrosshair() {
        setId("crosshair");
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void initialise() {
        crosshair = new UIImage(Assets.getTexture("engine:gui"));
        crosshair.setId("crosshair");
        crosshair.setTextureSize(new Vector2f(20f, 20f));
        crosshair.setTextureOrigin(new Vector2f(24f, 24f));
        crosshair.setSize(new Vector2f(40f, 40f));
        crosshair.setHorizontalAlign(EHorizontalAlign.CENTER);
        crosshair.setVerticalAlign(EVerticalAlign.CENTER);
        crosshair.setVisible(true);

        addDisplayElement(crosshair);
    }

	@Override
	public void open() {
	}

	@Override
	public void willShutdown() {
	}

    @Override
    public void shutdown() {

    }

}
