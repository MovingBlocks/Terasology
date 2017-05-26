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

import org.terasology.i18n.TranslationSystem;

public enum StorageServiceWorkerStatus {
    LOGGED_OUT(true, "${engine:menu#storage-service-logged-out}", "${engine:menu#storage-service-log-in}"),
    LOGGED_IN(true, "${engine:menu#storage-service-logged-in}", "${engine:menu#storage-service-log-out}"),
    WORKING(false, "${engine:menu#storage-service-wait}", "");

    private final boolean buttonEnabled;
    private final String statusMessageId;
    private final String buttonMessageId;

    StorageServiceWorkerStatus(boolean buttonEnabled, String statusMessageId, String buttonMessageId) {
        this.buttonEnabled = buttonEnabled;
        this.statusMessageId = statusMessageId;
        this.buttonMessageId = buttonMessageId;
    }

    public boolean isButtonEnabled() {
        return buttonEnabled;
    }

    public String getLocalizedStatusMessage(TranslationSystem translationSystem, String loginName) {
        return String.format(translationSystem.translate(statusMessageId), loginName);
    }

    public String getLocalizedButtonMessage(TranslationSystem translationSystem) {
        return translationSystem.translate(buttonMessageId);
    }
}
