// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.mode;

import org.terasology.engine.core.StateChangeSubscriber;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.modes.StateMainMenu;

/**
 * This listener checks whether the engine goes back to the main menu, which for a headless server signals the server
 * should be shut down. This happens mainly in cases where the loading process was not successful.
 */
public class HeadlessStateChangeListener implements StateChangeSubscriber {

    private final TerasologyEngine engine;

    public HeadlessStateChangeListener(TerasologyEngine engine) {
        this.engine = engine;
    }

    @Override
    public void onStateChange() {
        GameState state = engine.getState();
        if (state instanceof StateMainMenu) {
            engine.shutdown();
        }
    }

}
