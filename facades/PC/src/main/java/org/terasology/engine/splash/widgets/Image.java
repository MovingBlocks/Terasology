// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.splash.widgets;

import org.terasology.engine.GLFWSplashScreen;
import org.terasology.engine.splash.graphics.Renderer;
import org.terasology.engine.splash.graphics.Texture;

import java.io.IOException;

public class Image implements Widget {
    protected final Texture texture;
    protected int x;
    protected int y;

    public Image(String texture, int x, int y) throws IOException {
        this.texture = Texture.loadTexture(GLFWSplashScreen.class.getResource(texture));
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(Renderer renderer) {
        texture.bind();
        renderer.begin();
        renderer.drawTexture(texture, x, y);
        renderer.end();
    }

    public void delete() {
        texture.delete();
    }
}
