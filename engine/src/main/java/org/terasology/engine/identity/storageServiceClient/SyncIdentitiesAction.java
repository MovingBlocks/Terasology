// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PublicIdentityCertificate;

import java.util.Map;

/**
 */
final class SyncIdentitiesAction implements Action {

    @Override
    public void perform(StorageServiceWorker worker) {
        if (worker.hasConflictingIdentities()) {
            worker.logMessage(true, "${engine:menu#storage-service-sync-previous-conflicts}");
        } else {
            try {
                Map<PublicIdentityCertificate, ClientIdentity> local = worker.securityConfig.getAllIdentities();
                Map<PublicIdentityCertificate, ClientIdentity> remote = worker.sessionInstance.getAllIdentities();
                MapDifference<PublicIdentityCertificate, ClientIdentity> diff = Maps.difference(local, remote);
                //upload the "local only" ones
                for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry : diff.entriesOnlyOnLeft().entrySet()) {
                    if (entry.getValue().getPlayerPrivateCertificate() != null) { //TODO: find out why sometimes it's null
                        worker.sessionInstance.putIdentity(entry.getKey(), entry.getValue());
                    }
                }
                //download the "remote only" ones
                for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry : diff.entriesOnlyOnRight().entrySet()) {
                    worker.securityConfig.addIdentity(entry.getKey(), entry.getValue());
                }
                //keep track of the conflicting ones for manual resolution
                worker.resetConflicts();
                for (Map.Entry<PublicIdentityCertificate, MapDifference.ValueDifference<ClientIdentity>> entry : diff.entriesDiffering().entrySet()) {
                    worker.conflictingRemoteIdentities.addLast(new IdentityBundle(entry.getKey(), entry.getValue().rightValue()));
                }
                worker.saveConfig();
                worker.logMessage(false, "${engine:menu#storage-service-sync-ok}",
                        diff.entriesOnlyOnRight().size(), diff.entriesOnlyOnLeft().size(), diff.entriesDiffering().size());
                if (!diff.entriesDiffering().isEmpty()) {
                    worker.logMessage(true, "${engine:menu#storage-service-sync-conflicts}");
                }
            } catch (Exception e) {
                worker.logMessage(true, "${engine:menu#storage-service-sync-fail}", e.getMessage());
            }
        }
        worker.status = StorageServiceWorkerStatus.LOGGED_IN;
    }
}
