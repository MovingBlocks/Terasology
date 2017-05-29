/*
 * Copyright 2016 MovingBlocks
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

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.WorldGenerationSubscriberSystem;

import java.util.Iterator;
import java.util.LinkedList;

/**
 */
public class PostWorldGenerationSystems extends StepBasedLoadProcess {

    private final Context context;

    private Iterator<WorldGenerationSubscriberSystem> componentSystems;

    public PostWorldGenerationSystems(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Post-World Generation Systems";
    }

    @Override
    public boolean step() {
        if (componentSystems.hasNext()) {
            componentSystems.next().postWorldGeneration();
        }
        return !componentSystems.hasNext();
    }

    @Override
    public void begin() {
        ComponentSystemManager csm = context.get(ComponentSystemManager.class);
        LinkedList<WorldGenerationSubscriberSystem> systems = new LinkedList<>();
        for (ComponentSystem system : csm.iterateAll()) {
            if (system instanceof WorldGenerationSubscriberSystem) {
                systems.add((WorldGenerationSubscriberSystem) system);
            }
        }
        componentSystems = systems.iterator();
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
