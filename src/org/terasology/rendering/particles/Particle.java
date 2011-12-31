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
package org.terasology.rendering.particles;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.rendering.interfaces.RenderableObject;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Basic particle used by the particle emitter.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Particle implements RenderableObject {
    protected final ParticleEmitter _parent;

    protected final Vector3d _targetVelocity = new Vector3d(0.0f, -0.03f, 0.0f);
    protected final Vector3d _velDecSpeed = new Vector3d(0.003f, 0.003f, 0.003f);

    protected float _size = 0.01f;

    protected final Vector3d _position = new Vector3d();
    protected final Vector3d _initialVelocity = new Vector3d(), _velocity = new Vector3d();

    protected static final FastRandom _rand = new FastRandom();

    protected int _lifetime;

    public Particle(int lifeTime, Vector3d position, ParticleEmitter parent) {
        _position.set(position);
        _initialVelocity.set((_rand.randomInt() % 32) * 0.003d, (_rand.randomInt() % 32) * 0.003f, (_rand.randomInt() % 32) * 0.003d);
        _velocity.set(_initialVelocity);

        _lifetime = lifeTime;
        _parent = parent;
    }

    protected abstract void renderParticle();

    public void render() {
        if (isAlive()) {
            glPushMatrix();

            Vector3d r = getParent().getParent().getWorldProvider().getRenderingReferencePoint();
            glTranslated(_position.x - r.x, _position.y - r.y, _position.z - r.z);
            applyOrientation();
            glScalef(_size, _size, _size);

            renderParticle();

            glPopMatrix();
        }
    }

    public void update() {
        updateVelocity();
        updatePosition();
        decLifetime();
    }

    protected void applyOrientation() {
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
        int dirX = (_velocity.x - _targetVelocity.x) >= 0 ? -1 : 1;
        int dirY = (_velocity.y - _targetVelocity.y) >= 0 ? -1 : 1;
        int dirZ = (_velocity.z - _targetVelocity.z) >= 0 ? -1 : 1;

        if (Math.abs(_velocity.x - _targetVelocity.x) > 0.01)
            _velocity.x += dirX * _velDecSpeed.x;
        else
            _velocity.x = _targetVelocity.x;

        if (Math.abs(_velocity.y - _targetVelocity.y) > 0.01)
            _velocity.y += dirY * _velDecSpeed.y;
        else
            _velocity.y = _targetVelocity.y;

        if (Math.abs(_velocity.z - _targetVelocity.z) > 0.01)
            _velocity.z += dirZ * _velDecSpeed.z;
        else
            _velocity.z = _targetVelocity.z;
    }

    protected boolean canMoveVertically() {
        return true;
    }

    protected void updatePosition() {
        if (!canMoveVertically()) {
            _velocity.x += (_velocity.y / 2) * _initialVelocity.x;
            _velocity.z += (_velocity.y / 2) * _initialVelocity.z;
            _velocity.y = 0;
        }

        _position.add(_velocity);
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
}
