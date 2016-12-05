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

//TODO: consider removing the word "Node" from the name of all Node implementations now that they are in the dag.nodes package.

import java.util.Set;

/**
 * TODO: Add javadocs
 */
public interface Node {

    void initialise();

    void process();

    // TODO: invoked when Node is removed from RenderGraph
    void dispose();

    Set<StateChange> getDesiredStateChanges();
    Set<StateChange> getDesiredStateResets();

    RenderPipelineTask generateTask();

    boolean isEnabled();

    void setEnabled(boolean enabled);

}
