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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;

import java.util.Map;

/**
 */
final class SyncIdentitiesAction extends Action {

    @Override
    void perform(StorageServiceWorker worker) {
        try {
            if (worker.hasConflictingIdentities()) {
                throw new Exception("Conflicts were detected during the latest synchronization. " +
                        "It's not possible to start a new synchronization until the conflicts are solved.");
            }
            Map<PublicIdentityCertificate, ClientIdentity> local = worker.securityConfig.getAllIdentities();
            Map<PublicIdentityCertificate, ClientIdentity> remote = worker.sessionInstance.getAllIdentities();
            MapDifference<PublicIdentityCertificate, ClientIdentity> diff = Maps.difference(local, remote);
            //upload the "local only" ones
            for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnLeft().entrySet()) {
                if (entry.getValue().getPlayerPrivateCertificate() != null) { //TODO: find out why sometimes it's null
                    worker.sessionInstance.putIdentity(entry.getKey(), entry.getValue());
                }
            }
            //download the "remote only" ones
            for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnRight().entrySet()) {
                worker.securityConfig.addIdentity(entry.getKey(), entry.getValue());
            }
            //keep track of the conflicting ones for manual resolution
            for (Map.Entry<PublicIdentityCertificate, MapDifference.ValueDifference<ClientIdentity>> entry: diff.entriesDiffering().entrySet()) {
                worker.conflictingRemoteIdentities.put(entry.getKey(), entry.getValue().rightValue());
            }
            worker.saveConfig();
            String msg = String.format("Identity synchronization completed - %d downloaded, %d uploaded, %d conflicting",
                    diff.entriesOnlyOnLeft().size(), diff.entriesOnlyOnRight().size(), diff.entriesDiffering().size());
            worker.logMessage(false, msg);
            if (!diff.entriesDiffering().isEmpty()) {
                worker.logMessage(true, "WARNING: there were conflicting identities. Please click Join Game " +
                        "from the main menu to solve the conflicts.");
            }
        } catch (Exception e) {
            worker.logMessage(true, "Failed to synchronize identities - ", e.getMessage());
        }
        worker.status = StorageServiceWorkerStatus.LOGGED_IN;
    }
}
