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
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Particle implements RenderableObject {

    private final Vector3f _targetVelocity = new Vector3f(0.0f, -0.02f, 0.0f);
    private final Vector3f _velDecSpeed = new Vector3f(0.0005f, 0.0005f, 0.0005f);

    private final Vector3f _position = new Vector3f();
    private final Vector3f _velocity = new Vector3f();
    private int _orientation = 0;

    private static final FastRandom _rand = new FastRandom();

    private int _lifeTime;

    public Particle(int lifeTime, Vector3f position) {
        _position.set(position);
        _velocity.set((float) _rand.randomDouble() / 30f, (float) _rand.randomDouble() / 30f, (float) _rand.randomDouble() / 30f);
        _lifeTime = lifeTime;
        _orientation = _rand.randomInt() % 360;
    }

    protected abstract void renderParticle();

    public void render() {
        if (isAlive()) {
            glPushMatrix();
            glTranslatef(_position.x, _position.y, _position.z);
            glRotatef(_orientation, 0, 1, 0);
            renderParticle();
            glPopMatrix();
        }
    }

    public void update() {
        updateVelocity();
        updatePosition();
        decLifetime();
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

    protected void updatePosition() {
        Vector3f.add(_position, _velocity, _position);
    }

    protected void decLifetime() {
        if (_lifeTime > 0)
            _lifeTime--;
    }

    public boolean isAlive() {
        return _lifeTime > 0;
    }
}
