// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common.hibernation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.EngineSubsystem;


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
