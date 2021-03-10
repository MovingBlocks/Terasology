// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.concurrency;

/**
 * Task interface for use with {@link TaskMaster}. Typically, this interface is implemented by a BaseTask class
 * specific to your TaskMaster client code. For example, a pathfinder might create a PathfindingBaseTask, and
 * extend that to make a PathfindingMapAnalysisTask.
 * <p>
 * The {@link #run()} method is called (generally on a separate task processing thread) when the task manager is
 * ready to execute the task.
 *
 * @see TaskMaster
 */
public interface Task {

    String getName();

    /**
     * Called when the Task is ready to be executed
     */
    void run();

    /**
     * Return true if the {@link TaskProcessor} should shut down after performing this task
     * @return
     */
    boolean isTerminateSignal();
}
