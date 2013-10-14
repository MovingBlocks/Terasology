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
import org.terasology.math.Rect2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.LwjglCanvas;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;

/**
 * @author Immortius
 */
public class UINUITest extends UIWindow {

    private LwjglCanvas canvas;
    private Font font = Assets.getFont("engine:default");

    public UINUITest() {
        setId("nuitest");
        setModal(true);
        maximize();
        canvas = new LwjglCanvas();
    }

    @Override
    public void render() {
        canvas.preRender();

        canvas.drawTexture(Assets.getTexture("engine:containerWindow"), Rect2i.createFromMinAndSize(0, 0, 400, 400), ScaleMode.STRETCH);

        canvas.drawText(font, "Some Text");
        canvas.setOffset(15, 100);
        canvas.drawTextShadowed(font, "Shadowed Text", Color.BLACK);
        try (SubRegion ignored = canvas.subRegion(Rect2i.createFromMinAndMax(0, 300, 100, 500), true)) {
            canvas.drawText(font, "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n\nLorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", 300);
        }
        canvas.setOffset(0, 150);
        canvas.drawText(font, "Some Text");

        canvas.postRender();

    }
}
