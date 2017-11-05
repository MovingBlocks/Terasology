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

import java.util.Map;

/**
 */
final class PutIdentityAction implements Action {

    private final Map<PublicIdentityCertificate, ClientIdentity> identities;

    PutIdentityAction(Map<PublicIdentityCertificate, ClientIdentity> identities) {
        this.identities = identities;
    }

    @Override
    public void perform(StorageServiceWorker worker) {
        try {
            for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: identities.entrySet()) {
                worker.sessionInstance.putIdentity(entry.getKey(), entry.getValue());
            }
            if (!identities.isEmpty()) {
                worker.logMessage(false, "${engine:menu#storage-service-upload-ok}");
            }
        } catch (Exception e) {
            worker.logMessage(true, "${engine:menu#storage-service-upload-fail}", e.getMessage());
        }
        worker.status = StorageServiceWorkerStatus.LOGGED_IN;
    }
}
