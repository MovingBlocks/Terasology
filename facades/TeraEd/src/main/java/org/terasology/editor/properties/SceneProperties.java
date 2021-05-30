// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.properties;

import com.google.common.collect.Lists;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.modes.StateIngame;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.backdrop.BackdropRenderer;

import java.util.List;

public class SceneProperties implements PropertyProvider {

    private final TerasologyEngine engine;

    public SceneProperties(TerasologyEngine engine) {
        this.engine = engine;
    }

    @Override
    public List<Property<?>> getProperties() {
        List<Property<?>> result = Lists.newArrayList();

        GameState gameState = engine.getState();
        if (!(gameState instanceof StateIngame)) {
            return result;
        }
        StateIngame ingameState = (StateIngame) gameState;
        Context ingameContext = ingameState.getContext();

        BackdropProvider backdropProvider = ingameContext.get(BackdropProvider.class);
        if (backdropProvider != null) {
            result.addAll(new ReflectionProvider(backdropProvider, ingameContext).getProperties());
        }
        BackdropRenderer backdropRenderer = ingameContext.get(BackdropRenderer.class);
        if (backdropRenderer != null) {
            result.addAll(new ReflectionProvider(backdropRenderer, ingameContext).getProperties());
        }

        // TODO: fix this
        /*FrameBuffersManager renderingProcess = ingameContext.get(FrameBuffersManager.class);
        if (renderingProcess != null) {
            result.addAll(new ReflectionProvider(renderingProcess, ingameContext).getProperties());
        }*/
        return result;
    }
}
