// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.identity.storageServiceClient;

/**
 */
final class InitializeFromTokenAction implements Action {

    @Override
    public void perform(StorageServiceWorker worker) {
        if (worker.storageConfig.isSet()) {
            try {
                worker.sessionInstance = new APISession(worker.storageConfig.getServiceUrl(), worker.storageConfig.getSessionToken());
                worker.loginName = worker.sessionInstance.getLoginName();
                worker.status = StorageServiceWorkerStatus.LOGGED_IN;
                worker.logMessage(false, "${engine:menu#storage-service-token-ok}");
                worker.syncIdentities();
            } catch (Exception e) {
                worker.sessionInstance = null;
                worker.status = StorageServiceWorkerStatus.LOGGED_OUT;
                worker.logMessage(true, "${engine:menu#storage-service-token-fail}", e.getMessage());
            }
        } else {
            worker.status = StorageServiceWorkerStatus.LOGGED_OUT;
        }
    }
}
