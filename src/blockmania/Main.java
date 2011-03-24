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

    /** time at last frame */
    long lastFrame;
    /** frames per second */
    int fps;
    /** last fps time */
    long lastFPS;
    // Constant values
    private static final float DISPLAY_HEIGHT = 864.0f;
    private static final float DISPLAY_WIDTH = 1536.0f;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
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

        if (Configuration.displayDebug) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    new DebugWindow().setVisible(true);
                }
            });
        }


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
     * Get the time in milliseconds
     * 
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public int getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;

        return delta;
    }

    /**
     * Calculate the FPS and set it in the title bar
     */
    public void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
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

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_FOG);

        glDisable(GL_LIGHTING);

        //glDisable(GL_COLOR_MATERIAL);

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

        world = new World();
        player = new Player(world);
        world.setPlayer(player);
        Chunk.init();

    }

    public void processKeyboard() {
        if (Keyboard.isKeyDown(Keyboard.KEY_W))//move forward
        {
            player.walkForward();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S))//move backwards
        {
            player.walkBackwards();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A))//strafe left
        {
            player.strafeLeft();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D))//strafe right
        {
            player.strafeRight();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            player.jump();
        }
    }

    public void processMouse() {
    }

    public void render() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();

        glPushMatrix();
        player.render();
        world.render();

        // Draw coordinate axis
        // -->
        glBegin(GL_LINES);
        glColor3f(255.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(1000.0f, 0.0f, 0.0f);
        glEnd();

        glBegin(GL_LINES);
        glColor3f(0.0f, 255.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 1000.0f, 0.0f);
        glEnd();

        glBegin(GL_LINES);
        glColor3f(0.0f, 0.0f, 255.0f);
        glVertex3f(0.0f, 0.0f, 0.0f);
        glVertex3f(0.0f, 0.0f, 1000.0f);
        glEnd();
        // <--

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
        getDelta();
        lastFPS = getTime();

        while (!Display.isCloseRequested()) {
            int delta = getDelta();

            processKeyboard();
            processMouse();

            update(delta);
            render();

            Display.update();
            Display.sync(60);
        }

        Display.destroy();
    }

    public void update(int delta) {
        world.update(delta);
        player.update(delta);
    }
}
