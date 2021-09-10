// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
