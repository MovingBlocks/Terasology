/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.module.ModuleManager;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
public class InitialiseEntitySystem extends SingleStepLoadProcess {
    @Override
    public String getMessage() {
        return "Initialising Entity System...";
    }

    @Override
    public boolean step() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        new EntitySystemBuilder().build(moduleManager.getEnvironment(), CoreRegistry.get(NetworkSystem.class),
                CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(CopyStrategyLibrary.class));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
