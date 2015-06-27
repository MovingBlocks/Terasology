/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.engine.subsystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.utilities.concurrency.ShutdownTask;
import org.terasology.utilities.concurrency.Task;
import org.terasology.utilities.concurrency.TaskMaster;

import java.util.concurrent.RejectedExecutionException;

/**
 * @author Immortius
 */
public class ThreadManagerSubsystem implements EngineSubsystem, ThreadManager {

    private static final int MAX_NUMBER_THREADS = 16;
    private static final Logger logger = LoggerFactory.getLogger(ThreadManagerSubsystem.class);

    private final TaskMaster<Task> commonThreadPool = TaskMaster.createFIFOTaskMaster("common", MAX_NUMBER_THREADS);

    @Override
    public void submitTask(String name, Runnable task) {
        try {
            commonThreadPool.put(new Task() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    Thread.currentThread().setName("Engine-Task-Pool");
                    try (ThreadActivity ignored = ThreadMonitor.startThreadActivity(task.getClass().getSimpleName())) {
                        task.run();
                    } catch (RejectedExecutionException e) {
                        ThreadMonitor.addError(e);
                        logger.error("Thread submitted after shutdown requested: {}", name);
                    } catch (Throwable e) {
                        ThreadMonitor.addError(e);
                    }
                }

                @Override
                public boolean isTerminateSignal() {
                    return false;
                }
            });
        } catch (InterruptedException e) {
            logger.error("Failed to submit task {}, running on main thread", name, e);
            task.run();
        }
    }

    private void stopThreads() {
        commonThreadPool.shutdown(new ShutdownTask(), false);
    }

    private void restartThreads() {
        commonThreadPool.restart();
    }

    @Override
    public void preInitialise(Context context) {

    }

    @Override
    public void initialise(Context context) {

    }

    @Override
    public void registerCoreAssetTypes(ModuleAwareAssetTypeManager assetTypeManager) {

    }

    @Override
    public void postInitialise(Context context) {

    }

    @Override
    public void preUpdate(GameState currentState, float delta) {

    }

    @Override
    public void postUpdate(GameState currentState, float delta) {

    }

    @Override
    public void shutdown(Config config) {
        stopThreads();
    }

    @Override
    public void dispose() {
        commonThreadPool.shutdown(new ShutdownTask(), true);
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {

    }

}
