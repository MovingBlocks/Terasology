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

import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.rendering.RenderableObject;
import com.github.begla.blockmania.rendering.TextureManager;
import com.github.begla.blockmania.world.World;

import static org.lwjgl.opengl.GL11.*;

/**
 * A simple renderable sun/moon.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class SunMoon implements RenderableObject {

    private World _parent;

    private int _dlSunMoon = -1;

    public SunMoon(World parent) {
        _parent = parent;
        _dlSunMoon = glGenLists(1);

        generateSunMoonDisplayList();
    }

    public void render() {
        glPushMatrix();

        // Position the sun relatively to the player
        glTranslated(_parent.getPlayer().getPosition().x, Configuration.CHUNK_DIMENSIONS.y * 2.0, Configuration.getSettingNumeric("V_DIST_Z") * Configuration.CHUNK_DIMENSIONS.z + _parent.getPlayer().getPosition().z);
        glRotatef(-35, 1, 0, 0);

        glColor4f(1f, 1f, 1f, 1.0f);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        glEnable(GL_TEXTURE_2D);
        if (_parent.isDaytime()) {
            TextureManager.getInstance().bindTexture("sun");
        } else {
            TextureManager.getInstance().bindTexture("moon");
        }

        glCallList(_dlSunMoon);

        glDisable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();

        glDisable(GL_TEXTURE_2D);
    }

    private void generateSunMoonDisplayList() {
        glNewList(_dlSunMoon, GL_COMPILE);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3d(-Configuration.SUN_SIZE, Configuration.SUN_SIZE, -Configuration.SUN_SIZE);
        glTexCoord2f(1.f, 0.0f);
        glVertex3d(Configuration.SUN_SIZE, Configuration.SUN_SIZE, -Configuration.SUN_SIZE);
        glTexCoord2f(1.f, 1.0f);
        glVertex3d(Configuration.SUN_SIZE, -Configuration.SUN_SIZE, -Configuration.SUN_SIZE);
        glTexCoord2f(0.f, 1.0f);
        glVertex3d(-Configuration.SUN_SIZE, -Configuration.SUN_SIZE, -Configuration.SUN_SIZE);
        glEnd();
        glEndList();
    }

    public void update() {
        // Nothing to do at the moment.
    }
}
