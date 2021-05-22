// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import com.google.common.util.concurrent.AtomicDouble;
import org.terasology.engine.network.JoinStatus;

/**
 */
public class JoinStatusImpl implements JoinStatus {
    private Status status = Status.IN_PROGRESS;
    private String currentActivity = "";
    private AtomicDouble currentProgress = new AtomicDouble(0);
    private String errorMessage = "";

    public JoinStatusImpl() {
    }

    /**
     * Function sets the Join status error message and sets the status to FAILED.
     * @param errorMessage
     */
    public JoinStatusImpl(String errorMessage) {
        this.errorMessage = errorMessage;
        status = Status.FAILED;
    }

    @Override
    public synchronized Status getStatus() {
        return status;
    }

    @Override
    public synchronized String getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Sets the current activity.
     * @param currentActivity
     */
    public synchronized void setCurrentActivity(String currentActivity) {
        this.currentActivity = currentActivity;
        currentProgress.set(0);
    }

    @Override
    public float getCurrentActivityProgress() {
        return (float) currentProgress.get();
    }

    public void setCurrentProgress(float currentProgress) {
        this.currentProgress.set(currentProgress);
    }

    @Override
    public synchronized String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the current error message.
     * @param errorMessage
     */
    public synchronized void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        status = Status.FAILED;
    }

    /**
     * Sets the Join Status as complete.
     */
    public synchronized void setComplete() {
        status = Status.COMPLETE;
    }
}
