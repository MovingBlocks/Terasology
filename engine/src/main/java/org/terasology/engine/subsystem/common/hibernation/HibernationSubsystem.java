// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common.hibernation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
public class HibernationSubsystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(HibernationSubsystem.class);

    @In
    private DisplayDevice displayDevice;

    @In
    private ContextAwareClassFactory classFactory;

    private HibernationManager hibernationManager;

    @Override
    public String getName() {
        return "Hibernation";
    }

    @Override
    public void preInitialise(Context rootContext) {
        hibernationManager = classFactory.createToContext(HibernationManager.class);
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
