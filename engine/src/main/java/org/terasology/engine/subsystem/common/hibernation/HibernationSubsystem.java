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
package org.terasology.engine.subsystem.common.hibernation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.EngineSubsystem;

/**
 *
 */
public class HibernationSubsystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(HibernationSubsystem.class);
    private HibernationManager hibernationManager;
    private DisplayDevice displayDevice;

    @Override
    public String getName() {
        return "Hibernation";
    }

    @Override
    public void preInitialise(Context rootContext) {
        hibernationManager = new HibernationManager();
        rootContext.put(HibernationManager.class, hibernationManager);
    }

    @Override
    public void postInitialise(Context rootContext) {
        displayDevice = rootContext.get(DisplayDevice.class);
    }

    @Override
    public void postUpdate(GameState currentState, float delta) {
        if (hibernationManager.isHibernating()) {
            if (!hibernationManager.isHibernationAllowed() || displayDevice.hasFocus()) {
                hibernationManager.setHibernating(false);
            }
        } else {
            if (hibernationManager.isHibernationAllowed() && !displayDevice.hasFocus()) {
                hibernationManager.setHibernating(true);
            }
        }

        if (hibernationManager.isHibernating()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.warn("Hibernation sleep interrupted", e);
            }
        }
    }
}
