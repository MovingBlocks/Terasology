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
package com.github.begla.blockmania.logic.characters;

import com.github.begla.blockmania.logic.manager.ShaderManager;
import com.github.begla.blockmania.logic.manager.TextureManager;
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.model.structures.AABB;
import com.github.begla.blockmania.rendering.world.WorldRenderer;
import com.github.begla.blockmania.utilities.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Funny slime wobbling around in the world. Sweet!
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class GelatinousCube extends Character {

    private static final float WIDTH_HALF = 0.5f, HEIGHT_HALF = 0.5f;
    private static int _displayListOuterBody = -1, _displayListInnerBody = -1;
    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 0.2f), new Vector3f(1.0f, 0.2f, 0.2f), new Vector3f(0.2f, 1.0f, 0.2f), new Vector3f(1.0f, 1.0f, 0.2f)};

    private long _lastChangeOfDirectionAt = Blockmania.getInstance().getTime();
    private final Vector3d _movementTarget = new Vector3d();

    public final int _randomColorId;
    public float _randomSize = 1.0f;

    public GelatinousCube(WorldRenderer parent) {
        super(parent, 0.02, 1.5, 0.2);

        _randomSize = (float) MathHelper.clamp((_parent.getWorldProvider().getRandom().randomDouble() + 1.0) / 2.0 + 0.15);
        _randomColorId = Math.abs(_parent.getWorldProvider().getRandom().randomInt()) % COLORS.length;
    }

    public void update() {
        super.update();
    }

    public void render() {
        super.render();

        glPushMatrix();

        glTranslated(getPosition().x - _parent.getWorldProvider().getRenderingReferencePoint().x, getPosition().y - _parent.getWorldProvider().getRenderingReferencePoint().y, getPosition().z - _parent.getWorldProvider().getRenderingReferencePoint().z);
        glRotatef((float) _yaw, 0f, 1f, 0f);

        TextureManager.getInstance().bindTexture("slime");

        // Setup the shader
        ShaderManager.getInstance().enableShader("gelatinousCube");
        int tick = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("gelatinousCube"), "tick");
        GL20.glUniform1f(tick, _parent.getTick());
        int cOffset = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("gelatinousCube"), "colorOffset");
        GL20.glUniform4f(cOffset, COLORS[_randomColorId].x, COLORS[_randomColorId].y, COLORS[_randomColorId].z, 1.0f);
        int light = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("gelatinousCube"), "light");
        GL20.glUniform1f(light, _parent.getRenderingLightValueAt(getPosition()));

        if (_displayListOuterBody == -1 || _displayListInnerBody == -1) {
            generateDisplayList();
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glPushMatrix();
        glScalef(_randomSize, _randomSize, _randomSize);
        glCallList(_displayListInnerBody);

        glCallList(_displayListOuterBody);
        glPopMatrix();

        glDisable(GL_BLEND);

        ShaderManager.getInstance().enableShader(null);

        glPopMatrix();
    }

    private void generateDisplayList() {
        _displayListOuterBody = glGenLists(1);
        _displayListInnerBody = glGenLists(1);

        glNewList(_displayListOuterBody, GL_COMPILE);

        glBegin(GL_QUADS);

        drawCubeBody(new Vector4f(1.0f, 1.0f, 1.0f, 0.9f), 1.0f);

        GL11.glEnd();
        GL11.glEndList();

        glNewList(_displayListInnerBody, GL_COMPILE);

        glBegin(GL_QUADS);

        drawCubeBody(new Vector4f(0.75f, 0.75f, 0.75f, 0.9f), 0.5f);

        GL11.glEnd();
        GL11.glEndList();
    }

    private void drawCubeBody(Vector4f color, float size) {
        GL11.glColor4f(color.x, color.y, color.z, color.w);

        // TOP
        GL11.glNormal3f(0, 1, 0);

        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(WIDTH_HALF * size, HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(WIDTH_HALF * size, HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, HEIGHT_HALF * size, -WIDTH_HALF * size);

        GL11.glNormal3f(-1, 0, 0);

        // LEFT
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, -HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, -HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, HEIGHT_HALF * size, -WIDTH_HALF * size);

        GL11.glNormal3f(0, 0, -1);

        // BACK
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, -HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(WIDTH_HALF * size, -HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(WIDTH_HALF * size, HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, HEIGHT_HALF * size, WIDTH_HALF * size);

        GL11.glNormal3f(0, -1, 0);

        // BOTTOM
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, -HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(WIDTH_HALF * size, -HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(WIDTH_HALF * size, -HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, -HEIGHT_HALF * size, WIDTH_HALF * size);


        GL11.glColor4f(color.x * 0.75f, color.y * 0.75f, color.z * 0.75f, color.w);

        GL11.glNormal3f(0, 0, 1);

        // FRONT
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(WIDTH_HALF * size, HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(WIDTH_HALF * size, -HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-WIDTH_HALF * size, -HEIGHT_HALF * size, -WIDTH_HALF * size);

        GL11.glNormal3f(1, 0, 0);

        // RIGHT
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(WIDTH_HALF * size, HEIGHT_HALF * size, -WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(WIDTH_HALF * size, HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(WIDTH_HALF * size, -HEIGHT_HALF * size, WIDTH_HALF * size);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(WIDTH_HALF * size, -HEIGHT_HALF * size, -WIDTH_HALF * size);

    }

    public void processMovement() {
        double distanceToPlayer = distanceSquaredTo(_parent.getPlayer().getPosition());

        if (distanceToPlayer > 5 && distanceToPlayer < 32) {
            _movementTarget.set(_parent.getPlayer().getPosition());
        }

        if (Blockmania.getInstance().getTime() - _lastChangeOfDirectionAt > 5000 || distanceToPlayer <= 5) {
            _movementTarget.set(getPosition().x + _parent.getWorldProvider().getRandom().randomDouble() * 500, getPosition().y, getPosition().z + _parent.getWorldProvider().getRandom().randomDouble() * 500);
            _lastChangeOfDirectionAt = Blockmania.getInstance().getTime();
        }

        lookAt(_movementTarget);
        walkForward();

        if (_parent.getWorldProvider().getRandom().randomDouble() < -0.94)
            jump();
    }

    protected AABB generateAABBForPosition(Vector3d p) {
        return new AABB(p, new Vector3d(WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF));
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