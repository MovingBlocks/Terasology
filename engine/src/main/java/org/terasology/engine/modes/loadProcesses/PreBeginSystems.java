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
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.CoreRegistry;

import java.util.Iterator;

/**
 * @author Immortius
 */
public class PreBeginSystems extends StepBasedLoadProcess {

    private Iterator<ComponentSystem> componentSystems;

    @Override
    public String getMessage() {
        return "Reticulating Splines";
    }

    @Override
    public boolean step() {
        if (componentSystems.hasNext()) {
            componentSystems.next().preBegin();
        }
        return !componentSystems.hasNext();
    }

    @Override
    public void begin() {
        ComponentSystemManager csm = CoreRegistry.get(ComponentSystemManager.class);
        componentSystems = csm.iterateAll().iterator();
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
