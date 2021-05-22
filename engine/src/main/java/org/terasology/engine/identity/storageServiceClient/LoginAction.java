// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import java.net.URL;

final class LoginAction implements Action {

    private final URL serviceURL;
    private final String login;
    private final String password;

    LoginAction(URL serviceURL, String login, String password) {
        this.serviceURL = serviceURL;
        this.login = login;
        this.password = password;
    }

    @Override
    public void perform(StorageServiceWorker worker) {
        try {
            worker.sessionInstance = APISession.createFromLogin(serviceURL, login, password);
            worker.loginName = worker.sessionInstance.getLoginName();
            worker.storageConfig.setServiceURL(serviceURL);
            worker.storageConfig.setSessionToken(worker.sessionInstance.getSessionToken());
            worker.saveConfig();
            worker.status = StorageServiceWorkerStatus.LOGGED_IN;
            worker.logMessage(false, "${engine:menu#storage-service-login-ok}");
            worker.syncIdentities();
        } catch (Exception e) {
            worker.sessionInstance = null;
            worker.status = StorageServiceWorkerStatus.LOGGED_OUT;
            worker.logMessage(true, "${engine:menu#storage-service-login-fail}", e.getMessage());
        }
    }
}
