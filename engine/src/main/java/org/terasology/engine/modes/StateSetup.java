/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.engine.modes;

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.ConsoleImpl;
import org.terasology.logic.console.ConsoleSystem;
import org.terasology.logic.console.commands.CoreCommands;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;

/**
 * The class provides the basics for the setup stage for the game, where the game parameters are defined
 *
 * @author Benjamin Glatzel
 * @author Anton Kireev
 * @author Marcel Lehwald
 * @version 0.3
 */
public abstract class StateSetup implements GameState {
    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private ComponentSystemManager componentSystemManager;

    public StateSetup() {
    }

    @Override
    public void init(GameEngine gameEngine) {
        Context context = gameEngine.createChildContext();
        CoreRegistry.setContext(context);

        // let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);

        eventSystem = CoreRegistry.get(EventSystem.class);
        context.put(Console.class, new ConsoleImpl());

        NUIManager nuiManager = CoreRegistry.get(NUIManager.class);
        ((NUIManagerInternal) nuiManager).refreshWidgetsLibrary();

        componentSystemManager = new ComponentSystemManager();
        context.put(ComponentSystemManager.class, componentSystemManager);

        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");

        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = new LocalPlayer();
        context.put(LocalPlayer.class, localPlayer);
        localPlayer.setClientEntity(localPlayerEntity);

        componentSystemManager.initialise();
    }

    @Override
    public void dispose() {
        eventSystem.process();

        componentSystemManager.shutdown();

        entityManager.clear();
    }

    @Override
    public void handleInput(float delta) {
    }

    @Override
    public void update(float delta) {
        eventSystem.process();
    }

    @Override
    public void render() {
    }

    @Override
    public boolean isHibernationAllowed() {
        return true;
    }

}
