// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

public enum StorageServiceWorkerStatus {
    LOGGED_OUT(true, "${engine:menu#storage-service-logged-out}", "${engine:menu#storage-service-log-in}"),
    LOGGED_IN(true, "${engine:menu#storage-service-logged-in}", "${engine:menu#storage-service-log-out}"),
    WORKING(false, "${engine:menu#storage-service-wait}", "");

    final String statusMessageId;
    final String buttonMessageId;
    private final boolean buttonEnabled;

    StorageServiceWorkerStatus(boolean buttonEnabled, String statusMessageId, String buttonMessageId) {
        this.buttonEnabled = buttonEnabled;
        this.statusMessageId = statusMessageId;
        this.buttonMessageId = buttonMessageId;
    }

    public boolean isButtonEnabled() {
        return buttonEnabled;
    }
}
