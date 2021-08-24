// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphics;
import org.terasology.engine.monitoring.ThreadActivity;
import org.terasology.engine.monitoring.ThreadMonitor;
import org.terasology.gestalt.module.sandbox.API;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.Callable;

/** Schedulers to asynchronously run tasks on other threads. */
@API
public class GameScheduler {

    private static final Scheduler MAIN;
    private static ThreadCaptureScheduler GRAPHICS;

    static {
        MAIN = Schedulers.fromExecutor(runnable -> GameThread.asynch(runnable));
        GRAPHICS = new ThreadCaptureScheduler();
    }

    /**
     * A Scheduler to run tasks on the main thread.
     * <p>
     * <b>⚠</b> Use this only when necessary, as anything executed on the main thread will delay the core game loop.
     */
    public static Scheduler gameMain() {
        return MAIN;
    }

    public static Scheduler graphics() {
        return GRAPHICS;
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
    public static Scheduler parallel() {
        return AccessController.doPrivileged((PrivilegedAction<Scheduler>) Schedulers::parallel);
    }

    /**
     * Run a task asynchronously, named for monitoring.
     * <p>
     * The task will be run on the {@link #parallel()} scheduler.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Disposable scheduleParallel(String name, Runnable task) {
        return wrapActivity(name, Mono.fromRunnable(task))
                .subscribeOn(parallel())
                .subscribe();
    }

    public static <T> Mono<T> wrapActivity(String name, Mono<T> mono) {
        return Mono.using(
                () -> ThreadMonitor.startThreadActivity(name),
                activity -> mono,
                ThreadActivity::close
        );
    }

    public static <T> Flux<T> wrapActivity(Scheduler scheduler, String name, Flux<T> mono) {
        return Flux.using(
                () -> ThreadMonitor.startThreadActivity(name),
                activity -> mono,
                ThreadActivity::close
        );
    }

    public static <T> T runBlockingGraphics(String name, Callable<T> callable) {
        Mono<T> mono = wrapActivity(name, Mono.fromCallable(callable));
        if (GRAPHICS.getCapturedThread() != Thread.currentThread()) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(GRAPHICS);
        }
        return mono .block();
    }

    public static void runBlockingGraphics(String name, Runnable callable) {
        Mono<?> mono = wrapActivity(name, Mono.fromRunnable(callable));
        if (GRAPHICS.getCapturedThread() != Thread.currentThread()) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(GRAPHICS);
        }
        mono.block();
    }

    public static Disposable runOnGraphics(String name, Runnable callable) {
        Mono<?> mono = wrapActivity(name, Mono.fromRunnable(callable));
        if (GRAPHICS.getCapturedThread() != Thread.currentThread()) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(GRAPHICS);
        }
        return mono.subscribe();
    }

    // TODO idk what to with returned value..
    public static <T> Disposable runOnGraphics(String name, Callable<T> callable) {
        Mono<T> mono = wrapActivity(name, Mono.fromCallable(callable));
        if (GRAPHICS.getCapturedThread() != Thread.currentThread()) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(GRAPHICS);
        }
        return mono.subscribe();
    }
}
