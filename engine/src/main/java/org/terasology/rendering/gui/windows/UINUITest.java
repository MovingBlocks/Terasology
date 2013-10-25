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

import com.bulletphysics.linearmath.QuaternionUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.input.InputSystem;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.widgets.UIWindow;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlignment;
import org.terasology.rendering.nui.LwjglCanvas;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glFrustum;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
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

    private BaseInteractionListener c1 = new BaseInteractionListener();
    private BaseInteractionListener c2 = new BaseInteractionListener();
    private BaseInteractionListener c3 = new BaseInteractionListener();

    public UINUITest() {
        setId("nuitest");
        setModal(true);
        maximize();
        canvas = new LwjglCanvas();
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

        canvas.drawTextureBordered(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y),
                new Border(6, 6, 6, 6), true);

        canvas.drawTexture(getTexture(c1), Rect2i.createFromMinAndSize(0, 0, 128, 128), ScaleMode.STRETCH);
        canvas.addInteractionRegion(Rect2i.createFromMinAndSize(0, 0, 128, 128), c1);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(12, 12, 104, 104), ScaleMode.STRETCH);

        try (SubRegion ignored = canvas.subRegion(Rect2i.createFromMinAndSize(12, 12, 104, 104), true)) {
            canvas.setTextCursor(3, 88);
            canvas.drawTextShadowed(font, "Stretched with a lot more text than there should be", 160, Color.BLACK);
        }

        /*canvas.drawTexture(getTexture(c2), Rect2i.createFromMinAndSize(128, 0, 128, 128), ScaleMode.STRETCH);
        canvas.addInteractionRegion(Rect2i.createFromMinAndSize(128, 0, 128, 128), c2);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(140, 12, 104, 104), ScaleMode.SCALE_FIT);
        canvas.setTextCursor(143, 75);
        canvas.drawTextShadowed(font, "Scaled Fit", Color.BLACK);

        canvas.drawTexture(getTexture(c3), Rect2i.createFromMinAndSize(256, 0, 128, 128), ScaleMode.STRETCH);
        canvas.addInteractionRegion(Rect2i.createFromMinAndSize(256, 0, 128, 128), c3);
        canvas.drawTexture(Assets.getTexture("engine:loadingBackground"), Rect2i.createFromMinAndSize(268, 12, 104, 104), ScaleMode.SCALE_FILL);
        canvas.setTextCursor(270, 100);
        canvas.drawTextShadowed(font, "Scaled Fill", Color.BLACK);   */

        canvas.setTextCursor(0, 150);
        canvas.setTextColor(Color.GREY);
        canvas.drawText(font, "Some Text");
        canvas.drawText(font, "Some More Text");
        canvas.drawText(font, "A little to the right", canvas.size().x, HorizontalAlignment.RIGHT);
        canvas.drawText(font, "Smack in the middle", canvas.size().x, HorizontalAlignment.CENTER);

        canvas.drawTexture(Assets.getTexture("engine:icons"), Rect2i.createFromMinAndSize(0, 256, 64, 64), ScaleMode.STRETCH, 52, 0, 9, 9);
        canvas.drawTextureBordered(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(256, 128, 512, 128), new Border(6, 6, 6, 6), false);
        canvas.drawTextureBordered(Assets.getTexture("engine:testWindowBorder"), Rect2i.createFromMinAndSize(256, 256, 512, 128), new Border(6, 6, 6, 6), true);

        canvas.drawMaterial(Assets.getMaterial("engine:testMaterial"), Rect2i.createFromMinAndSize(0, 128, 256, 256));
        canvas.drawTexture(Assets.getTexture("engine:icons"), Rect2i.createFromMinAndSize(0, 128, 256, 256), ScaleMode.STRETCH, 52, 0, 9, 9);

        Quat4f rot = new Quat4f(0, 0, 0, 1);
        QuaternionUtil.setEuler(rot, CoreRegistry.get(Time.class).getGameTime(), 0, 0);
        canvas.drawMesh(Assets.getMesh("engine:testmonkey"), Assets.getTexture("engine:mhead"),
                Rect2i.createFromMinAndSize(0, 128, 256, 256), rot, new Vector3f(0, 0, 0), 1.5f);

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
