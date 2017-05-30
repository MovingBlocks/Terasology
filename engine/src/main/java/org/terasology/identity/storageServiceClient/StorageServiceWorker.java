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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.IdentityStorageServiceConfig;
import org.terasology.config.SecurityConfig;
import org.terasology.context.Context;
import org.terasology.i18n.TranslationSystem;
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

    StorageServiceWorkerStatus status = StorageServiceWorkerStatus.LOGGED_OUT;
    APISession sessionInstance;
    String loginName;
    Map<PublicIdentityCertificate, ClientIdentity> conflictingRemoteIdentities;

    final IdentityStorageServiceConfig storageConfig;
    final SecurityConfig securityConfig;

    private final Console console;
    private final Config config;
    private final TranslationSystem translationSystem;

    public StorageServiceWorker(Context context) {
        this.console = context.get(Console.class);
        this.config = context.get(Config.class);
        this.storageConfig = this.config.getIdentityStorageService();
        this.securityConfig = this.config.getSecurity();
        this.translationSystem = context.get(TranslationSystem.class);
        this.conflictingRemoteIdentities = Maps.newHashMap();
    }

    void saveConfig() {
        config.save();
    }

    void logMessage(boolean warning, String messageId, Object... args) {
        String localizedMessage = "Identity storage service: " + translationSystem.translate(messageId);
        console.addMessage(String.format(localizedMessage, args), CoreMessageType.NOTIFICATION);
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

    private void putIdentities(Map<PublicIdentityCertificate, ClientIdentity> identities) {
        performAction(new PutIdentityAction(identities), StorageServiceWorkerStatus.LOGGED_IN);
    }

    /**
     * Uploads the specified identity certificate to the server.
     */
    public void putIdentity(PublicIdentityCertificate serverIdentity, ClientIdentity clientIdentity) {
        putIdentities(ImmutableMap.of(serverIdentity, clientIdentity));
    }

    /**
     * Performs a full synchronization of the locally stored identity certificates with the ones stored on the service.
     */
    public void syncIdentities() {
        performAction(new SyncIdentitiesAction(), StorageServiceWorkerStatus.LOGGED_IN);
    }

    /**
     * @return whether syncronization conflicts happened during the latest identity sync
     */
    public boolean hasConflictingIdentities() {
        return !conflictingRemoteIdentities.isEmpty();
    }

    /**
     * Solves the synchronization conflicts (if any) generated by the last synchronization.
     * @param solver the criteria to decide what action must be done for each conflicting identity.
     * @see IdentityConflictSolver
     */
    public void solveConflicts(IdentityConflictSolver solver) {
        Map<PublicIdentityCertificate, ClientIdentity> toUpload = Maps.newHashMap();
        for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry: conflictingRemoteIdentities.entrySet()) {
            PublicIdentityCertificate server = entry.getKey();
            ClientIdentity remote = entry.getValue();
            ClientIdentity local = securityConfig.getIdentity(server);
            switch (solver.solve(server, local, remote)) {
                case KEEP_LOCAL: //save for upload (remote will be overwritten)
                    toUpload.put(server, local);
                    break;
                case KEEP_REMOTE: //store the remote identity locally, overwriting the local one
                    securityConfig.addIdentity(server, remote);
                    break;
                case IGNORE:
                    //do nothing, keep remote on remote and local on local
            }
        }
        //reset conflicts
        conflictingRemoteIdentities.clear();
        //perform uploads (asynchronously)
        putIdentities(toUpload);
    }
}
