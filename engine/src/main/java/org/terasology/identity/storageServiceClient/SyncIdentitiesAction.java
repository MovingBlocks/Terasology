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
            Map<PublicIdentityCertificate, ClientIdentity> local = worker.securityConfig.getAllIdentities();
            Map<PublicIdentityCertificate, ClientIdentity> remote = worker.sessionInstance.getAllIdentities();
            MapDifference<PublicIdentityCertificate, ClientIdentity> diff = Maps.difference(local, remote);
            //upload the "only local" ones
            for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnLeft().entrySet()) {
                if (entry.getValue().getPlayerPrivateCertificate() != null) { //TODO: find out why sometimes it's null
                    worker.sessionInstance.putIdentity(entry.getKey(), entry.getValue());
                }
            }
            //download the "only remote" ones
            for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnRight().entrySet()) {
                worker.securityConfig.addIdentity(entry.getKey(), entry.getValue());
            }
            worker.saveConfig();
            worker.logMessage(false, "Successfully synchronized identities");
        } catch (Exception e) {
            worker.logMessage(true, "Failed to synchronize identities - ", e.getMessage());
        }
        worker.status = StorageServiceWorkerStatus.LOGGED_IN;
    }
}
