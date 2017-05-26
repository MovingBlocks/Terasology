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
final class InitializeFromTokenAction extends Action {

    @Override
    void perform(StorageServiceWorker worker) {
        if (worker.storageConfig.isSet()) {
            try {
                worker.sessionInstance = new APISession(worker.storageConfig.getServiceUrl(), worker.storageConfig.getSessionToken());
                worker.loginName = worker.sessionInstance.getLoginName();
                worker.status = StorageServiceWorkerStatus.LOGGED_IN;
                worker.logMessage(false, "Successfully logged in using token stored in credentials");
                worker.syncIdentities();
            } catch (Exception e) {
                worker.sessionInstance = null;
                worker.status = StorageServiceWorkerStatus.LOGGED_OUT;
                worker.logMessage(true, "Authentication from stored token and URL failed - %s", e.getMessage());
            }
        } else {
            worker.logMessage(false, "No configuration data is present, staying logged out");
            worker.status = StorageServiceWorkerStatus.LOGGED_OUT;
        }
    }
}
