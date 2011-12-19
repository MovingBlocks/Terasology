/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.gui;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import com.github.begla.blockmania.rendering.manager.FontManager;
import com.github.begla.blockmania.rendering.manager.TextureManager;
import com.github.begla.blockmania.world.chunk.ChunkMeshGenerator;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class HUD implements RenderableObject {

    /* ANIMATION */
    private double _cubeRotation;

    private Blockmania _parent;

    /**
     * Init. the HUD.
     *
     * @param parent The parent
     */
    public HUD(Blockmania parent) {
        _parent = parent;
    }

    /**
     * A small rotating cube that serves as indicator for the currently selected block type.
     */
    private void drawRotatingBlock() {
        // TODO: The color shifted leaf blocks seem to appear without color shifting on this rotating block (and Cactus looks funny)
        Block b = BlockManager.getInstance().getBlock(_parent.getActiveWorld().getPlayer().getSelectedBlockType());
        if (b == null)
            return;

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();

        float aspectRatio = ((Double) ConfigurationManager.getInstance().getConfig().get("Graphics.aspectRatio")).floatValue();
        gluPerspective(25f, aspectRatio, 0.1f, 32f);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glAlphaFunc(GL_GREATER, 0.1f);
        glDisable(GL_DEPTH_TEST);
        gluLookAt(0, 0, -25, 8f, 4.5f, 0, 0, 1, 0);
        glRotated(_cubeRotation % 360, 0, 1, 1);

        b.render();

        glEnable(GL_DEPTH_TEST);
        glDisable(GL11.GL_BLEND);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    /**
     * Renders the HUD on the screen.
     */
    public void render() {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(), 0, -5, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glDisable(GL_DEPTH_TEST);
        // Draw the crosshair
        if ((Boolean) ConfigurationManager.getInstance().getConfig().get("HUD.crosshair")) {
            glColor4f(1f, 1f, 1f, 1f);
            glLineWidth(2f);

            glBegin(GL_LINES);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f - 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f + 8f, Display.getDisplayMode().getHeight() / 2f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f - 8f);
            glVertex2d(Display.getDisplayMode().getWidth() / 2f, Display.getDisplayMode().getHeight() / 2f + 8f);
            glEnd();
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // HACK: Make sure Slick binds a new texture...
        TextureManager.getInstance().getTexture("terrain").bind();

        /*
        * Draw debugging information.
        */
        if ((Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.debug")) {
            double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;

            FontManager.getInstance().getFont("default").drawString(4, 4, String.format("%s (fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f)", ConfigurationManager.getInstance().getConfig().get("System.gameTitle"), Blockmania.getInstance().getAverageFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
            FontManager.getInstance().getFont("default").drawString(4, 22, String.format("%s", _parent.getActiveWorld().getPlayer()));
            FontManager.getInstance().getFont("default").drawString(4, 38, String.format("%s", _parent.getActiveWorld()));
            FontManager.getInstance().getFont("default").drawString(4, 54, String.format("total vus: %s | active threads: %s", ChunkMeshGenerator.getVertexArrayUpdateCount(), _parent.getThreadPool().getActiveCount()));
        }

        if (Blockmania.getInstance().isGamePaused()) {
            // Display the console input text
            FontManager.getInstance().getFont("default").drawString(4, Display.getDisplayMode().getHeight() - 16 - 4, String.format("%s_", _parent.getConsole()));
        }

        // HACK: Disable the texture mode Slick activated...
        glDisable(GL11.GL_TEXTURE_2D);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);


        if ((Boolean) ConfigurationManager.getInstance().getConfig().get("HUD.rotatingBlock"))
            drawRotatingBlock();
    }

    public void update() {
        // Rotate the block indicator
        _cubeRotation += 0.5;
    }
}
