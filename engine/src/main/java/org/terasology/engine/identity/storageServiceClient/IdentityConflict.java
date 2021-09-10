// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
