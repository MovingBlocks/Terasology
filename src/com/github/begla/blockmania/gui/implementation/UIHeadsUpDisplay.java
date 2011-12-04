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
package com.github.begla.blockmania.gui.implementation;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.gui.framework.BlockmaniaDisplayRenderer;
import com.github.begla.blockmania.world.chunk.ChunkMeshGenerator;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * HUD displayed on the user's screen.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIHeadsUpDisplay extends BlockmaniaDisplayRenderer {

    /* ANIMATION */
    private double _cubeRotation;

    /* DISPLAY ELEMENTS */
    private UICrosshair _crosshair;
    private UIText _debugLine1, _debugLine2, _debugLine3, _debugLine4;
    private UIDebugConsole _console;

    /**
     * Init. the HUD.
     */
    public UIHeadsUpDisplay() {
        _crosshair = new UICrosshair();

        _debugLine1 = new UIText(new Vector2f(4, 4));
        _debugLine2 = new UIText(new Vector2f(4, 22));
        _debugLine3 = new UIText(new Vector2f(4, 38));
        _debugLine4 = new UIText(new Vector2f(4, 54));

        addDisplayElement(_crosshair);
        addDisplayElement(_debugLine1);
        addDisplayElement(_debugLine2);
        addDisplayElement(_debugLine3);
        addDisplayElement(_debugLine4);

        _console = new UIDebugConsole();
        addDisplayElement(_console);
    }

    /**
     * A small rotating cube that serves as indicator for the currently selected block type.
     */
    private void drawRotatingBlock() {
        Block b = BlockManager.getInstance().getBlock(Blockmania.getInstance().getActiveWorld().getPlayer().getSelectedBlockType());

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
        super.render();

        if ((Boolean) ConfigurationManager.getInstance().getConfig().get("HUD.rotatingBlock"))
            drawRotatingBlock();
    }

    public void update() {
        super.update();

        _console.setVisible(Blockmania.getInstance().isGamePaused());
        _crosshair.setVisible((Boolean) ConfigurationManager.getInstance().getConfig().get("HUD.crosshair"));
        _crosshair.setPosition(new Vector2f(Display.getDisplayMode().getWidth() / 2, Display.getDisplayMode().getHeight() / 2));

        boolean enableDebug = (Boolean) ConfigurationManager.getInstance().getConfig().get("System.Debug.debug");
        _debugLine1.setVisible(enableDebug);
        _debugLine2.setVisible(enableDebug);
        _debugLine3.setVisible(enableDebug);
        _debugLine4.setVisible(enableDebug);

        double memoryUsage = ((double) Runtime.getRuntime().totalMemory() - (double) Runtime.getRuntime().freeMemory()) / 1048576.0;
        _debugLine1.setText(String.format("%s (fps: %.2f, mem usage: %.2f MB, total mem: %.2f, max mem: %.2f)", ConfigurationManager.getInstance().getConfig().get("System.gameTitle"), Blockmania.getInstance().getAverageFps(), memoryUsage, Runtime.getRuntime().totalMemory() / 1048576.0, Runtime.getRuntime().maxMemory() / 1048576.0));
        _debugLine2.setText(String.format("%s", Blockmania.getInstance().getActiveWorld().getPlayer()));
        _debugLine3.setText(String.format("%s", Blockmania.getInstance().getActiveWorld()));
        _debugLine4.setText(String.format("total vus: %s | active threads: %s", ChunkMeshGenerator.getVertexArrayUpdateCount(), Blockmania.getInstance().getThreadPool().getActiveCount()));

        // Rotate the block indicator
        _cubeRotation += 0.5;
    }

    public UIDebugConsole getDebugConsole() {
        return _console;
    }
}
