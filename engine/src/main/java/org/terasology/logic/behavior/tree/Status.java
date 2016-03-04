/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import org.terasology.module.sandbox.API;

/**
 * Status for a {@link Task}.
 */
@API
public enum Status {
    /**
     * A task which has never ticked
     */
    NOT_INITIALIZED,
    /**
     * Task finished with success but is not terminated
     */
    SUCCESS,
    /**
     * Task finished with a failure but not terminated
     */
    FAILURE,
    /**
     * Task is running
     */
    RUNNING,
    /**
     * Task is suspended
     */
    SUSPENDED,
    /**
     * Terminated task
     */
    TERMINATED
}
