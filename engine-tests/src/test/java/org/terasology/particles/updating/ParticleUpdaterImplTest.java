// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.updating;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.particles.components.affectors.VelocityAffectorComponent;
import org.terasology.engine.particles.components.generators.EnergyRangeGeneratorComponent;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.gestalt.entitysystem.component.Component;

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
