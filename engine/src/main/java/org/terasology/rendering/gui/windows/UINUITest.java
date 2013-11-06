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
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.layout.ColumnLayout;
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

    private ColumnLayout grid;


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
                .setBackgroundBorder(new Border(2, 2, 2, 2))
                .setMargin(new Border(4, 4, 4, 4))
                .setTextShadowed(true)
                .setTextureScaleMode(ScaleMode.SCALE_FIT)

                .setWidgetMode("hover")
                .setBackground(Assets.getTexture("engine", "buttonOver"))

                .setWidgetMode("down")
                .setBackground(Assets.getTexture("engine", "buttonDown"))
                .setTextColor(Color.YELLOW)
                .build();

        skin = Assets.generateAsset(new AssetUri(AssetType.UI_SKIN, "engine:defaultSkin"), skinData, UISkin.class);

        grid = new ColumnLayout();
        grid.addWidget(new UIButton("Single Player"));
        grid.addWidget(new UIButton("Host Game"));
        grid.addWidget(new UIButton("Join Game"));
        grid.addWidget(new UIButton("Settings"));
        grid.addWidget(null);
        grid.addWidget(new UIButton("Exit"));
        grid.setPadding(new Border(0, 0, 4, 4));
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
        canvas.drawTextureRaw(Assets.getTexture("engine:menuBackground"), Rect2i.createFromMinAndSize(Vector2i.zero(), canvas.size()), ScaleMode.SCALE_FILL);

        canvas.drawWidget(grid, Rect2i.createFromMinAndSize((canvas.size().x - 280) / 2, (canvas.size().y - 192) / 2, 280, 192));

        canvas.postRender();

        checkGLError();
    }
}
