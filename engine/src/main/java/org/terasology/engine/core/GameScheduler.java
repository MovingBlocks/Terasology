// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.terasology.gestalt.module.sandbox.API;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.security.AccessController;
import java.security.PrivilegedAction;

/** Schedulers to asynchronously run tasks on other threads. */
@API
public class GameScheduler {

    private static final Scheduler MAIN;

    static {
        MAIN = Schedulers.fromExecutor(runnable -> GameThread.asynch(runnable));
    }

    /**
     * A Scheduler to run tasks on the main thread.
     * <p>
     * <b>⚠</b> Use this only when necessary, as anything executed on the main thread will delay the core game loop.
     */
    public static Scheduler gameMain() {
        return MAIN;
    }

    /**
     * A Scheduler to run tasks off the main thread.
     * <p>
     * You can use this {@link Scheduler} with a
     * <ul>
     *     <li>{@link Runnable}, to run a function with no return value.
     *     <li>{@link Mono}, to run an operation one time, providing a future result.
     *     <li>{@link Flux}, to asynchronously generate a stream of events over time.
     * </ul>
     * <p>
     * You can expect this to always return the <em>same</em> scheduler; it does not create a new scheduler instance or thread
     * on every call.
     *
     * @return (singleton)
     * @see <a href="https://projectreactor.io/docs/core/release/reference/#core-features">Reactor Core Features</a>
     */
    public static Scheduler getScheduler() {
        return AccessController.doPrivileged((PrivilegedAction<Scheduler>) Schedulers::parallel);
    }
}
