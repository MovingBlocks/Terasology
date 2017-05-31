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
import org.terasology.identity.PrivateIdentityCertificate;
import org.terasology.identity.PublicIdentityCertificate;

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
