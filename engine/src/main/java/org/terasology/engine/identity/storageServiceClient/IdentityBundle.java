// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PrivateIdentityCertificate;
import org.terasology.engine.identity.PublicIdentityCertificate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an identity (a server public - client public - client private certificates triplet).
 */
class IdentityBundle {

    private PublicIdentityCertificate server;
    private PublicIdentityCertificate clientPublic;
    private PrivateIdentityCertificate clientPrivate;

    IdentityBundle(PublicIdentityCertificate server, PublicIdentityCertificate clientPublic, PrivateIdentityCertificate clientPrivate) {
        this.server = server;
        this.clientPublic = clientPublic;
        this.clientPrivate = clientPrivate;
    }

    IdentityBundle(PublicIdentityCertificate server, ClientIdentity client) {
        this(server, client.getPlayerPublicCertificate(), client.getPlayerPrivateCertificate());
    }

    public PublicIdentityCertificate getServer() {
        return server;
    }

    public ClientIdentity getClient() {
        return new ClientIdentity(clientPublic, clientPrivate);
    }

    public static Map<PublicIdentityCertificate, ClientIdentity> listToMap(List<IdentityBundle> ls) {
        Map<PublicIdentityCertificate, ClientIdentity> result = new HashMap<>();
        for (IdentityBundle item: ls) {
            result.put(item.server, new ClientIdentity(item.clientPublic, item.clientPrivate));
        }
        return result;
    }
}
