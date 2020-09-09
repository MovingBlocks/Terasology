// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.terasology.engine.context.Context;

import java.awt.Canvas;

public class LwjglPortlet extends BaseLwjglSubsystem {

    private Canvas customViewPort;

    @Override
    public String getName() {
        return "Portlet";
    }

    @Override
    public void postInitialise(Context context) {
        try {
            Display.setParent(customViewPort);
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
        }
    }

    public void setCustomViewport(Canvas canvas) {
        this.customViewPort = canvas;
    }

}
