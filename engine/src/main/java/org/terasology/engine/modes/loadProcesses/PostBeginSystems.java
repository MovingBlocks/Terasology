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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.ComponentSystem;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PostBeginSystems extends StepBasedLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(PostBeginSystems.class);

    private final Context context;

    private Iterator<ComponentSystem> componentSystems;

    private ComponentSystem currentSystem;

    public PostBeginSystems(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "${engine:menu#post-initialise-systems}";
    }

    @Override
    public boolean step() {
        if (componentSystems.hasNext()) {
            try {
                currentSystem = componentSystems.next();
                currentSystem.postBegin();
            } catch (NoSuchElementException e) {
                logger.error("Failed to load system : '" + currentSystem.toString() + "'");
            }
        }
        return !componentSystems.hasNext();
    }

    @Override
    public void begin() {
        ComponentSystemManager csm = context.get(ComponentSystemManager.class);
        componentSystems = csm.iterateAll().iterator();
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
