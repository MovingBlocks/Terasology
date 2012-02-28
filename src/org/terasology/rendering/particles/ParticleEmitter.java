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

import org.lwjgl.opengl.GL11;
import org.terasology.logic.manager.Config;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * Simple particle system.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ParticleEmitter implements IGameObject {

    protected static final int MAX_PARTICLES = Config.getInstance().getMaxParticles();
    protected static final int PARTICLES_PER_UPDATE = 32;
    /* ------- */
    protected int _particlesToEmit;

    protected final ArrayList<Particle> _particles = new ArrayList<Particle>();
    protected final Vector3d _origin = new Vector3d();

    protected final WorldRenderer _parent;

    public ParticleEmitter(WorldRenderer parent) {
        _parent = parent;
    }

    public void render() {
        glDisable(GL11.GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < _particles.size(); i++) {
            Particle p = _particles.get(i);
            p.render();
        }

        glDisable(GL_BLEND);
        glEnable(GL11.GL_CULL_FACE);
    }

    public void update() {
        removeDeadParticles();
        emitParticles();

        for (int i = 0; i < _particles.size(); i++) {
            Particle p = _particles.get(i);
            p.update();
        }
    }

    private void removeDeadParticles() {
        for (int i = _particles.size() - 1; i >= 0; i--) {
            Particle p = _particles.get(i);

            if (!p.isAlive() || _particles.size() > MAX_PARTICLES) {
                _particles.remove(i);
            }
        }
    }

    protected void emitParticles() {
        for (int i = 0; i < PARTICLES_PER_UPDATE && _particlesToEmit > 0; i++) {
            _particles.add(0, createParticle());
            _particlesToEmit--;
        }
    }

    public void setOrigin(Vector3d origin) {
        _origin.set(origin);
    }

    public void emitParticles(int amount) {
        _particlesToEmit = amount;
    }

    public WorldRenderer getParent() {
        return _parent;
    }

    protected abstract Particle createParticle();
}
