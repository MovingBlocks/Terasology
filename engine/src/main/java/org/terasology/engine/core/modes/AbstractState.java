// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes;

import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
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
import org.terasology.gestalt.di.ServiceRegistry;

import static com.google.common.base.Verify.verifyNotNull;

public abstract class AbstractState implements GameState {
    protected Context context;
    protected EngineEntityManager entityManager;
    protected EventSystem eventSystem;
    protected ComponentSystemManager componentSystemManager;

    protected void registerEntityAndComponentManagers(boolean isHeadless, ServiceRegistry serviceRegistry) {
        verifyNotNull(context);
        CoreRegistry.setContext(context);

        // let's get the entity event system running
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context, serviceRegistry);

        serviceRegistry.with(Console.class).lifetime(Lifetime.Singleton).use(ConsoleImpl.class);
        if (!isHeadless) {
            serviceRegistry.with(NUIManager.class).lifetime(Lifetime.Singleton).use(NUIManagerInternal.class);
        }

        serviceRegistry.with(ComponentSystemManager.class).lifetime(Lifetime.Singleton).use(ComponentSystemManager.class);
    }

    protected void initEntityAndComponentManagers() {
        verifyNotNull(context);
        CoreRegistry.setContext(context);

        entityManager = context.get(EngineEntityManager.class);
        eventSystem = context.get(EventSystem.class);
        componentSystemManager = context.get(ComponentSystemManager.class);

        componentSystemManager.register(new ConsoleSystem(), "engine:ConsoleSystem");
        componentSystemManager.register(new CoreCommands(), "engine:CoreCommands");

        EntitySystemSetupUtil.registerEvents(eventSystem, context.get(ModuleManager.class).getEnvironment());
    }

    protected void registerLocalPlayer(ServiceRegistry serviceRegistry) {
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class),
                context.get(RecordAndReplayCurrentStatus.class));
        serviceRegistry.with(LocalPlayer.class).lifetime(Lifetime.Singleton).use(() -> localPlayer);
    }

    protected void createLocalPlayer() {
        EntityRef localPlayerEntity = entityManager.create(new ClientComponent());
        context.get(LocalPlayer.class).setClientEntity(localPlayerEntity);
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
