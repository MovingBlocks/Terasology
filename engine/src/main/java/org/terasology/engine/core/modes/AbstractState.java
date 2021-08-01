// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.ConsoleImpl;
import org.terasology.engine.logic.console.ConsoleSystem;
import org.terasology.engine.logic.console.commands.CoreCommands;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;
import org.terasology.engine.rendering.nui.internal.TerasologyCanvasRenderer;
import org.terasology.nui.canvas.CanvasRenderer;

import static com.google.common.base.Verify.verifyNotNull;

public abstract class AbstractState implements GameState {
    protected Context context;
    protected EngineEntityManager entityManager;
    protected EventSystem eventSystem;
    protected ComponentSystemManager componentSystemManager;

    protected void initEntityAndComponentManagers() {
        verifyNotNull(context);
        CoreRegistry.setContext(context);

        // let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);

        eventSystem = context.get(EventSystem.class);
        context.put(Console.class, new ConsoleImpl(context));

        NUIManager nuiManager = new NUIManagerInternal((TerasologyCanvasRenderer) context.get(CanvasRenderer.class), context);
        context.put(NUIManager.class, nuiManager);

        componentSystemManager = new ComponentSystemManager(context);
        context.put(ComponentSystemManager.class, componentSystemManager);

        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");
    }

    protected static void createLocalPlayer(Context context) {
        EngineEntityManager entityManager = context.get(EngineEntityManager.class);
        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class),
                context.get(RecordAndReplayCurrentStatus.class));
        context.put(LocalPlayer.class, localPlayer);
        localPlayer.setClientEntity(localPlayerEntity);
    }

    @Override
    public void dispose(boolean shuttingDown) {
        // Apparently this can be disposed of before it is completely initialized? Probably only during
        // crashes, but crashing again during shutdown complicates the diagnosis.
        if (eventSystem != null) {
            eventSystem.process();
        }
        if (componentSystemManager != null) {
            componentSystemManager.shutdown();
        }
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    @Override
    public Context getContext() {
        return context;
    }
}
