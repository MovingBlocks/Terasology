// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
