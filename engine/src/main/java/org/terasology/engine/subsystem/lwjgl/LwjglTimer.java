// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.lwjgl;

import org.terasology.context.Context;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.internal.TimeLwjgl;
import org.terasology.engine.subsystem.common.TimeSubsystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class LwjglTimer extends BaseLwjglSubsystem implements TimeSubsystem {

    @In
    ContextAwareClassFactory classFactory;

    private EngineTime time;

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public void preInitialise(Context context) {
        super.preInitialise(context);
        time = (EngineTime) classFactory.createInjectableInstance(Time.class, TimeLwjgl.class);
    }

    @Override
    public EngineTime getEngineTime() {
        return time;
    }
}
