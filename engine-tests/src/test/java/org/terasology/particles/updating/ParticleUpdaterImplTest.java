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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.particles.components.affectors.VelocityAffectorComponent;
import org.terasology.particles.components.generators.EnergyRangeGeneratorComponent;
import org.terasology.particles.functions.affectors.AffectorFunction;
import org.terasology.particles.functions.affectors.VelocityAffectorFunction;
import org.terasology.particles.functions.generators.EnergyRangeGeneratorFunction;
import org.terasology.particles.functions.generators.GeneratorFunction;
import org.terasology.physics.Physics;
import org.terasology.physics.engine.PhysicsEngine;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ParticleUpdaterImpl}.
 */
public class ParticleUpdaterImplTest {

    private ParticleUpdater particleUpdater;
    private BiMap<Class<Component>, GeneratorFunction> registeredGeneratorFunctions;
    private BiMap<Class<Component>, AffectorFunction> registeredAffectorFunctions;

    @Before
    public void setUp() throws Exception {
        Physics physics = mock(PhysicsEngine.class);
        particleUpdater = new ParticleUpdaterImpl(physics);
        registeredGeneratorFunctions = HashBiMap.create();
        registeredAffectorFunctions = HashBiMap.create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEmitterRegistration() {
        particleUpdater.register(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonEmitterRegistration() {
        EntityRef emitterEntity = mock(EntityRef.class);
        when(emitterEntity.getComponent(ParticleEmitterComponent.class)).thenReturn(null);

        particleUpdater.register(emitterEntity);
    }

    @Test
    public void testEmitterRegistration() {
        EntityRef emitterEntity = mock(EntityRef.class);
        when(emitterEntity.getComponent(ParticleEmitterComponent.class)).thenReturn(new ParticleEmitterComponent());

        particleUpdater.register(emitterEntity);
    }

    private Iterator<Component> getTestGeneratorsAndAffectors() {
        Collection<Component> components = new LinkedList<>();
        components.add(new EnergyRangeGeneratorComponent(0.5f, 1f));
        components.add(new VelocityAffectorComponent());

        EnergyRangeGeneratorFunction energyRangeGeneratorFunction = new EnergyRangeGeneratorFunction();
        registeredGeneratorFunctions.put(((GeneratorFunction) energyRangeGeneratorFunction).getComponentClass(), energyRangeGeneratorFunction);

        VelocityAffectorFunction velocityAffectorFunction = new VelocityAffectorFunction();
        registeredAffectorFunctions.put(((AffectorFunction) velocityAffectorFunction).getComponentClass(), velocityAffectorFunction);

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

        particleUpdater.register(emitterEntity);
        particleUpdater.configureEmitter(particleEmitterComponent, registeredAffectorFunctions, registeredGeneratorFunctions);

        for (Component component : (Iterable<Component>) () -> componentIterator) {
            if (registeredGeneratorFunctions.containsKey(component.getClass())) {
                assertTrue(particleEmitterComponent.generatorFunctionMap.containsKey(component));
            } else if (registeredGeneratorFunctions.containsKey(component.getClass())) {
                assertTrue(particleEmitterComponent.generatorFunctionMap.containsKey(component));
            }
        }
    }
}
