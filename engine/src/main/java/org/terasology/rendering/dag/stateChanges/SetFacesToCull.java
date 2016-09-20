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
package org.terasology.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.SetFacesToCullTask;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;

/**
 * TODO: Add javadocs
 */
public final class SetFacesToCull implements StateChange {
    private static SetFacesToCull defaultInstance = new SetFacesToCull(GL_BACK); // also specified in OpenGL documentation
    private static Map<Integer, String> modeNameMap = ImmutableMap.of(GL_BACK, "GL_BACK",
            GL_FRONT, "GL_FRONT",
            GL_FRONT_AND_BACK, "GL_FRONT_AND_BACK");
    private SetFacesToCullTask task;
    private int mode;

    public SetFacesToCull(int mode) {
        if (mode == GL_BACK || mode == GL_FRONT || mode == GL_FRONT_AND_BACK) {
            this.mode = mode;
        } else {
            throw new IllegalArgumentException("Mode must be GL_BACK, GL_FRONT or GL_FRONT_AND_BACK.");
        }
    }

    public int getMode() {
        return mode;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetFacesToCullTask(mode);
        }

        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mode);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetFacesToCull) && mode == ((SetFacesToCull) obj).getMode();
    }

    @Override
    public boolean isTheDefaultInstance() {
        return defaultInstance == this;
    }

    public static String getModeName(int mode) {
        return modeNameMap.get(mode);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), getModeName(mode));
    }
}
