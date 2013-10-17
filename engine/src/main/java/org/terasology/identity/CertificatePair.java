/*
 * Copyright 2013 MovingBlocks
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

/**
 * Holds a public/private certificate pair
 */
public class CertificatePair {
    private PublicIdentityCertificate publicCert;
    private PrivateIdentityCertificate privateCert;

    public CertificatePair(PublicIdentityCertificate publicCert, PrivateIdentityCertificate privateCert) {
        this.privateCert = privateCert;
        this.publicCert = publicCert;
    }

    public PublicIdentityCertificate getPublicCert() {
        return publicCert;
    }

    public PrivateIdentityCertificate getPrivateCert() {
        return privateCert;
    }
}
