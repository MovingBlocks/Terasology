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

import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PublicIdentityCertificate;

public final class IdentityConflict {

    private final PublicIdentityCertificate server;
    private final ClientIdentity localClientIdentity;
    private final ClientIdentity remoteClientIdentity;

    public IdentityConflict(PublicIdentityCertificate server, ClientIdentity localClientIdentity, ClientIdentity remoteClientIdentity) {
        this.server = server;
        this.localClientIdentity = localClientIdentity;
        this.remoteClientIdentity = remoteClientIdentity;
    }

    public PublicIdentityCertificate getServer() {
        return server;
    }

    public ClientIdentity getLocalClientIdentity() {
        return localClientIdentity;
    }

    public ClientIdentity getRemoteClientIdentity() {
        return remoteClientIdentity;
    }

    public String getServerId() {
        return server.getId();
    }

    public String getLocalId() {
        return localClientIdentity.getPlayerPublicCertificate().getId();
    }

    public String getRemoteId() {
        return remoteClientIdentity.getPlayerPublicCertificate().getId();
    }
}
