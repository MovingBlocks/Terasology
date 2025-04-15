// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.game.Game;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;


// TODO: Get rid of this subsystem, it is kind of silly (remove Game class, convert to entity?)
public class GameSubsystem implements EngineSubsystem {
    @Inject
    public GameSubsystem() {
    }

    @Override
    public String getName() {
        return "Game";
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        Game game = new Game();
        serviceRegistry.with(Game.class).lifetime(Lifetime.Singleton).use(() -> game);
    }
}
