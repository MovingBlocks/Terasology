// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.SingleStepLoadProcess;

/**
 */
public class InitialiseEntitySystem extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseEntitySystem(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Entity System...";
    }

    @Override
    public boolean step() {
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
