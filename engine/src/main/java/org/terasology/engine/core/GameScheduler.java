// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;
import org.terasology.engine.core.schedulers.ThreadAwareScheduler;
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
    private static ThreadAwareScheduler graphicsScheduler;

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

    public static Scheduler graphics() {
        if (graphicsScheduler == null) {
            throw new IllegalStateException("You should setup graphic scheduler firsts. Use `GameScheduler.setupGraphicsScheduler`");
        }
        return graphicsScheduler;
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

    /**
     * Wraps {@link Mono} with activity monitor.
     * @param name activity name.
     * @param mono {@link Mono} to wraps.
     * @param <T> {@link Mono}'s value type.
     * @return Wrapped Mono
     */
    public static <T> Mono<T> wrapActivity(String name, Mono<T> mono) {
        return Mono.using(
                () -> ThreadMonitor.startThreadActivity(name),
                activity -> mono,
                ThreadActivity::close
        );
    }

    /**
     * Wraps {@link Flux} with activity monitor.
     * @param name activity name.
     * @param mono {@link Flux} to wraps.
     * @param <T> {@link Flux}'s value type.
     * @return Wrapped Flux
     */
    public static <T> Flux<T> wrapActivity(Scheduler scheduler, String name, Flux<T> mono) {
        return Flux.using(
                () -> ThreadMonitor.startThreadActivity(name),
                activity -> mono,
                ThreadActivity::close
        );
    }

    /**
     * Run {@link Callable} in blocking manner at {@link this#graphics()} scheduler.
     * @param name activity name.
     * @param callable callable to run at {@link this#graphics()}.
     * @param <T> type of callable/return value
     * @return value after running {@link Callable}.
     */
    public static <T> T runBlockingGraphics(String name, Callable<T> callable) {
        Mono<T> mono = wrapActivity(name, Mono.fromCallable(callable));
        if (!graphicsScheduler.isSchedulerThread(Thread.currentThread())) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(graphicsScheduler);
        }
        return mono.block();
    }

    /**
     * Run {@link Runnable} in blocking manner at {@link this#graphics()} scheduler.
     * @param name activity name.
     * @param callable runnable to run at {@link this#graphics()}.
     * @return value after running {@link Runnable}.
     */
    public static void runBlockingGraphics(String name, Runnable callable) {
        Mono<?> mono = wrapActivity(name, Mono.fromRunnable(callable));
        if (!graphicsScheduler.isSchedulerThread(Thread.currentThread())) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(graphicsScheduler);
        }
        mono.block();
    }

    /**
     * Runs {@link Runnable} at {@link this#graphics()}. non-blocking.
     * @param name activity name.
     * @param runnable {@link Runnable} to run.
     * @return {@link Disposable} from Mono subscriber.
     */
    public static Disposable runOnGraphics(String name, Runnable runnable) {
        Mono<?> mono = wrapActivity(name, Mono.fromRunnable(runnable));
        if (!graphicsScheduler.isSchedulerThread(Thread.currentThread())) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(graphicsScheduler);
        }
        return mono.subscribe();
    }

    /**
     * Runs {@link Callable} at {@link this#graphics()}. non-blocking.
     * @param name activity name.
     * @param callable {@link Callable} to run.
     * @param <T> type of callable.
     * @return {@link Disposable} from Mono subscriber.
     */
    // TODO idk what to with returned value..
    public static <T> Disposable runOnGraphics(String name, Callable<T> callable) {
        Mono<T> mono = wrapActivity(name, Mono.fromCallable(callable));
        if (!graphicsScheduler.isSchedulerThread(Thread.currentThread())) {
            mono = mono
                    .doOnSubscribe(s-> GLFW.glfwMakeContextCurrent(LwjglGraphics.windowId))
                    .doFinally(f -> GLFW.glfwMakeContextCurrent(MemoryUtil.NULL))
                    .subscribeOn(graphicsScheduler);
        }
        return mono.subscribe();
    }

    /**
     * Setups graphics scheduler.
     * @param scheduler new graphics scheduler.
     * @return scheduler from params.
     */
    public static Scheduler setupGraphicsScheduler(ThreadAwareScheduler scheduler) {
        if (graphicsScheduler != null) {
            throw new IllegalStateException("Graphics scheduler is setup already");
        }
        graphicsScheduler = scheduler;
        return scheduler;
    }
}
