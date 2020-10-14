// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.monitoring.gui.AdvancedMonitor;
import org.terasology.registry.In;

public class MonitoringSubsystem implements EngineSubsystem {

    @In
    private Config config;

    private AdvancedMonitor advancedMonitor;

    @Override
    public String getName() {
        return "Monitoring";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        if (config.getSystem().isMonitoringEnabled()) {
            advancedMonitor = new AdvancedMonitor();
            advancedMonitor.setVisible(true);
        }
    }

    @Override
    public void shutdown() {
        if (advancedMonitor != null) {
            advancedMonitor.close();
        }
    }
}
