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
package org.terasology.engine;

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
        return Thread.currentThread() == gameThread;
    }

    /**
     * Runs a process on the game thread, not waiting for it to run.
     * <br><br>
     * If the current thread is the game thread, then the process runs immediately
     *
     * @param process
     */
    public static void asynch(Runnable process) {
        if (Thread.currentThread() != gameThread) {
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
        if (Thread.currentThread() != gameThread) {
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
        if (Thread.currentThread() == gameThread) {
            List<Runnable> processes = Lists.newArrayList();
            pendingRunnables.drainTo(processes);
            processes.forEach(Runnable::run);
        }
    }

    /**
     * Removes all pending processess without running them
     */
    public static void clearWaitingProcesses() {
        if (gameThread == Thread.currentThread()) {
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
     * A process decorated allowing a thread to block until the process has been run.
     */
    private static class BlockingProcess implements Runnable {
        private Runnable internalProcess;
        private Semaphore semaphore = new Semaphore(0);

        public BlockingProcess(Runnable runnable) {
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
