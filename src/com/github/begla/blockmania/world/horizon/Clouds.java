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
package com.github.begla.blockmania.world.horizon;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.rendering.Primitives;
import com.github.begla.blockmania.rendering.RenderableObject;
import com.github.begla.blockmania.rendering.ShaderManager;
import com.github.begla.blockmania.world.World;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;
import org.newdawn.slick.util.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import static org.lwjgl.opengl.GL11.*;

/**
 * Some blocky clouds which randomly float around and follow the player's movement.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Clouds implements RenderableObject {

    private boolean[][] _clouds;
    private int _dlClouds = -1;

    private final Vector2f _cloudOffset = new Vector2f(), _windDirection = new Vector2f(0.25f, 0);
    private short _nextWindUpdateInSeconds = 32;
    private double _lastWindUpdate = 0;

    private final World _parent;

    public Clouds(World parent) {
        _parent = parent;
        _dlClouds = glGenLists(1);

        generateClouds();
        generateCloudDisplayList();
    }

    private void generateClouds() {
        try {
            BufferedImage cloudImage = ImageIO.read(ResourceLoader.getResource("com/github/begla/blockmania/data/textures/clouds.png").openStream());
            _clouds = new boolean[cloudImage.getWidth()][cloudImage.getHeight()];

            for (int x = 0; x < cloudImage.getWidth(); x++) {
                for (int y = 0; y < cloudImage.getHeight(); y++) {
                    if ((cloudImage.getRGB(x, y) & 0x00FFFFFF) != 0) {
                        _clouds[x][y] = true;
                    }
                }
            }
        } catch (IOException ex) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates the cloud display list.
     */
    private void generateCloudDisplayList() {
        glNewList(_dlClouds, GL_COMPILE);
        glBegin(GL_QUADS);

        int length = _clouds.length;

        for (int x = 0; x < length; x++) {
            for (int y = 0; y < length; y++) {
                if (_clouds[x][y]) {
                    try {
                        Primitives.drawCloud(16, 16, 16, x * 16f - (length / 2 * 16f), 0, y * 16f - (length / 2 * 16f), !_clouds[x - 1][y], !_clouds[x + 1][y], !_clouds[x][y + 1], !_clouds[x][y - 1]);
                    } catch (Exception e) {

                    }
                }
            }
        }

        glEnd();
        glEndList();
    }

    public void render() {
        // Nothing to do if the player is swimming
        if (_parent.getPlayer().isHeadUnderWater())
            return;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        ShaderManager.getInstance().enableShader("cloud");
        int daylight = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("cloud"), "daylight");
        GL20.glUniform1f(daylight, (float) _parent.getDaylight());

        // Render two passes: The first one only writes to the depth buffer, the second one to the frame buffer
        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                glColorMask(false, false, false, false);
            } else {
                glColorMask(true, true, true, true);
            }

            glPushMatrix();
            glTranslatef(_parent.getPlayer().getPosition().x + _cloudOffset.x, 190f, _parent.getPlayer().getPosition().z + _cloudOffset.y);
            glCallList(_dlClouds);
            glPopMatrix();
        }

        ShaderManager.getInstance().enableShader(null);
        glDisable(GL_BLEND);
    }

    public void update() {
        // Move the clouds a bit each update
        _cloudOffset.x += _windDirection.x;
        _cloudOffset.y += _windDirection.y;

        if (_cloudOffset.x >= _clouds.length * 16 / 2 || _cloudOffset.x <= -(_clouds.length * 16 / 2)) {
            _windDirection.x = -_windDirection.x;
        } else if (_cloudOffset.y >= _clouds.length * 16 / 2 || _cloudOffset.y <= -(_clouds.length * 16 / 2)) {
            _windDirection.y = -_windDirection.y;
        }

        if (Blockmania.getInstance().getTime() - _lastWindUpdate > _nextWindUpdateInSeconds * 1000) {
            _windDirection.x = (float) _parent.getRandom().randomDouble() / 4;
            _windDirection.y = (float) _parent.getRandom().randomDouble() / 4;
            _nextWindUpdateInSeconds = (short) (Math.abs(_parent.getRandom().randomInt()) % 16 + 32);
            _lastWindUpdate = Blockmania.getInstance().getTime();
        }
    }
}
