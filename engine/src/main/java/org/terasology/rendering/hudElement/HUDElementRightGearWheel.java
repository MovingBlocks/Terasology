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
public class HUDElementRightGearWheel extends UIDisplayContainer implements HUDElement {
    private UIImage rightGearWheel;
    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public HUDElementRightGearWheel() {
        setId("rightGearWheel");
    }

    @Override
    public void update() {
        super.update();

        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        if (character == null) {
            rightGearWheel.setVisible(false);
        } else {
            rightGearWheel.setVisible(true);
        }
    }

    @Override
    public void initialise() {
        rightGearWheel = new UIImage(Assets.getTexture("engine:inventory"));
        rightGearWheel.setSize(new Vector2f(36f, 36f));
        rightGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        rightGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        rightGearWheel.setId("rightGearWheel");
        rightGearWheel.setVisible(true);

        rightGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
        rightGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
        rightGearWheel.setPosition(new Vector2f(
                rightGearWheel.getPosition().x + 240f,
                rightGearWheel.getPosition().y - 4f)
        );

        addDisplayElement(rightGearWheel);

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
