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
import org.terasology.context.Context;
import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.CoreMessageType;

import java.net.URL;
import java.util.Map;

/**
 * The public interface to this package. Manages a communication session with the storage service server,
 * can answer for status information queries and can perform asynchronous operations on the storage service.
 * This class can be in two states, with the default being "logged out".
 */
public final class StorageServiceWorker {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceWorker.class);

    private StorageServiceWorkerStatus status = StorageServiceWorkerStatus.LOGGED_OUT_OK;

    private Config config;
    private IdentityStorageServiceConfig thisConfig;
    private SecurityConfig securityConfig;
    private Console console;

    private APISession sessionInstance;
    private String loginName;

    public StorageServiceWorker(Context context) {
        this.console = context.get(Console.class);
        this.config = context.get(Config.class);
        this.thisConfig = this.config.getIdentityStorageService();
        this.securityConfig = this.config.getSecurity();
    }

    private void logMessage(boolean warning, String message, Object... args) {
        console.addMessage("Identity storage service: " + String.format(message, args), warning ? CoreMessageType.ERROR : CoreMessageType.CONSOLE);
    }

    private boolean checkStatus(StorageServiceWorkerStatus requiredStatus, String action) {
        if (status != requiredStatus) {
            logMessage(true, "Action \"{}\" could not be performed (requires status {}, actual is {})",
                    action, requiredStatus.toString(), status.toString());
        }
        return true;
    }

    private boolean checkStatus(StorageServiceWorkerStatus altStatus1, StorageServiceWorkerStatus altStatus2, String action) {
        if (status != altStatus1 && status != altStatus2) {
            logMessage(true, "Action \"{}\" could not be performed (requires status {} or {}, actual is {})",
                    action, altStatus1.toString(), altStatus2.toString(), status.toString());
        }
        return true;
    }

    public StorageServiceWorkerStatus getStatus() {
        return status;
    }

    public String getLoginName() {
        return loginName;
    }

    /**
     * Tries to initialize the session using the parameters (host URL and session token) read from configuration.
     * The session token is verified against the server; if it's valid, the status is switched to logged in.
     */
    public void initializeFromConfig() {
        if (!checkStatus(StorageServiceWorkerStatus.LOGGED_OUT_OK, "initializeFromConfig")) {
            return;
        }
        if (thisConfig.isSet()) {
            status = StorageServiceWorkerStatus.LOGGING_IN;
            new Thread(() -> {
                try {
                    sessionInstance = new APISession(thisConfig.getServiceUrl(), thisConfig.getSessionToken());
                    loginName = sessionInstance.getLoginName();
                    status = StorageServiceWorkerStatus.LOGGED_IN_IDLE;
                    logMessage(false, "Successfully logged in using token stored in credentials");
                    syncIdentities();
                } catch (Exception e) {
                    sessionInstance = null;
                    status = StorageServiceWorkerStatus.LOGGED_OUT_ERROR;
                    logMessage(true, "Authentication from stored token and URL failed - %s", e.getMessage());
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
        if (!checkStatus(StorageServiceWorkerStatus.LOGGED_OUT_OK, StorageServiceWorkerStatus.LOGGED_OUT_ERROR, "login")) {
            return;
        }
        status = StorageServiceWorkerStatus.LOGGING_IN;
        new Thread(() -> {
            try {
                sessionInstance = APISession.createFromLogin(serviceURL, login, password);
                loginName = sessionInstance.getLoginName();
                thisConfig.setServiceURL(serviceURL);
                thisConfig.setSessionToken(sessionInstance.getSessionToken());
                config.save();
                status = StorageServiceWorkerStatus.LOGGED_IN_IDLE;
                logMessage(false, "Successfully logged in");
                syncIdentities();
            } catch (Exception e) {
                sessionInstance = null;
                status = StorageServiceWorkerStatus.LOGGED_OUT_ERROR;
                logMessage(true, "Login failed due to - {}", e.getMessage());
            }
        }).start();
    }

    /**
     * Destroys the current session and switches to the logged out status.
     */
    public void logout() {
        if (!checkStatus(StorageServiceWorkerStatus.LOGGED_IN_IDLE, "logout")) {
            return;
        }
        status = StorageServiceWorkerStatus.LOGGED_IN_WORKING;
        new Thread(() -> {
            try {
                sessionInstance.logout();
                sessionInstance = null;
                thisConfig.setSessionToken(null);
                status = StorageServiceWorkerStatus.LOGGED_OUT_OK;
                config.save();
                logMessage(false, "Successfully logged out");
            } catch (Exception e) {
                status = StorageServiceWorkerStatus.LOGGED_IN_IDLE;
                logMessage(true, "Logout failed - {} ", e.getMessage());
            }
        }).start();
    }

    /**
     * Uploads the specified identity certificate to the server.
     */
    public void putIdentity(PublicIdentityCertificate serverIdentity, ClientIdentity clientIdentity) {
        if (!checkStatus(StorageServiceWorkerStatus.LOGGED_IN_IDLE, "upload identity")) {
            return;
        }
        status = StorageServiceWorkerStatus.LOGGED_IN_WORKING;
        new Thread(() -> {
            try {
                sessionInstance.putIdentity(serverIdentity, clientIdentity);
                logMessage(false, "Successfully uploaded new identity");
            } catch (Exception e) {
                logMessage(true, "Failed to upload identity - {}", e.getMessage());
            }
            status = StorageServiceWorkerStatus.LOGGED_IN_IDLE;
        }).start();
    }

    /**
     * Performs a full synchronization of the locally stored identity certificates with the ones stored on the service.
     */
    public void syncIdentities() {
        if (!checkStatus(StorageServiceWorkerStatus.LOGGED_IN_IDLE, "sync identities")) {
            return;
        }
        status = StorageServiceWorkerStatus.LOGGED_IN_WORKING;
        new Thread(() -> {
            try {
                Map<PublicIdentityCertificate, ClientIdentity> local = securityConfig.getAllIdentities();
                Map<PublicIdentityCertificate, ClientIdentity> remote = sessionInstance.getAllIdentities();
                MapDifference<PublicIdentityCertificate, ClientIdentity> diff = Maps.difference(local, remote);
                //upload the "only local" ones
                for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnLeft().entrySet()) {
                    if (entry.getValue().getPlayerPrivateCertificate() != null) { //TODO: find out why sometimes it's null
                        sessionInstance.putIdentity(entry.getKey(), entry.getValue());
                    }
                }
                //download the "only remote" ones
                for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: diff.entriesOnlyOnRight().entrySet()) {
                    securityConfig.addIdentity(entry.getKey(), entry.getValue());
                }
                config.save();
                logMessage(false, "Successfully synchronized identities");
            } catch (Exception e) {
                logMessage(true, "Failed to synchronize identities - ", e.getMessage());
            }
            status = StorageServiceWorkerStatus.LOGGED_IN_IDLE;
        }).start();
    }
}
