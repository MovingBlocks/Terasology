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
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.input.InputSystem;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.LwjglCanvas;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UISkinBuilder;
import org.terasology.rendering.nui.skin.UISkinData;

import static org.lwjgl.opengl.Util.checkGLError;

/**
 * @author Immortius
 */
public class UINUITest extends UIWindow {

    private LwjglCanvas canvas;
    private Font font = Assets.getFont("engine:default");
    private InputSystem input = CoreRegistry.get(InputSystem.class);

    private UISkin skin;

    private UIButton button;


    public UINUITest() {
        setId("nuitest");
        setModal(true);
        maximize();
        canvas = new LwjglCanvas();
        CoreRegistry.get(AssetManager.class).setAssetFactory(AssetType.UI_SKIN, new AssetFactory<UISkinData, UISkin>() {
            @Override
            public UISkin buildAsset(AssetUri uri, UISkinData data) {
                return new UISkin(uri, data);
            }
        });

        UISkinData skinData = new UISkinBuilder()
                .setWidgetClass(UIButton.class)
                .setBackground(Assets.getTexture("engine", "button"))
                .setTextHorizontalAlignment(HorizontalAlign.CENTER)
                .setTextVerticalAlignment(VerticalAlign.MIDDLE)
                .setBackgroundBorder(new Border(1, 1, 1, 1))
                .setMargin(new Border(4, 4, 4, 4))
                .setTextShadowed(true)

                .setWidgetMode("hover")
                .setBackground(Assets.getTexture("engine", "buttonOver"))

                .setWidgetMode("down")
                .setBackground(Assets.getTexture("engine", "buttonDown"))
                .setTextColor(Color.YELLOW)
                .build();

        skin = Assets.generateAsset(new AssetUri(AssetType.UI_SKIN, "engine:defaultSkin"), skinData, UISkin.class);

        button = new UIButton("Click Me please and lots of other text to demonstrate margin");
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
        canvas.setSkin(skin);

        try (SubRegion ignored = canvas.subRegion(Rect2i.createFromMinAndSize(0, 0, 120, 80), true)) {
            canvas.setWidget(button.getClass());
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
}
