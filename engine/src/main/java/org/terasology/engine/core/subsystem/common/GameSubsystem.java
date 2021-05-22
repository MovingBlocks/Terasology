// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.game.Game;

/**
 *
 */
// TODO: Get rid of this subsystem, it is kind of silly (remove Game class, convert to entity?)
public class GameSubsystem implements EngineSubsystem {
    @Override
    public String getName() {
        return "Game";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        rootContext.put(Game.class, new Game());
    }
}
