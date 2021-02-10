// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.nui.WidgetUtil;
import org.terasology.rendering.nui.CoreScreenLayer;

public class DebugInfo extends CoreScreenLayer {

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "closeButton", widget -> getManager().closeScreen(DebugInfo.this));
    }
}
