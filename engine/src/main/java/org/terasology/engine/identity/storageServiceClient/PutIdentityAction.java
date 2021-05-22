// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PublicIdentityCertificate;

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
