// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;

public class InitialiseCommandSystem extends SingleStepLoadProcess {

    private Context context;

    public InitialiseCommandSystem(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Command System...";
    }

    @Override
    public boolean step() {
        context.put(Console.class, new ConsoleImpl(context));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
