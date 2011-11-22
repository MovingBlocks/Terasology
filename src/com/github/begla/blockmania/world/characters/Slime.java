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
 *
 */
package com.github.begla.blockmania.world.characters;

import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.rendering.manager.TextureManager;
import com.github.begla.blockmania.world.main.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Funny slime.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Slime extends Character {

    public static int _instanceCounter;

    private long _lastChangeOfDirectionAt = Blockmania.getInstance().getTime();
    private Vector3f _movementTarget = new Vector3f();

    public Slime(World parent) {
        // TODO: Load this from config
        super(parent, 0.01, 1.5, 0.125);
        _instanceCounter++;
    }

    public void update() {
        super.update();
    }

    public void render() {
        super.render();

        glPushMatrix();

        glTranslatef(getPosition().x - _parent.getWorldProvider().getRenderingReferencePoint().x, getPosition().y - _parent.getWorldProvider().getRenderingReferencePoint().y, getPosition().z - _parent.getWorldProvider().getRenderingReferencePoint().z);
        glRotatef((float) _yaw, 0f, 1f, 0f);

        glEnable(GL_TEXTURE_2D);
        TextureManager.getInstance().bindTexture("slime");

        float brightness = (float) Math.pow(0.84, 15.0 - _parent.getDaylight() * 15.0);

        glBegin(GL_QUADS);
        GL11.glColor3f(brightness, brightness, brightness);

        // TOP
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-0.25f, 0.2f, 0.25f);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(0.25f, 0.2f, 0.25f);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(0.25f, 0.2f, -0.25f);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-0.25f, 0.2f, -0.25f);

        // LEFT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-0.25f, -0.2f, -0.25f);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(-0.25f, -0.2f, 0.25f);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(-0.25f, 0.2f, 0.25f);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-0.25f, 0.2f, -0.25f);


        // BACK
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-0.25f, -0.2f, 0.25f);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(0.25f, -0.2f, 0.25f);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(0.25f, 0.2f, 0.25f);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-0.25f, 0.2f, 0.25f);

        // RIGHT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(0.25f, 0.2f, -0.25f);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(0.25f, 0.2f, 0.25f);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(0.25f, -0.2f, 0.25f);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(0.25f, -0.2f, -0.25f);

        GL11.glColor3f(brightness * 0.25f, brightness * 0.25f, brightness * 0.25f);

        // FRONT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-0.25f, 0.2f, -0.25f);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(0.25f, 0.2f, -0.25f);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(0.25f, -0.2f, -0.25f);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-0.25f, -0.2f, -0.25f);

        // BOTTOM
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-0.25f, -0.2f, -0.25f);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(0.25f, -0.2f, -0.25f);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(0.25f, -0.2f, 0.25f);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-0.25f, -0.2f, 0.25f);

        GL11.glEnd();

        glDisable(GL11.GL_TEXTURE_2D);

        glPopMatrix();
    }

    public void processMovement() {
        double distanceToPlayer = distanceSquaredTo(_parent.getPlayer().getPosition());

        if (distanceToPlayer > 5 && distanceToPlayer < 32) {
            _movementTarget.set(_parent.getPlayer().getPosition());
        }

        if (Blockmania.getInstance().getTime() - _lastChangeOfDirectionAt > 5000 || distanceToPlayer <= 5) {
            _movementTarget.set((float) (getPosition().x + _parent.getWorldProvider().getRandom().randomDouble() * 500), getPosition().y, (float) (getPosition().z + _parent.getWorldProvider().getRandom().randomDouble() * 500));
            _lastChangeOfDirectionAt = Blockmania.getInstance().getTime();
        }


        lookAt(_movementTarget);
        walkForward();

        if (_parent.getWorldProvider().getRandom().randomDouble() < -0.94)
            jump();
    }

    protected AABB generateAABBForPosition(Vector3f p) {
        return new AABB(p, new Vector3f(.25f, .25f, .25f));
    }

    public AABB getAABB() {
        return generateAABBForPosition(getPosition());
    }

    @Override
    protected void handleVerticalCollision() {
        // Do nothing
    }

    @Override
    protected void handleHorizontalCollision() {
        _lastChangeOfDirectionAt = 0;
    }
}