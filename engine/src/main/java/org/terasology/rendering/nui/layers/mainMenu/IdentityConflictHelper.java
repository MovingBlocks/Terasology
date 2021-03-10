// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.storageServiceClient.IdentityConflict;
import org.terasology.engine.identity.storageServiceClient.IdentityConflictSolution;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.engine.rendering.nui.NUIManager;

public final class IdentityConflictHelper {

    private StorageServiceWorker storageServiceWorker;
    private NUIManager nuiManager;
    private TranslationSystem translationSystem;

    public IdentityConflictHelper(StorageServiceWorker storageServiceWorker, NUIManager nuiManager, TranslationSystem translationSystem) {
        this.storageServiceWorker = storageServiceWorker;
        this.nuiManager = nuiManager;
        this.translationSystem = translationSystem;
    }

    public void runSolver() {
        if (storageServiceWorker.hasConflictingIdentities()) {
            IdentityConflict conflict = storageServiceWorker.getNextConflict();
            ThreeButtonPopup conflictPopup = nuiManager.pushScreen(ThreeButtonPopup.ASSET_URI, ThreeButtonPopup.class);
            String message = String.format(translationSystem.translate("${engine:menu#storage-service-conflict-message}"),
                    conflict.getServerId(), conflict.getLocalId(), conflict.getRemoteId());
            conflictPopup.setMessage(translationSystem.translate("${engine:menu#warning}"), message);
            conflictPopup.setLeftButton(translationSystem.translate("${engine:menu#storage-service-conflict-keep-local}"),
                    () -> solveAndShowNext(IdentityConflictSolution.KEEP_LOCAL));
            conflictPopup.setCenterButton(translationSystem.translate("${engine:menu#storage-service-conflict-keep-remote}"),
                    () -> solveAndShowNext(IdentityConflictSolution.KEEP_REMOTE));
            conflictPopup.setRightButton(translationSystem.translate("${engine:menu#storage-service-conflict-ignore}"),
                    () -> solveAndShowNext(IdentityConflictSolution.IGNORE));
        }
    }

    private void solveAndShowNext(IdentityConflictSolution solution) {
        storageServiceWorker.solveNextConflict(solution);
        runSolver();
    }
}
