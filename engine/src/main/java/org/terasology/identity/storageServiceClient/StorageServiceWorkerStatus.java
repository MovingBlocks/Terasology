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

public enum StorageServiceWorkerStatus {
    LOGGED_OUT(true, "${engine:menu#storage-service-logged-out}", "${engine:menu#storage-service-log-in}"),
    LOGGED_IN(true, "${engine:menu#storage-service-logged-in}", "${engine:menu#storage-service-log-out}"),
    WORKING(false, "${engine:menu#storage-service-wait}", "");

    final String statusMessageId;
    final String buttonMessageId;
    private final boolean buttonEnabled;

    StorageServiceWorkerStatus(boolean buttonEnabled, String statusMessageId, String buttonMessageId) {
        this.buttonEnabled = buttonEnabled;
        this.statusMessageId = statusMessageId;
        this.buttonMessageId = buttonMessageId;
    }

    public boolean isButtonEnabled() {
        return buttonEnabled;
    }
}
