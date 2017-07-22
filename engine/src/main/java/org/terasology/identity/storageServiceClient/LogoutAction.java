/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.identity.storageServiceClient;

/**
 */
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
