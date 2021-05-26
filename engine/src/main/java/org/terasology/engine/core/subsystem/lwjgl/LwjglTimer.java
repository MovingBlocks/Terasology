// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.internal.TimeLwjgl;
import org.terasology.engine.core.subsystem.common.TimeSubsystem;

public class LwjglTimer extends BaseLwjglSubsystem implements TimeSubsystem {

    private EngineTime time;

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public void preInitialise(Context context) {
        super.preInitialise(context);
        time = new TimeLwjgl();
        context.put(Time.class, time);
    }

    @Override
    public EngineTime getEngineTime() {
        return time;
    }
}
