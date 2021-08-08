// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.terasology.gestalt.module.sandbox.API;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.security.AccessController;
import java.security.PrivilegedAction;


@API
public class GameScheduler {

    private static final Scheduler MAIN;

    static {
        MAIN = Schedulers.fromExecutor(runnable -> GameThread.asynch(runnable));
    }

    public static Scheduler gameMain() {
        return MAIN;
    }

    public static Scheduler getScheduler() {
        return AccessController.doPrivileged((PrivilegedAction<Scheduler>) Schedulers::parallel);
    }
}
