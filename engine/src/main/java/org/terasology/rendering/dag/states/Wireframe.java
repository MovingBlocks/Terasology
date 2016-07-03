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
package org.terasology.rendering.dag.states;

import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;

/**
 *
 */
public class Wireframe extends AbstractState {
    private RenderingDebugConfig renderingDebugConfig;

    public Wireframe(Context context) {
        super(context);
        renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
    }

    @Override
    public void set(StateValue stateValue) {
        super.set(stateValue);

        if (!renderingDebugConfig.isWireframe()) {
            return;
        }

        switch (stateValue) {
            case ENABLED:
                GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                break;

            case DISABLED:
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL_FILL);
                break;

            case OPTIONAL:
                break;

        }
    }
}

