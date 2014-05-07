/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.entitySystem;

import org.junit.Before;
import org.mockito.Mockito;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

public abstract class AbstractFunctionalEntityTest {
    protected EventSystem eventSystem;
    protected NetworkSystem networkSystem;

    protected EntityManager entityManager;
    protected WorldProvider worldProvider;
    protected BlockEntityRegistry blockEntityRegistry;

    @Before
    public final void registerCommonInfrastructure() {
        entityManager = Mockito.mock(EntityManager.class);
        worldProvider = Mockito.mock(WorldProvider.class);
        blockEntityRegistry = Mockito.mock(BlockEntityRegistry.class);

        CoreRegistry.put(EntityManager.class, entityManager);
        CoreRegistry.put(WorldProvider.class, worldProvider);
        CoreRegistry.put(BlockEntityRegistry.class, blockEntityRegistry);
    }

    protected void setup(NetworkMode networkMode) {
        ReflectFactory reflectFactory = new ReflectionReflectFactory();
        CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
        EventLibrary eventLibrary = new EventLibrary(reflectFactory, copyStrategyLibrary);
        networkSystem = Mockito.mock(NetworkSystem.class);
        Mockito.when(networkSystem.getMode()).thenReturn(networkMode);
        eventSystem = new EventSystemImpl(eventLibrary, networkSystem);
    }

    protected void registerComponentSystem(ComponentSystem componentSystem) {
        InjectionHelper.inject(componentSystem);
        componentSystem.initialise();
        componentSystem.preBegin();
        componentSystem.postBegin();
        eventSystem.registerEventHandler(componentSystem);
    }

    public final void tearDown() {
        CoreRegistry.clear();
    }
}
