// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.monitoring.gui.AdvancedMonitor;

public class MonitoringSubsystem implements EngineSubsystem {

    private AdvancedMonitor advancedMonitor;

    @Override
    public String getName() {
        return "Monitoring";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        if (rootContext.get(SystemConfig.class).monitoringEnabled.get()) {
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
