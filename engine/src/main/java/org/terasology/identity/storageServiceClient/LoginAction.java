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
package org.terasology.engine.identity.storageServiceClient;

import java.net.URL;

/**
 */
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
