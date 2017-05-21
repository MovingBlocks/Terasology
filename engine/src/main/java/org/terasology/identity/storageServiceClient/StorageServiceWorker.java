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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.IdentityStorageServiceConfig;
import org.terasology.config.SecurityConfig;
import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;

import java.net.URL;
import java.util.Map;

/**
 * The public interface to this package. Manages a communication session with the storage service server,
 * can answer for status information queries and can perform asynchronous operations on the storage service.
 * This class can be in two states, with the default being "logged out".
 */
public final class StorageServiceWorker {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceWorker.class);

    private IdentityStorageServiceConfig config;
    private SecurityConfig securityConfig;
    private APISession sessionInstance;
    private String loginName;

    public StorageServiceWorker(Config config) {
        this.config = config.getIdentityStorageService();
        this.securityConfig = config.getSecurity();
    }

    public boolean isLoggedIn() {
        return sessionInstance != null;
    }

    public String getLoginName() {
        return loginName;
    }

    /**
     * Tries to initialize the session using the parameters (host URL and session token) read from configuration.
     * The session token is verified against the server; if it's valid, the status is switched to logged in.
     */
    public void initializeFromConfig() {
        if (config.isSet()) {
            new Thread(() -> {
                try {
                    sessionInstance = new APISession(config.getServiceUrl(), config.getSessionToken());
                    loginName = sessionInstance.getLoginName();
                } catch (Exception e) {
                    logger.warn("Authentication from stored token and URL failed", e);
                }
            }).start();
        } else {
            logger.info("No configuration data is present, staying logged out");
        }
    }

    /**
     * Tries to login with the specified credentials; on success, the status is switched to logged in
     * and the parameters are stored in configuration.
     */
    public void login(URL serviceURL, String login, String password) {
        new Thread(() -> {
            try {
                sessionInstance = APISession.createFromLogin(serviceURL, login, password);
                loginName = sessionInstance.getLoginName();
                config.setServiceURL(serviceURL);
                config.setSessionToken(sessionInstance.getSessionToken());
            } catch (Exception e) {
                logger.warn("Login failed", e);
            }
        }).start();
    }

    /**
     * Destroys the current session and switches to the logged out status.
     */
    public void logout() {
        new Thread(() -> {
            try {
                sessionInstance.logout();
                sessionInstance = null;
                config.setSessionToken(null);
            } catch (Exception e) {
                logger.warn("Logout failed", e);
            }
        }).start();
    }

    /**
     * Uploads the specified identity certificate to the server.
     */
    public void putIdentity(PublicIdentityCertificate serverIdentity, ClientIdentity clientIdentity) {
        new Thread(() -> {
            try {
                sessionInstance.putIdentity(serverIdentity, clientIdentity);
            } catch (Exception e) {
                logger.warn("Failed to upload identity", e);
            }
        }).start();
    }

    /**
     * Performs a full synchronization of the locally stored identity certificates with the ones stored on the service.
     */
    public void syncIdentities() {
        new Thread(() -> {
            try {
                Map<PublicIdentityCertificate, ClientIdentity> local = securityConfig.getAllIdentities();
                Map<PublicIdentityCertificate, ClientIdentity> remote = sessionInstance.getAllIdentities();
                MapDifference<PublicIdentityCertificate, ClientIdentity> diff = Maps.difference(local, remote);
                //upload the "only local" ones
                for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnLeft().entrySet()) {
                    sessionInstance.putIdentity(entry.getKey(), entry.getValue());
                }
                //download the "only remote" ones
                for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnRight().entrySet()) {
                    securityConfig.addIdentity(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                logger.warn("Identity certificate synchronization failed", e);
            }
        }).start();
    }
}
