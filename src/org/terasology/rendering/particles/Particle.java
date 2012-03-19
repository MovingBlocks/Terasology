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
import org.terasology.game.Terasology;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Basic particle used by the particle emitter.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Particle implements IGameObject {
    protected final ParticleEmitter _parent;

    protected static final double METERS_TO_MM = 0.001;

    protected final Vector3d _targetVelocity = new Vector3d(0.0f, -1.0f * METERS_TO_MM, 0.0f);
    protected final Vector3d _velDecFactor = new Vector3d(0.01 * METERS_TO_MM, 0.01 * METERS_TO_MM, 0.01 * METERS_TO_MM);

    protected float _size = 0.01f;

    protected final Vector3d _position = new Vector3d();
    protected final Vector3d _initialVelocity = new Vector3d(), _velocity = new Vector3d();

    protected static final FastRandom _rand = new FastRandom();

    protected int _lifetime;

    public Particle(int lifeTime, Vector3d position, ParticleEmitter parent) {
        _position.set(position);

        _initialVelocity.set(_rand.randomDouble() * 4.0, _rand.randomDouble() * 4.0, _rand.randomDouble() * 4.0);
        _initialVelocity.scale(METERS_TO_MM);

        _velocity.set(_initialVelocity);

        _lifetime = lifeTime;
        _parent = parent;
    }

    protected abstract void renderParticle();

    public void render() {
        if (isAlive()) {
            glPushMatrix();

            Vector3d cameraPosition = Terasology.getInstance().getActiveCamera().getPosition();
            glTranslated(_position.x - cameraPosition.x, _position.y - cameraPosition.y, _position.z - cameraPosition.z);
            applyOrientation();
            glScalef(_size, _size, _size);

            renderParticle();

            glPopMatrix();
        }
    }

    public void update(double delta) {
        updateVelocity(delta);
        updatePosition(delta);
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

    protected void updateVelocity(double delta) {
        int dirX = (_velocity.x - _targetVelocity.x) >= 0 ? -1 : 1;
        int dirY = (_velocity.y - _targetVelocity.y) >= 0 ? -1 : 1;
        int dirZ = (_velocity.z - _targetVelocity.z) >= 0 ? -1 : 1;

        if (Math.abs(_velocity.x - _targetVelocity.x) > 0.000001)
            _velocity.x += dirX * _velDecFactor.x * delta;
        else
            _velocity.x = _targetVelocity.x;

        if (Math.abs(_velocity.y - _targetVelocity.y) > 0.000001)
            _velocity.y += dirY * _velDecFactor.y * delta;
        else
            _velocity.y = _targetVelocity.y;

        if (Math.abs(_velocity.z - _targetVelocity.z) > 0.000001)
            _velocity.z += dirZ * _velDecFactor.z * delta;
        else
            _velocity.z = _targetVelocity.z;
    }

    protected boolean canMoveVertically() {
        return true;
    }

    protected void updatePosition(double delta) {
        if (!canMoveVertically()) {
            _velocity.x += (_velocity.y / 2) * _initialVelocity.x * delta;
            _velocity.z += (_velocity.y / 2) * _initialVelocity.z * delta;
            _velocity.y = 0;
        }

        _position.x += _velocity.x * delta;
        _position.y += _velocity.y * delta;
        _position.z += _velocity.z * delta;
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
