// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.subsystem.common.TimeSubsystem;
import org.terasology.engine.core.subsystem.headless.device.TimeSystem;

public class HeadlessTimer implements TimeSubsystem {

    private EngineTime time;

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public void preInitialise(Context context) {
        initTimer(context);
    }

    private void initTimer(Context context) {
        time = new TimeSystem();
        context.put(Time.class, time);
    }

    @Override
    public EngineTime getEngineTime() {
        return time;
    }
}
