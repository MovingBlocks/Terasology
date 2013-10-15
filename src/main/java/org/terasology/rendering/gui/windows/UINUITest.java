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
import org.terasology.rendering.nui.HorizontalAlignment;
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

        canvas.drawTexture(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(0, 0, 128, 128), ScaleMode.STRETCH);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(12, 12, 104, 104), ScaleMode.STRETCH);
        canvas.setOffset(15, 100);
        canvas.drawTextShadowed(font, "Stretched", Color.BLACK);

        canvas.drawTexture(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(128, 0, 128, 128), ScaleMode.STRETCH);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(140, 12, 104, 104), ScaleMode.SCALE_FIT);
        canvas.setOffset(143, 75);
        canvas.drawTextShadowed(font, "Scaled Fit", Color.BLACK);

        canvas.drawTexture(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(256, 0, 128, 128), ScaleMode.STRETCH);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(268, 12, 104, 104), ScaleMode.SCALE_FILL);
        canvas.setOffset(270, 100);
        canvas.drawTextShadowed(font, "Scaled Fill", Color.BLACK);

        canvas.setOffset(0, 150);
        canvas.setTextColor(Color.GREY);
        canvas.drawText(font, "Some Text");
        canvas.drawText(font, "Some More Text");
        canvas.drawText(font, "A little to the right", canvas.size().x, HorizontalAlignment.RIGHT);
        canvas.drawText(font, "Smack in the middle", canvas.size().x, HorizontalAlignment.CENTER);

        canvas.postRender();

    }
}
