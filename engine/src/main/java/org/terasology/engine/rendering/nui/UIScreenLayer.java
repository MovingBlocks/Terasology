// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.terasology.context.annotation.IndexInherited;
import org.terasology.nui.ControlWidget;

@IndexInherited
public interface UIScreenLayer extends ControlWidget {

    boolean isLowerLayerVisible();

    boolean isReleasingMouse();

    boolean isModal();

    NUIManager getManager();

    /**
     * Called when the layer becomes visible again (all layers on top have been closed)
     */
    void onShow();

    /**
     * Called when the layer becomes invisible (at least one other layer is on top)
     */
    void onHide();
}
