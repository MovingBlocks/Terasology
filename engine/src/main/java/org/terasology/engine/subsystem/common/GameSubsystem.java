// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.game.Game;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;

/**
 *
 */
// TODO: Get rid of this subsystem, it is kind of silly (remove Game class, convert to entity?)
public class GameSubsystem implements EngineSubsystem {

    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getName() {
        return "Game";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        classFactory.createInjectableInstance(Game.class);
    }
}
