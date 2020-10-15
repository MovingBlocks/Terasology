// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.headless;

import org.terasology.context.Context;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.common.TimeSubsystem;
import org.terasology.engine.subsystem.headless.device.TimeSystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

public class HeadlessTimer implements TimeSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    private EngineTime time;

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public void preInitialise(Context context) {
        time = classFactory.createToContext(TimeSystem.class, Time.class);
        context.put(EngineTime.class, time);
    }

    @Override
    public EngineTime getEngineTime() {
        return time;
    }
}
