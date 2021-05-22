// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network;

public interface JoinStatus {

    enum Status {
        IN_PROGRESS,
        COMPLETE,
        FAILED
    }

    Status getStatus();

    String getCurrentActivity();

    float getCurrentActivityProgress();

    String getErrorMessage();
}
