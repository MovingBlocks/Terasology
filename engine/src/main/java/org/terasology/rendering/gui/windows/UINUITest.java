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

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.input.InputSystem;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.HorizontalAlignment;
import org.terasology.rendering.nui.LwjglCanvas;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIStyle;
import org.terasology.rendering.nui.baseWidgets.UIButton;

import static org.lwjgl.opengl.Util.checkGLError;

/**
 * @author Immortius
 */
public class UINUITest extends UIWindow {

    private LwjglCanvas canvas;
    private Font font = Assets.getFont("engine:default");
    private InputSystem input = CoreRegistry.get(InputSystem.class);

    private Texture inactive = Assets.getTexture("engine:testWindowBorder");
    private Texture active = Assets.getTexture("engine:testWindowBorderOver");

    private UIStyle baseStyle;
    private UIStyle buttonStyle;
    private UIStyle buttonHoverStyle;
    private UIStyle buttonActiveStyle;

    private UIButton button;


    public UINUITest() {
        setId("nuitest");
        setModal(true);
        maximize();
        canvas = new LwjglCanvas();
        baseStyle = new UIStyle();
        buttonStyle = new UIStyle();
        buttonStyle.setBackground(Assets.getTexture("engine", "button"));
        buttonStyle.setTextAlignmentH(HorizontalAlignment.CENTER);
        buttonHoverStyle = new UIStyle(buttonStyle);
        buttonHoverStyle.setBackground(Assets.getTexture("engine", "buttonOver"));
        buttonActiveStyle = new UIStyle();
        buttonActiveStyle.setBackground(Assets.getTexture("engine", "buttonDown"));

        button = new UIButton("Click Me", buttonStyle, buttonHoverStyle, buttonActiveStyle);
    }

    @Override
    public void update() {
        super.update();

        canvas.processMouseOver(new Vector2i(Mouse.getX(), Display.getHeight() - Mouse.getY()));
    }

    @Override
    public void render() {
        checkGLError();
        canvas.preRender();


        try (SubRegion ignored = canvas.subRegion(Rect2i.createFromMinAndSize(0, 0, 120, 80), true)) {
            button.draw(canvas);
        }

        /*canvas.drawTextureBordered(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y),
                new Border(6, 6, 6, 6), true);

        canvas.drawTexture(getTexture(c1), Rect2i.createFromMinAndSize(0, 0, 128, 128), ScaleMode.STRETCH);
        canvas.addInteractionRegion(Rect2i.createFromMinAndSize(0, 0, 128, 128), c1);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(12, 12, 104, 104), ScaleMode.STRETCH);

        try (SubRegion ignored = canvas.subRegion(Rect2i.createFromMinAndSize(12, 12, 104, 104), true)) {
            canvas.drawTextShadowed("Stretched with a lot more text than there should be", font, Rect2i.createFromMinAndSize(3, 88, 160, 160), Color.BLACK);
        }

        canvas.drawTexture(getTexture(c2), Rect2i.createFromMinAndSize(128, 0, 128, 128), ScaleMode.STRETCH);
        canvas.addInteractionRegion(Rect2i.createFromMinAndSize(128, 0, 128, 128), c2);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(140, 12, 104, 104), ScaleMode.SCALE_FIT);
        canvas.drawTextShadowed("Scaled Fit", font, Rect2i.createFromMinAndSize(142, 75, 104, 104), Color.BLACK);

        canvas.drawTexture(getTexture(c3), Rect2i.createFromMinAndSize(256, 0, 128, 128), ScaleMode.STRETCH);
        canvas.addInteractionRegion(Rect2i.createFromMinAndSize(256, 0, 128, 128), c3);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(268, 12, 104, 104), ScaleMode.SCALE_FILL);
        canvas.drawTextShadowed("Scaled Fill", font, Rect2i.createFromMinAndMax(270, 100, 104, 104), Color.BLACK);

        canvas.drawTexture(Assets.getTexture("engine:icons"), Rect2i.createFromMinAndSize(0, 256, 64, 64), ScaleMode.STRETCH, 52, 0, 9, 9);
        canvas.drawTextureBordered(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(256, 128, 512, 128), new Border(6, 6, 6, 6), false);
        canvas.drawTextureBordered(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(256, 256, 512, 128), new Border(6, 6, 6, 6), true);

        canvas.drawMaterial(Assets.getMaterial("engine:testMaterial"), Rect2i.createFromMinAndSize(0, 128, 256, 256));
        canvas.drawTexture(Assets.getTexture("engine:icons"), Rect2i.createFromMinAndSize(0, 128, 256, 256), ScaleMode.STRETCH, 52, 0, 9, 9);

        Quat4f rot = new Quat4f(0, 0, 0, 1);
        QuaternionUtil.setEuler(rot, CoreRegistry.get(Time.class).getGameTime(), 0, 0);
        canvas.drawMesh(Assets.getMesh("engine:testmonkey"), Assets.getTexture("engine:mhead"),
                Rect2i.createFromMinAndSize(0, 128, 256, 256), rot, new Vector3f(0, 0, 0), 1.5f);     */

        canvas.postRender();

        checkGLError();

    }

    private Texture getTexture(BaseInteractionListener listener) {
        if (listener.isMouseOver()) {
            return active;
        }
        return inactive;
    }
}
