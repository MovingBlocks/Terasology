// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.loadingScreen;

import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UILoadBar;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

/**
 */
public class LoadingScreen extends CoreScreenLayer {

    private UILabel messageLabel;
    private UILoadBar fillBar;

    @In
    private TranslationSystem translationSystem;

    @Override
    public void initialise() {
        messageLabel = find("statusLabel", UILabel.class);
        fillBar = find("progressBar", UILoadBar.class);
    }

    public void updateStatus(String message, float v) {
        if (messageLabel != null) {
            messageLabel.setText(translationSystem.translate(message));
        }
        if (fillBar != null) {
            fillBar.setValue(v);
        }
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
