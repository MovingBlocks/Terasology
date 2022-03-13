// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.StepBasedLoadProcess;
import org.terasology.engine.entitySystem.systems.ComponentSystem;

import java.util.Iterator;
import java.util.List;

/**
 * Responsible for calling {@link ComponentSystem#preBegin()} on all registered systems.
 */
public class PreBeginSystems extends StepBasedLoadProcess {

    private final Context context;

    private Iterator<ComponentSystem> componentSystems;

    public PreBeginSystems(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "${engine:menu#reticulating-splines}";
    }

    @Override
    public boolean step() {
        if (componentSystems.hasNext()) {
            componentSystems.next().preBegin();
            stepDone();
        }
        return !componentSystems.hasNext();
    }

    @Override
    public void begin() {
        ComponentSystemManager csm = context.get(ComponentSystemManager.class);
        final List<ComponentSystem> componentSystemList = csm.getAllSystems();
        componentSystems = componentSystemList.iterator();
        setTotalSteps(componentSystemList.size());
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
