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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.storageServiceClient.IdentityConflict;
import org.terasology.identity.storageServiceClient.IdentityConflictSolution;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.rendering.nui.NUIManager;

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
