// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

final class LogoutAction implements Action {

    private boolean deleteLocalIdentities;

    LogoutAction(boolean deleteLocalIdentities) {
        this.deleteLocalIdentities = deleteLocalIdentities;
    }

    @Override
    public void perform(StorageServiceWorker worker) {
        try {
            worker.sessionInstance.logout();
            worker.sessionInstance = null;
            worker.storageConfig.setSessionToken(null);
            worker.status = StorageServiceWorkerStatus.LOGGED_OUT;
            worker.resetConflicts();
            if (deleteLocalIdentities) {
                worker.securityConfig.clearIdentities();
            }
            worker.saveConfig();
            worker.logMessage(false, "${engine:menu#storage-service-logout-ok}");
        } catch (Exception e) {
            worker.status = StorageServiceWorkerStatus.LOGGED_IN;
            worker.logMessage(true, "${engine:menu#storage-service-logout-fail}", e.getMessage());
        }
    }
}
