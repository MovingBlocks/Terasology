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
package com.github.begla.blockmania.game.mobs;

import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.rendering.manager.ShaderManager;
import com.github.begla.blockmania.rendering.manager.TextureManager;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.characters.Character;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.main.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Funny slime.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class GelatinousCube extends Character {

    private static final float WIDTH_HALF = 0.5f, HEIGHT_HALF = 0.5f;
    private static int _displayListOuterBody = -1, _displayListInnerBody = -1;
    private static final Vector3f[] COLORS = {new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 0.75f, 0.75f), new Vector3f(0.75f, 1.0f, 0.75f), new Vector3f(1.0f, 1.0f, 0.75f)};

    private long _lastChangeOfDirectionAt = Blockmania.getInstance().getTime();
    private Vector3f _movementTarget = new Vector3f();

    public int _randomColorId;
    public float _randomSize = 1.0f;

    public GelatinousCube(World parent) {
        super(parent, 0.01, 1.5, 0.125);

        _randomSize = (float) MathHelper.clamp((_parent.getWorldProvider().getRandom().randomDouble() + 1.0) / 2.0 + 0.5);
        _randomColorId = Math.abs(_parent.getWorldProvider().getRandom().randomInt()) % COLORS.length;
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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        TextureManager.getInstance().bindTexture("slime");

        // Setup the shader
        ShaderManager.getInstance().enableShader("gelatinousCube");
        int tick = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("gelatinousCube"), "tick");
        GL20.glUniform1f(tick, _parent.getTick());
        int cOffset = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("gelatinousCube"), "colorOffset");
        GL20.glUniform4f(cOffset, COLORS[_randomColorId].x, COLORS[_randomColorId].y, COLORS[_randomColorId].z, 1.0f);
        int light = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("gelatinousCube"), "light");
        GL20.glUniform1f(light, calcLightValue());

        if (_displayListOuterBody == -1 || _displayListInnerBody == -1) {
            generateDisplayList();
        }

        glPushMatrix();
        glScalef(_randomSize, _randomSize, _randomSize);
        glCallList(_displayListInnerBody);
        glCallList(_displayListOuterBody);
        glPopMatrix();

        glDisable(GL11.GL_TEXTURE_2D);
        glDisable(GL_BLEND);

        ShaderManager.getInstance().enableShader(null);

        glPopMatrix();
    }

    private float calcLightValue() {
        double lightValueSun = ((double) _parent.getWorldProvider().getLightAtPosition(getPosition(), Chunk.LIGHT_TYPE.SUN));
        lightValueSun = (lightValueSun / 15.0) * _parent.getDaylight();
        double lightValueBlock = _parent.getWorldProvider().getLightAtPosition(getPosition(), Chunk.LIGHT_TYPE.BLOCK);
        lightValueBlock = lightValueBlock / 15.0;

        return (float) Math.max(lightValueSun, lightValueBlock);
    }

    private void generateDisplayList() {
        _displayListOuterBody = glGenLists(1);
        _displayListInnerBody = glGenLists(1);

        glNewList(_displayListOuterBody, GL_COMPILE);

        glBegin(GL_QUADS);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);

        // TOP
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF, HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, HEIGHT_HALF, -WIDTH_HALF);

        // LEFT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, -HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, -HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, HEIGHT_HALF, -WIDTH_HALF);


        // BACK
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, -HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF, -HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF);

        // RIGHT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF, HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF, -HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF, -HEIGHT_HALF, -WIDTH_HALF);

        GL11.glColor4f(0.8f, 0.8f, 0.8f, 0.8f);

        // FRONT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF, HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF, -HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, -HEIGHT_HALF, -WIDTH_HALF);

        // BOTTOM
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, -HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF, -HEIGHT_HALF, -WIDTH_HALF);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF, -HEIGHT_HALF, WIDTH_HALF);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF, -HEIGHT_HALF, WIDTH_HALF);

        GL11.glEnd();
        GL11.glEndList();

        glNewList(_displayListInnerBody, GL_COMPILE);

        glBegin(GL_QUADS);
        GL11.glColor4f(0.8f, 0.8f, 0.8f, 1.0f);

        // TOP
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, HEIGHT_HALF / 2, -WIDTH_HALF / 2);

        // LEFT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, -HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, -HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, HEIGHT_HALF / 2, -WIDTH_HALF / 2);


        // BACK
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, -HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, -HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, HEIGHT_HALF / 2, WIDTH_HALF / 2);

        // RIGHT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, -HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, -HEIGHT_HALF / 2, -WIDTH_HALF / 2);

        GL11.glColor4f(0.6f, 0.6f, 0.6f, 1.0f);

        // FRONT
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, -HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, -HEIGHT_HALF / 2, -WIDTH_HALF / 2);

        // BOTTOM
        GL11.glTexCoord2f(0f / 64f, 28f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, -HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 28f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, -HEIGHT_HALF / 2, -WIDTH_HALF / 2);
        GL11.glTexCoord2f(6f / 64f, 22f / 32f);
        GL11.glVertex3f(WIDTH_HALF / 2, -HEIGHT_HALF / 2, WIDTH_HALF / 2);
        GL11.glTexCoord2f(0f / 64f, 22f / 32f);
        GL11.glVertex3f(-WIDTH_HALF / 2, -HEIGHT_HALF / 2, WIDTH_HALF / 2);

        GL11.glEnd();
        GL11.glEndList();
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
        return new AABB(p, new Vector3f(WIDTH_HALF, HEIGHT_HALF, WIDTH_HALF));
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