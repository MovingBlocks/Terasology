// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import java.net.URL;

/**
 * Used to keep track of storage service URL and session token.
 */
public class IdentityStorageServiceConfig {

    private URL serviceURL;
    private String sessionToken;

    public boolean isSet() {
        return serviceURL != null && sessionToken != null;
    }

    public URL getServiceUrl() {
        return serviceURL;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setServiceURL(URL serviceURL) {
        this.serviceURL = serviceURL;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

}
