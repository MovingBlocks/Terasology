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

import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;

/**
 */
final class PutIdentityAction extends Action {

    private final PublicIdentityCertificate serverIdentity;
    private final ClientIdentity clientIdentity;

    PutIdentityAction(PublicIdentityCertificate serverIdentity, ClientIdentity clientIdentity) {
        this.serverIdentity = serverIdentity;
        this.clientIdentity = clientIdentity;
    }

    @Override
    void perform(StorageServiceWorker worker) {
        try {
            worker.sessionInstance.putIdentity(serverIdentity, clientIdentity);
            worker.logMessage(false, "Successfully uploaded new identity");
        } catch (Exception e) {
            worker.logMessage(true, "Failed to upload identity - %s", e.getMessage());
        }
        worker.status = StorageServiceWorkerStatus.LOGGED_IN;
    }
}
