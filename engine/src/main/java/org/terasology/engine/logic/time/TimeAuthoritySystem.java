// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.time;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;

@RegisterSystem
public class TimeAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final float TARGET_DELTA = 0.05f;
    private static final long CHECK_AUTOMATIC_TIME_DILATION_MS = 500;


    @In
    WorldProvider worldProvider;
    @In
    private Time time;

    private float lastGameTimeDilationValue;
    //TODO: persist this across server restarts
    private boolean enableAutomaticTimeDilation;
    private long lastCheckedAutomaticTimeDilation;

    @Override
    public void initialise() {
        lastGameTimeDilationValue = time.getGameTimeDilation();
    }

    @Override
    public void update(float delta) {
        if (enableAutomaticTimeDilation && lastCheckedAutomaticTimeDilation < time.getRealTimeInMs()) {
            lastCheckedAutomaticTimeDilation = time.getRealTimeInMs() + CHECK_AUTOMATIC_TIME_DILATION_MS;
            // attempt to maintain the target delta by slowing down time in general.
            float newGameTimeDilation = Math.max(0.1f, Math.min(1f, TARGET_DELTA / time.getGameDelta()));
            float deltaGameTimeDilation = newGameTimeDilation - time.getGameTimeDilation();
            // change time dilation in 0.1 increments up or down
            if (deltaGameTimeDilation > 0.1f) {
                time.setGameTimeDilation(time.getGameTimeDilation() + 0.1f);
            } else if (deltaGameTimeDilation < -0.1f) {
                time.setGameTimeDilation(time.getGameTimeDilation() - 0.1f);
            }
        }

        // send a resynch event if the authority values has changed
        if (lastGameTimeDilationValue != time.getGameTimeDilation()) {
            lastGameTimeDilationValue = time.getGameTimeDilation();
            worldProvider.getWorldEntity().send(new TimeResynchEvent(lastGameTimeDilationValue));
        }
    }

    @Command(shortDescription = "Toggle automatic time dilation", requiredPermission = PermissionManager.SERVER_MANAGEMENT_PERMISSION, runOnServer = true)
    public String toggleAutoTimeDilation() {
        enableAutomaticTimeDilation = !enableAutomaticTimeDilation;
        if (enableAutomaticTimeDilation) {
            return "Automatic Time Dilation enabled";
        } else {

            return "Automatic Time Dilation disabled";
        }
    }
}
