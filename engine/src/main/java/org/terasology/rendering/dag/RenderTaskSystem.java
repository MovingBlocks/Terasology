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
package org.terasology.rendering.dag;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.lwjgl.opengl.Display;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add javadocs
 */
@RegisterSystem
@Share(RenderTaskSystem.class)
public class RenderTaskSystem extends BaseComponentSystem {
    private List<RenderPipelineTask> taskList;
    private Iterator<RenderPipelineTask> taskIterator;
    private RenderPipelineTask cursorTask;

    @Override
    public void initialise() {
        super.initialise();
    }

    public void setTaskList(List<RenderPipelineTask> taskList) {
        Preconditions.checkNotNull(taskList, "Illegal argument passed, `taskList` is null.");
        Preconditions.checkState(!taskList.isEmpty(), "`taskList` is empty.");

        this.taskList = taskList;
        ListIterator<RenderPipelineTask> iterator = taskList.listIterator(taskList.size());
        while (iterator.hasPrevious()) {
            RenderPipelineTask task = iterator.previous();
            if (task instanceof NodeTask) {
                cursorTask = task;
                break;
            }
        }

        Preconditions.checkNotNull(cursorTask, "`taskList` does not have any `NodeTask`s.");
        taskIterator = Iterators.cycle(taskList);
    }

    public void toggle() {
        do {
            cursorTask = taskIterator.next();
        } while (!(cursorTask instanceof NodeTask));
    }

    public void executeAll() {
        for (RenderPipelineTask task : taskList) {
            task.execute();
            if (task == cursorTask) {
                // TODO: find a more elegant solution
                bindDisplay();
                renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());
                break;
            }
        }
    }

    public RenderPipelineTask getCursorTask() {
        return cursorTask;
    }
}
