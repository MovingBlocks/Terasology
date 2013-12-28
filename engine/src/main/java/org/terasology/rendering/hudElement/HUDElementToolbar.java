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
import javax.vecmath.Vector4f;

import org.terasology.engine.CoreRegistry;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.rendering.logic.manager.HUDElement;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class HUDElementToolbar extends UIDisplayContainer implements HUDElement {
    private UIInventoryGrid toolbar;

    private LocalPlayer localPlayer;

    /**
     * Init. the HUD.
     */
    public HUDElementToolbar() {
        setId("toolbar");
    }

    @Override
    public void update() {
        super.update();

        CharacterComponent character = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
        if (character == null) {
            toolbar.setVisible(false);
        } else {
            toolbar.setVisible(true);
            toolbar.linkToEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity(), 0, 10);
            toolbar.setSelected(character.selectedItem);
        }

    }

    @Override
    public void initialise() {

        toolbar = new UIInventoryGrid(10);
        toolbar.setId("toolbar");
        toolbar.setVisible(true);
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.BOTTOM);

        toolbar.setVisible(true);
        toolbar.setCellMargin(new Vector2f(0f, 0f));
        toolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        toolbar.linkToEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity(), 0, 10);
        CharacterComponent character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CharacterComponent.class);
        if (character != null) {
            toolbar.setSelected(character.selectedItem);
        }

        addDisplayElement(toolbar);

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
