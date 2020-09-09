// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame;

import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;

public class DebugInfo extends CoreScreenLayer {

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "closeButton", widget -> getManager().closeScreen(DebugInfo.this));
    }
}
