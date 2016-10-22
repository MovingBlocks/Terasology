/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag.tasks;

import org.terasology.rendering.dag.RenderPipelineTask;

/**
 * Instances of this class are intended to be inserted in the Render Task List.
 *
 * If the content of the task list is printed out by the logger, instances of this class
 * visually separate the tasks releated to a node from those of the previous one.
 */
public class MarkerTask implements RenderPipelineTask {

    private String message;

    /**
     * Instantiate a MarkerTask.
     *
     * @param message A string used by the toString() method.
     */
    public MarkerTask(String message) {
        this.message = message;
    }

    @Override
    public void execute() { }

    /**
     * Returns a string description of the instance.
     *
     * @return A string in the form: "----- <message>",
     *         where <message> is the string passed to the constructor.
     */
    public String toString() {
        return String.format("----- %s", message);
    }

}
