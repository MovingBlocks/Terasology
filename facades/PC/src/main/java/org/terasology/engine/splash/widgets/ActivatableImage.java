// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.splash.widgets;

import org.terasology.engine.TerasologyEngineStatus;
import org.terasology.engine.splash.graphics.Renderer;

import java.io.IOException;

public class ActivatableImage extends Image {

    private String triggerMsg;
    private boolean active;

    public ActivatableImage(String texture, int x, int y, TerasologyEngineStatus triggerMsg) throws IOException {
        super(texture, x, y);
        this.triggerMsg = triggerMsg.getDescription();
        active = false;
    }

    @Override
    public void render(Renderer renderer) {
        if (active) {
            super.render(renderer);
        }
    }

    public void post(String message) {
        if (message.equalsIgnoreCase(triggerMsg)) {
            active = true;
        }
    }

}
