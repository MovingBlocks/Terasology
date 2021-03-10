/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.engine.core.modes;

/**
 * Used for LoadProcesses taking more than one step.
 */
public abstract class StepBasedLoadProcess implements LoadProcess {

    private int stepsComplete;
    private int totalSteps = 1;

    /**
     * Must be called after every step counted in {@link #setTotalSteps(int)} is finished.
     *
     * The preferable way to do this is from the end of the {@link #step()} method.
     */
    protected void stepDone() {
        stepsComplete++;
    }

    /**
     * Set the total amount of steps load process is going to take.
     *
     * @param amount The expected amount of steps.
     */
    protected void setTotalSteps(int amount) {
        this.totalSteps = Math.max(1, amount);
    }

    @Override
    public final float getProgress() {
        return (float) Math.min(totalSteps, stepsComplete) / totalSteps;
    }
}
