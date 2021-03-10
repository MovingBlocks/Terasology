/*
 * Copyright 2016 MovingBlocks
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
