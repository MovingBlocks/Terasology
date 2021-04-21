// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;


import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;

/**
 */
public class InitialiseGraphics extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseGraphics(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Graphics";
    }

    @Override
    public boolean step() {
        // Refresh widget library after modules got laoded:
        NUIManager nuiManager = context.get(NUIManager.class);
        ((NUIManagerInternal) nuiManager).refreshWidgetsLibrary();

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
