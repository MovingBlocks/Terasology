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
package com.github.begla.blockmania.rendering.particles;

import com.github.begla.blockmania.rendering.RenderableObject;
import com.github.begla.blockmania.utilities.FastRandom;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Basic particle used by the particle emitter.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Particle implements RenderableObject {
    protected ParticleEmitter _parent;

    protected final Vector3f _targetVelocity = new Vector3f(0.0f, -0.03f, 0.0f);
    protected final Vector3f _velDecSpeed = new Vector3f(0.003f, 0.003f, 0.003f);

    protected final Vector3f _position = new Vector3f();
    protected final Vector3f _velocity = new Vector3f();
    protected int _orientation = 0;
    protected int _displayList = -1;

    protected static final FastRandom _rand = new FastRandom();

    protected int _lifetime;

    public Particle(int lifeTime, Vector3f position, ParticleEmitter parent) {
        _position.set(position);
        _velocity.set((float) _rand.randomDouble() / 16f, (float) _rand.randomDouble() / 16f, (float) _rand.randomDouble() / 16f);
        _lifetime = lifeTime;
        _orientation = _rand.randomInt() % 360;
        _parent = parent;
    }

    protected abstract void renderParticle();

    public void render() {
        if (isAlive()) {
            glPushMatrix();
            glTranslatef(_position.x, _position.y, _position.z);
            applyOrientation();

            if (_displayList == -1) {
                glGenLists(_displayList);
                renderParticle();
                glEndList();
            }

            glCallList(_displayList);

            glPopMatrix();
        }
    }

    public void update() {
        updateVelocity();
        updatePosition();
        decLifetime();
    }

    private void applyOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j)
                    model.put(i * 4 + j, 1.0f);
                else
                    model.put(i * 4 + j, 0.0f);
            }
        }

        GL11.glLoadMatrix(model);
    }

    protected void updateVelocity() {
        if (_velocity.x > _targetVelocity.x)
            _velocity.x -= _velDecSpeed.x;
        if (_velocity.x < _targetVelocity.x)
            _velocity.x += _velDecSpeed.x;
        if (_velocity.y > _targetVelocity.y)
            _velocity.y -= _velDecSpeed.y;
        if (_velocity.y < _targetVelocity.y)
            _velocity.y += _velDecSpeed.y;
        if (_velocity.z > _targetVelocity.z)
            _velocity.z -= _velDecSpeed.z;
        if (_velocity.z < _targetVelocity.z)
            _velocity.z += _velDecSpeed.z;
    }

    protected boolean canMove() {
        return true;
    }

    protected void updatePosition() {
        if (!canMove())
            return;

        Vector3f.add(_position, _velocity, _position);
    }

    protected void decLifetime() {
        if (_lifetime > 0)
            _lifetime--;
    }

    public boolean isAlive() {
        return _lifetime > 0;
    }

    protected ParticleEmitter getParent() {
        return _parent;
    }

    protected void dispose() {
        if (_displayList != -1)
            glDeleteLists(_displayList, 1);
        _displayList = 0;
    }
}
