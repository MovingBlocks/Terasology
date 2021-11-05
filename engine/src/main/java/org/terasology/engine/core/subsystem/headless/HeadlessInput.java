// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.EngineSubsystem;

public class HeadlessInput implements EngineSubsystem {

    @Override
    public String getName() {
        return "Input";
    }

    @Override
    public void postInitialise(Context context) {
        initControls(context);
    }

    private void initControls(Context context) {

    }

}
