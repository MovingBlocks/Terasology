// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.registry.In;

/**
 *
 */
@ExpectedCost(1)
public class InitialiseEntitySystem extends SingleStepLoadProcess {

    @In
    private Context context;

    @Override
    public String getMessage() {
        return "Initialising Entity System...";
    }

    @Override
    public boolean step() {
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        return true;
    }
}
