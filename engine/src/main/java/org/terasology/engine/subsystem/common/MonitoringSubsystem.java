/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.common;

import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.monitoring.gui.AdvancedMonitor;

/**
 *Class made MonitoringSubsystem.
 */
public class MonitoringSubsystem implements EngineSubsystem {
	/**
	 * Private field made advanceMonitor
	 */

    private AdvancedMonitor advancedMonitor;

    @Override
    /**
     * Method getName Override.
	 * Which returns String 
	 * @return "Monitoring"
     */
    public String getName() {
        return "Monitoring";
    }

    @Override
    /**
     * initialise Method Overrided 
     * with @parameters of (GameEngine engine, Context rootContext)
     */
    public void initialise(GameEngine engine, Context rootContext) {
    	/**
    	 * If Condition is true then set advancedMonitor (Visible). 
    	 */
        if (rootContext.get(Config.class).getSystem().isMonitoringEnabled()) {
            advancedMonitor = new AdvancedMonitor();
            advancedMonitor.setVisible(true);
        }
    }

    @Override
    /**
     * shutdown method Overrided 
     * if advanceMonitor is there then set advancedMonitor Visibilty false. 
     */
    public void shutdown() {
        if (advancedMonitor != null) {
            advancedMonitor.setVisible(false);
        }
    }
}
