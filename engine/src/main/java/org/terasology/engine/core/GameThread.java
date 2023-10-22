// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Semaphore;

/**
 * Information and access to the GameThread - the main thread of Terasology. Certain updates can only occur on the GameThread:
 * <ul>
 * <li>World changes</li>
 * <li>Entity changes</li>
 * <li>Some asset acquisition (if it involves a Display or Audio context)</li>
 * </ul>
 *
 */
public final class GameThread {

    private static volatile Thread gameThread;
    private static BlockingDeque<Runnable> pendingRunnables = Queues.newLinkedBlockingDeque();

    private GameThread() {
    }

    /**
     * @return Whether the currentThread is the gameThread.
     */
    public static boolean isCurrentThread() {
        return Thread.currentThread().equals(gameThread);
    }

    /**
     * Runs a process on the game thread, not waiting for it to run.
     * <br><br>
     * If the current thread is the game thread, then the process runs immediately
     *
     * @param process
     */
    public static void asynch(Runnable process) {
        if (!Thread.currentThread().equals(gameThread)) {
            pendingRunnables.push(process);
        } else {
            process.run();
        }
    }

    /**
     * Runs a process on the game thread, waiting for it to run (the current thread is blocked).
     * <br><br>
     * If the current thread is the game thread, then the process runs immediately
     *
     * @param process
     */
    public static void synch(Runnable process) throws InterruptedException {
        if (!Thread.currentThread().equals(gameThread)) {
            BlockingProcess blockingProcess = new BlockingProcess(process);
            pendingRunnables.push(blockingProcess);
            blockingProcess.waitForCompletion();
        } else {
            process.run();
        }
    }

    /**
     * Runs all pending processes submitted from other threads
     */
    public static void processWaitingProcesses() {
        if (Thread.currentThread().equals(gameThread)) {
            List<Runnable> processes = Lists.newArrayList();
            pendingRunnables.drainTo(processes);
            processes.forEach(Runnable::run);
        }
    }

    /**
     * Removes all pending processes without running them
     */
    public static void clearWaitingProcesses() {
        if (gameThread.equals(Thread.currentThread())) {
            pendingRunnables.clear();
        }
    }

    /**
     * Sets the game thread. This can only be done once.
     */
    public static void setToCurrentThread() {
        if (gameThread == null) {
            gameThread = Thread.currentThread();
        }
    }

    /**
     * Sets the game thread to null. Should called after a test that calls engine.initialise() is finished.
     */
    public static void reset() {
        gameThread = null;
    }

    /**
     * A process decorated allowing a thread to block until the process has been run.
     */
    private static class BlockingProcess implements Runnable {
        private Runnable internalProcess;
        private Semaphore semaphore = new Semaphore(0);

        BlockingProcess(Runnable runnable) {
            this.internalProcess = runnable;
        }

        @Override
        public void run() {
            internalProcess.run();
            semaphore.release();
        }

        public void waitForCompletion() throws InterruptedException {
            semaphore.acquire();
        }
    }
}
