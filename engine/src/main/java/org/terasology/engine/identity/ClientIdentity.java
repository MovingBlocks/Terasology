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
package org.terasology.identity;

import org.terasology.config.SecurityConfig;

import java.util.Objects;

public class ClientIdentity {


    private PublicIdentityCertificate playerPublicCertificate;
    private PrivateIdentityCertificate playerPrivateCertificate;

    /**
     * @param publicCert  The public certificate to be passed
     * @param privateCert The private certificate to be passed
     */
    public ClientIdentity(PublicIdentityCertificate publicCert, PrivateIdentityCertificate privateCert) {
        this.playerPublicCertificate = publicCert;
        this.playerPrivateCertificate = privateCert;
    }

    /**
     * @return A PublicIdentityCertificate belonging to the player
     */
    public PublicIdentityCertificate getPlayerPublicCertificate() {
        return playerPublicCertificate;
    }

    /**
     * @return A PrivateIndentityCertificate belonging to the player, if possible (exists and is allowed)
     */
    public PrivateIdentityCertificate getPlayerPrivateCertificate() {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(SecurityConfig.PRIVATE_CERTIFICATE_ACCESS_PERMISSION);
        }
        return playerPrivateCertificate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerPublicCertificate, playerPrivateCertificate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClientIdentity) {
            ClientIdentity other = (ClientIdentity) obj;
            return Objects.equals(playerPublicCertificate, other.playerPublicCertificate) && Objects.equals(playerPrivateCertificate, other.playerPrivateCertificate);
        }
        return false;
    }
}
