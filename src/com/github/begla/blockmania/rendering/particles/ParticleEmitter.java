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

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import com.github.begla.blockmania.world.main.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * Simple particle system.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ParticleEmitter implements RenderableObject {

    protected static final int MAX_PARTICLES = (Integer) ConfigurationManager.getInstance().getConfig().get("System.maxParticles");;
    protected static final int PARTICLES_PER_UPDATE = 16;
    /* ------- */
    protected int _particlesToEmit;

    protected ArrayList<Particle> _particles = new ArrayList();
    protected Vector3f _origin = new Vector3f();

    protected World _parent;

    public ParticleEmitter(World parent) {
        _parent = parent;
    }

    public void render() {
        glDisable(GL11.GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < _particles.size(); i++) {
            Particle p = _particles.get(i);
            p.render();
        }

        glDisable(GL_BLEND);
        glDisable(GL11.GL_TEXTURE_2D);
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

    public void setOrigin(Vector3f origin) {
        _origin.set(origin);
    }

    public void emitParticles(int amount) {
        _particlesToEmit = amount;
    }

    public World getParent() {
        return _parent;
    }

    protected abstract Particle createParticle();
}
