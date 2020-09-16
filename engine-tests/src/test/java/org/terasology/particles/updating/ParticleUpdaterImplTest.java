/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.particles.updating;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.components.affectors.VelocityAffectorComponent;
import org.terasology.particles.components.generators.EnergyRangeGeneratorComponent;
import org.terasology.physics.Physics;
import org.terasology.physics.engine.PhysicsEngine;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ParticleUpdaterImpl}.
 */
public class ParticleUpdaterImplTest {

    private ParticleUpdater particleUpdater;

    @BeforeEach
    public void setUp() throws Exception {
        Physics physics = mock(PhysicsEngine.class);
        ModuleManager moduleManager = mock(ModuleManager.class);
        particleUpdater = new ParticleUpdaterImpl(physics, moduleManager);
    }

    @Test
    public void testNullEmitterRegistration() {
        Assertions.assertThrows(IllegalArgumentException.class,() -> {
            particleUpdater.addEmitter(null);
        });
    }

    @Test
    public void testNonEmitterRegistration() {
        EntityRef emitterEntity = mock(EntityRef.class);
        when(emitterEntity.getComponent(ParticleEmitterComponent.class)).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class,() -> {
            particleUpdater.addEmitter(emitterEntity);
        });
    }

    @Test
    public void testEmitterRegistration() {
        EntityRef emitterEntity = mock(EntityRef.class);
        when(emitterEntity.getComponent(ParticleEmitterComponent.class)).thenReturn(new ParticleEmitterComponent());

        particleUpdater.addEmitter(emitterEntity);
    }

    private Iterator<Component> getTestGeneratorsAndAffectors() {
        Collection<Component> components = new LinkedList<>();
        components.add(new EnergyRangeGeneratorComponent(0.5f, 1f));
        components.add(new VelocityAffectorComponent());

        return components.iterator();
    }

    @Test
    public void testEmitterConfiguration() {
        EntityRef emitterEntity = mock(EntityRef.class);
        Iterator<Component> componentIterator = getTestGeneratorsAndAffectors();
        when(emitterEntity.iterateComponents()).thenReturn(() -> componentIterator);

        ParticleEmitterComponent particleEmitterComponent = new ParticleEmitterComponent();
        particleEmitterComponent.ownerEntity = emitterEntity;
        when(emitterEntity.getComponent(ParticleEmitterComponent.class)).thenReturn(particleEmitterComponent);

        particleUpdater.addEmitter(emitterEntity);
        particleUpdater.configureEmitter(particleEmitterComponent);

        for (Component component : (Iterable<Component>) () -> componentIterator) {
            if (component.getClass() == EnergyRangeGeneratorComponent.class) {
                assertTrue(particleEmitterComponent.generatorFunctionMap.containsKey(component));
            } else if (component.getClass() == VelocityAffectorComponent.class) {
                assertTrue(particleEmitterComponent.affectorFunctionMap.containsKey(component));
            }
        }
    }
}
