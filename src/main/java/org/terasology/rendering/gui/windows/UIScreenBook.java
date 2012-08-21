/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.terasology.asset.AssetManager;
import org.terasology.input.binds.UseItemButton;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

public class UIScreenBook extends UIDisplayWindow {

    private final UIGraphicsElement background;

    public UIScreenBook() {
        setBackgroundColor(0x00, 0x00, 0x00, 0.75f);
        setModal(true);
        setCloseBinds(new String[] {UseItemButton.ID});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        maximize();
        
        background = new UIGraphicsElement(AssetManager.loadTexture("engine:openbook"));
        background.setPosition(new Vector2f(-250, -200));
        background.setSize(new Vector2f(500, 300));
        background.setVisible(true);
        
        addDisplayElement(background);
        
        layout();
    }

    @Override
    public void layout() {
        super.layout();
        
        if (background != null) {
            background.center();
        }
    }
}
