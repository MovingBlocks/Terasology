// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.StepBasedLoadProcess;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.In;

import java.util.Iterator;
import java.util.List;

/**
 * Responsible for calling {@link ComponentSystem#preBegin()} on all registered systems.
 */
@ExpectedCost(1)
public class PreBeginSystems extends StepBasedLoadProcess {

    @In
    private ComponentSystemManager csm;

    private Iterator<ComponentSystem> componentSystems;


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
        final List<ComponentSystem> componentSystemList = csm.getAllSystems();
        componentSystems = componentSystemList.iterator();
        setTotalSteps(componentSystemList.size());
    }
}
