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
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.logic.manager.HUDElement;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class HUDElementLeftGearWheel extends UIDisplayContainer implements HUDElement {

    private UIImage leftGearWheel;

    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public HUDElementLeftGearWheel() {
        setId("leftGearWheel");
    }

    @Override
    public void update() {
        super.update();

        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        if (character == null) {
            leftGearWheel.setVisible(false);
        } else {
            leftGearWheel.setVisible(true);
        }
    }

    @Override
    public void initialise() {

        leftGearWheel = new UIImage(Assets.getTexture("engine:inventory"));

        leftGearWheel.setSize(new Vector2f(36f, 36f));
        leftGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        leftGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        leftGearWheel.setId("leftGearWheel");
        leftGearWheel.setVisible(true);

        leftGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
        leftGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
        leftGearWheel.setPosition(new Vector2f(
                leftGearWheel.getPosition().x - 240f,
                leftGearWheel.getPosition().y - 4f)
        );

        addDisplayElement(leftGearWheel);

        localPlayer = CoreRegistry.get(LocalPlayer.class);
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
