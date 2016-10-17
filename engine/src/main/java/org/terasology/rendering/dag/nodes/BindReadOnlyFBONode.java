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
package org.terasology.rendering.dag.nodes;

import org.terasology.rendering.dag.AbstractNode;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;

// TODO: eliminate this node - it is only temporary as the instructions in process() used to be in a different node
public class BindReadOnlyFBONode extends AbstractNode {

    public void initialise() {

    }

    public void process() {
        READ_ONLY_GBUFFER.bind();
        setViewportToSizeOf(READ_ONLY_GBUFFER); // TODO: verify this is necessary
    }
}
