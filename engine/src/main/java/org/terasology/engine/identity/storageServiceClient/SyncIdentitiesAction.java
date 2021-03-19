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
