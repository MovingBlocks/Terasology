/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package blockmania;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Main {

    // Constant values
    private String GAME_TITLE = "Blockmania (Pre) Alpha";
    private static long timerTicksPerSecond = Sys.getTimerResolution();
    private static final float DISPLAY_HEIGHT = 600.0f;
    private static final float DISPLAY_WIDTH = 800.0f;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    // Time at the start of the last render loop
    private long lastLoopTime = getTime();
    // Time at last fps measurement.
    private long lastFpsTime;
    // Measured rames per second.
    private int fps;
    // Player
    Player player;
    // World
    World world;

    static {
        try {
            LOGGER.addHandler(new FileHandler("errors.log", true));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.toString(), ex);
        }
    }

    public static void main(String[] args) {

        Main main = null;

        try {
            main = new Main();

            main.create();
            main.start();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        } finally {
            if (main != null) {
                main.destroy();
            }
        }

        System.exit(0);
    }

    /**
     * Gets the current time in milliseconds.
     * 
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / timerTicksPerSecond;
    }

    public void create() throws LWJGLException {
        // Display
        Display.setDisplayMode(new DisplayMode((int) DISPLAY_WIDTH, (int) DISPLAY_HEIGHT));
        Display.setFullscreen(false);
        Display.setTitle("Blockmania");
        Display.create();

        // Keyboard
        Keyboard.create();

        // Mouse
        Mouse.setGrabbed(true);
        Mouse.create();

        // OpenGL
        initGL();
        resizeGL();
    }

    public void destroy() {
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    public void initGL() {
        glClearColor(0.5f, 0.75f, 1.0f, 1.0f);
        glLineWidth(2.0f);

        glShadeModel(GL_FLAT);

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_FOG);

        glDisable(GL_LIGHTING);
        glDisable(GL_NORMALIZE);
        glDisable(GL_LIGHTING);

        float[] fogColor = {0.75f, 0.75f, 0.75f, 1.0f};
        FloatBuffer fogColorBuffer = BufferUtils.createFloatBuffer(4);
        fogColorBuffer.put(fogColor);
        fogColorBuffer.rewind();

        glFog(GL_FOG_COLOR, fogColorBuffer);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        glFogf(GL_FOG_DENSITY, 1.0f);
        glHint(GL_FOG_HINT, GL_DONT_CARE);
        glFogf(GL_FOG_START, 256.0f);
        glFogf(GL_FOG_END, 512.0f);

        world = new World("WORLD1", "YEY");
        player = new Player(world);
        Chunk.init();

    }

    public void processKeyboard() {
    }

    public void processMouse() {
    }

    public void render() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        glPushMatrix();
        player.render();
        Chunk.renderAllChunks();
        glPopMatrix();
    }

    public void resizeGL() {
        glViewport(0, 0, (int) DISPLAY_WIDTH, (int) DISPLAY_HEIGHT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(64.0f, DISPLAY_WIDTH / DISPLAY_HEIGHT, 0.1f, 1000f);
        glPushMatrix();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
    }

    public void start() {

        while (!Display.isCloseRequested()) {
            // Sync. at 60 FPS.
            Display.sync(60);

            long delta = getTime() - lastLoopTime;
            lastLoopTime = getTime();
            lastFpsTime += delta;
            fps++;

            // Update the FPS display in the title bar (only) at every second passed.
            if (lastFpsTime >= 1000) {
                Display.setTitle(GAME_TITLE + " (FPS: " + fps + ")");
                lastFpsTime = 0;
                fps = 0;
            }

            // Update the scene.
            update(delta);
            // Render the scene.
            render();

            // Update the display.
            Display.update();

            // Process the keyboard and mouse input.
            processKeyboard();
            processMouse();
        }

        Display.destroy();
    }

    /**
     * Updates the player and the world.
     */
    public void update(long delta) {
        world.update(delta);
        player.update(delta);
    }
}
