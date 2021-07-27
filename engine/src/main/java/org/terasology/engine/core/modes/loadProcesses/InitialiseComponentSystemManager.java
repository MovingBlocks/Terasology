// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.SingleStepLoadProcess;

public class InitialiseComponentSystemManager extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseComponentSystemManager(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising component system...";
    }

    @Override
    public boolean step() {
        context.put(ComponentSystemManager.class, new ComponentSystemManager(context));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
