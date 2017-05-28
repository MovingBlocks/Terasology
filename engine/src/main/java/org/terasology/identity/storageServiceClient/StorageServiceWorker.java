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

/**
 * The public interface to this package. Manages a communication session with the storage service server,
 * can answer for status information queries and can perform asynchronous operations on the storage service.
 * This class can be in two states, with the default being "logged out".
 */
public final class StorageServiceWorker {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceWorker.class);

    StorageServiceWorkerStatus status = StorageServiceWorkerStatus.LOGGED_OUT;
    APISession sessionInstance;
    String loginName;

    final IdentityStorageServiceConfig storageConfig;
    final SecurityConfig securityConfig;

    private final Console console;
    private final Config config;

    public StorageServiceWorker(Context context) {
        this.console = context.get(Console.class);
        this.config = context.get(Config.class);
        this.storageConfig = this.config.getIdentityStorageService();
        this.securityConfig = this.config.getSecurity();
    }

    void saveConfig() {
        config.save();
    }

    void logMessage(boolean warning, String message, Object... args) {
        console.addMessage("Identity storage service: " + String.format(message, args), CoreMessageType.NOTIFICATION);
    }

    private synchronized void performAction(Action action, StorageServiceWorkerStatus requiredStatus) {
        if (status != requiredStatus) {
            throw new RuntimeException("StorageServiceWorker is not in the required status");
        }
        status = StorageServiceWorkerStatus.WORKING;
        logger.info("Performing action {}", action.getClass().getSimpleName());
        new Thread(() -> {
            action.perform(this);
            logger.info("Completed action {}", action.getClass().getSimpleName());
        }).start();
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
        performAction(new InitializeFromTokenAction(), StorageServiceWorkerStatus.LOGGED_OUT);
    }

    /**
     * Tries to login with the specified credentials; on success, the status is switched to logged in
     * and the parameters are stored in configuration.
     */
    public void login(URL serviceURL, String login, String password) {
        performAction(new LoginAction(serviceURL, login, password), StorageServiceWorkerStatus.LOGGED_OUT);
    }

    /**
     * Destroys the current session and switches to the logged out status.
     */
    public void logout() {
        performAction(new LogoutAction(), StorageServiceWorkerStatus.LOGGED_IN);
    }

    /**
     * Uploads the specified identity certificate to the server.
     */
    public void putIdentity(PublicIdentityCertificate serverIdentity, ClientIdentity clientIdentity) {
        performAction(new PutIdentityAction(serverIdentity, clientIdentity), StorageServiceWorkerStatus.LOGGED_IN);
    }

    /**
     * Performs a full synchronization of the locally stored identity certificates with the ones stored on the service.
     */
    public void syncIdentities() {
        performAction(new SyncIdentitiesAction(), StorageServiceWorkerStatus.LOGGED_IN);
    }
}
