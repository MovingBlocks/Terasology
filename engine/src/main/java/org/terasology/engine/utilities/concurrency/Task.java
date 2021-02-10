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

package org.terasology.utilities.concurrency;

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
