// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.subsystem.common.TimeSubsystem;
import org.terasology.engine.core.subsystem.headless.device.TimeSystem;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class HeadlessTimer implements TimeSubsystem {

    private EngineTime time;

    @Inject
    public HeadlessTimer() {
    }

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public void preInitialise(ServiceRegistry serviceRegistry) {
        initTimer(serviceRegistry);
    }

    private void initTimer(ServiceRegistry serviceRegistry) {
        time = new TimeSystem();
        serviceRegistry.with(Time.class).lifetime(Lifetime.Singleton).use(() -> time);
        serviceRegistry.with(EngineTime.class).lifetime(Lifetime.Singleton).use(() -> time);
    }

    @Override
    public EngineTime getEngineTime() {
        return time;
    }
}
