// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.IdentityStorageServiceConfig;
import org.terasology.engine.config.SecurityConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PublicIdentityCertificate;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.CoreMessageType;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The public interface to this package. Manages a communication session with the storage service server, can answer for
 * status information queries and can perform asynchronous operations on the storage service. This class can be in two
 * states, with the default being "logged out".
 */
public final class StorageServiceWorker {

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceWorker.class);

    StorageServiceWorkerStatus status = StorageServiceWorkerStatus.LOGGED_OUT;
    APISession sessionInstance;
    String loginName;
    Deque<IdentityBundle> conflictingRemoteIdentities;

    final IdentityStorageServiceConfig storageConfig;
    final SecurityConfig securityConfig;

    private final Config config;
    private final TranslationSystem translationSystem;
    private final Deque<ConsoleNotification> notificationBuffer;
    private Map<PublicIdentityCertificate, ClientIdentity> conflictSolutionsToUpload;

    public StorageServiceWorker(Context context) {
        this.config = context.get(Config.class);
        this.storageConfig = this.config.getIdentityStorageService();
        this.securityConfig = this.config.getSecurity();
        this.translationSystem = context.get(TranslationSystem.class);
        this.notificationBuffer = new LinkedBlockingDeque<>();
        this.conflictingRemoteIdentities = new ArrayDeque<>();
    }

    void resetConflicts() {
        this.conflictingRemoteIdentities = new ArrayDeque<>();
        this.conflictSolutionsToUpload = Maps.newHashMap();
    }

    void saveConfig() {
        config.save();
    }

    void logMessage(boolean warning, String messageId, Object... args) {
        notificationBuffer.push(new ConsoleNotification(messageId, args));
    }

    private synchronized void performAction(Action action, StorageServiceWorkerStatus requiredStatus) {
        if (status != requiredStatus) {
            throw new RuntimeException("StorageServiceWorker is not in the required status");
        }
        status = StorageServiceWorkerStatus.WORKING;
        logger.atInfo().log("Performing action {}", action.getClass().getSimpleName());
        new Thread(() -> {
            action.perform(this);
            logger.atInfo().log("Completed action {}", action.getClass().getSimpleName());
        }).start();
    }

    public void flushNotificationsToConsole(Console target) {
        while (!notificationBuffer.isEmpty()) {
            ConsoleNotification notification = notificationBuffer.pop();
            String message =
                    translationSystem.translate("${engine:menu#storage-service}") + ": "
                            + String.format(translationSystem.translate(notification.messageId), notification.args);
            target.addMessage(message, CoreMessageType.NOTIFICATION);
        }
    }

    public StorageServiceWorkerStatus getStatus() {
        return status;
    }

    public String getLoginName() {
        return loginName;
    }

    /**
     * Tries to initialize the session using the parameters (host URL and session token) read from configuration. The
     * session token is verified against the server; if it's valid, the status is switched to logged in.
     */
    public void initializeFromConfig() {
        performAction(new InitializeFromTokenAction(), StorageServiceWorkerStatus.LOGGED_OUT);
    }

    /**
     * Tries to login with the specified credentials; on success, the status is switched to logged in and the parameters
     * are stored in configuration.
     */
    public void login(URL serviceURL, String login, String password) {
        performAction(new LoginAction(serviceURL, login, password), StorageServiceWorkerStatus.LOGGED_OUT);
    }

    /**
     * Destroys the current session and switches to the logged out status.
     *
     * @param deleteLocalIdentities whether the locally stored identities should be deleted or not.
     */
    public void logout(boolean deleteLocalIdentities) {
        performAction(new LogoutAction(deleteLocalIdentities), StorageServiceWorkerStatus.LOGGED_IN);
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
     * @return whether synchronization conflicts happened during the latest identity sync.
     */
    public boolean hasConflictingIdentities() {
        return !conflictingRemoteIdentities.isEmpty();
    }

    /**
     * @return details about one synchronization conflict.
     */
    public IdentityConflict getNextConflict() {
        IdentityBundle entry = conflictingRemoteIdentities.peekFirst();
        return new IdentityConflict(entry.getServer(), securityConfig.getIdentity(entry.getServer()),
                entry.getClient());
    }

    /**
     * @param solution the strategy to resolve the conflict returned by the latest call to {@link
     *         #getNextConflict()}. If there are no more conflicts and some of the solved conflicts must keep the local
     *         version, the uploads are performed asynchronously.
     */
    public void solveNextConflict(IdentityConflictSolution solution) {
        IdentityBundle entry = conflictingRemoteIdentities.removeFirst();
        PublicIdentityCertificate server = entry.getServer();
        ClientIdentity remote = entry.getClient();
        ClientIdentity local = securityConfig.getIdentity(server);
        switch (solution) {
            case KEEP_LOCAL: //save for upload (remote will be overwritten)
                conflictSolutionsToUpload.put(server, local);
                break;
            case KEEP_REMOTE: //store the remote identity locally, overwriting the local version
                securityConfig.addIdentity(server, remote);
                break;
            case IGNORE:
                //do nothing
        }
        //if there are no more conflicts, perform the uploads and reset
        if (!hasConflictingIdentities()) {
            putIdentities(conflictSolutionsToUpload);
            resetConflicts();
        }
    }

    private static final class ConsoleNotification {
        private String messageId;
        private Object[] args;

        private ConsoleNotification(String messageId, Object[] args) {
            this.messageId = messageId;
            this.args = args;
        }
    }
}
