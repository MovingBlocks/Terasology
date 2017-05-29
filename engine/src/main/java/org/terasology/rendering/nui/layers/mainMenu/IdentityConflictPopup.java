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

import org.terasology.assets.ResourceUrn;
import org.terasology.i18n.TranslationSystem;
import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.IdentityConflictSolver;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * Ask the user to confirm or cancel an action.
 */
public class IdentityConflictPopup extends CoreScreenLayer {
    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:identityConflictPopup!instance");

    @In
    private TranslationSystem translationSystem;

    @In
    private StorageServiceWorker storageServiceWorker;

    private UILabel messageLabel;
    private IdentityConflictSolver.Result selectedButton;

    @Override
    public void initialise() {
        messageLabel = find("message", UILabel.class);
        WidgetUtil.trySubscribe(this, "keepLocal", button -> selectedButton = IdentityConflictSolver.Result.KEEP_LOCAL);
        WidgetUtil.trySubscribe(this, "keepRemote", button -> selectedButton = IdentityConflictSolver.Result.KEEP_REMOTE);
        WidgetUtil.trySubscribe(this, "ignore", button -> selectedButton = IdentityConflictSolver.Result.IGNORE);
    }

    private void setMessage(String serverId, String localClientId, String remoteClientId) {
        messageLabel.setText(String.format(translationSystem.translate("${engine:menu#storage-service-conflict-message}"), serverId, localClientId, remoteClientId));
    }

    public void startInteractiveConflictSolving() {
        new Thread(() -> {
            storageServiceWorker.solveConflicts((PublicIdentityCertificate server, ClientIdentity local, ClientIdentity remote) -> {
                setMessage(server.getId(), local.getPlayerPublicCertificate().getId(), remote.getPlayerPublicCertificate().getId());
                while (selectedButton == null) {
                    Thread.yield();
                }
                IdentityConflictSolver.Result result = selectedButton;
                selectedButton = null;
                return result;
            });
            getManager().popScreen();
        }).start();
    }
}
