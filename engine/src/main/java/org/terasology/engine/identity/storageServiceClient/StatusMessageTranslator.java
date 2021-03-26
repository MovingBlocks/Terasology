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

import org.terasology.engine.i18n.TranslationSystem;

public final class StatusMessageTranslator {

    private StatusMessageTranslator() {
    }

    public static String getLocalizedStatusMessage(StorageServiceWorkerStatus status, TranslationSystem translationSystem, String loginName) {
        return String.format(translationSystem.translate(status.statusMessageId), loginName);
    }

    public static String getLocalizedButtonMessage(StorageServiceWorkerStatus status, TranslationSystem translationSystem) {
        return translationSystem.translate(status.buttonMessageId);
    }
}
