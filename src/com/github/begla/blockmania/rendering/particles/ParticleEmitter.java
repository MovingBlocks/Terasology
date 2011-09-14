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
import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;

/**
 * Simple particle system.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ParticleEmitter implements RenderableObject {

    protected int _particlesToEmitPerTurn = 16;
    protected int _particlesToEmit;

    protected FastList<Particle> _particles = new FastList();
    protected Vector3f _origin = new Vector3f();

    public void render() {
        for (FastList.Node<Particle> n = _particles.head(), end = _particles.tail(); (n = n.getNext()) != end; ) {
            n.getValue().render();
        }
    }

    public void update() {
        removeDeadParticles();
        emitParticles();

        for (FastList.Node<Particle> n = _particles.head(), end = _particles.tail(); (n = n.getNext()) != end; ) {
            n.getValue().update();
        }
    }

    private void removeDeadParticles() {
        for (int i = _particles.size() - 1; i >= 0; i--) {
            Particle p = _particles.get(i);

            if (!p.isAlive()) {
                _particles.remove(i);
            }
        }
    }

    protected void emitParticles() {
        for (int i = 0; i < _particlesToEmitPerTurn && _particlesToEmit > 0; i++) {
            _particles.add(createParticle());
            _particlesToEmit--;
        }
    }

    public void setOrigin(Vector3f origin) {
        _origin.set(origin);
    }

    public void emitParticles(int amount) {
        _particlesToEmit = amount;
    }

    protected abstract Particle createParticle();
}
