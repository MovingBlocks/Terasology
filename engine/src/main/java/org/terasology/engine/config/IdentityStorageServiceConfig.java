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
