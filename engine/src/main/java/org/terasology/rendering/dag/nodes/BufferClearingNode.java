/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.context.Context;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;

import static org.lwjgl.opengl.GL11.glClear;

/**
 * Instances of this node clear specific buffers attached to an FBOs, in accordance to a clearing mask.
 * Normally this means that all the pixels in the buffers selected by the mask are reset to a default value.
 *
 * This class could be inherited by a more specific class that sets the default values, via (yet to be written)
 * state changes.
 */
public class BufferClearingNode extends AbstractNode {
    private int clearingMask;

    /**
     * Constructs the node by requesting the creation (if necessary) of the FBO to be cleared
     * and by requesting for this FBO to be bound by the time process() gets executed. Also
     * stores the clearing mask, for use in process().
     *
     * @param fboConfig an FBOConfig object characterizing the FBO to act upon, if necessary prompting its creation.
     * @param fboManager an instance implementing the BaseFBOsManager interface, used to retrieve and bind the FBO.
     * @param clearingMask a glClear(int)-compatible mask, selecting which FBO-attached buffers to clear,
     *                      i.e. "GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT". This argument can't be zero.
     *                      Non GL_*_BIT values will be accepted but might eventually generate an opengl error.
     * @throws IllegalArgumentException if fboConfig, fboManager are null and if clearingMask is zero.
     */
    public BufferClearingNode(String nodeUri, Context context, FBOConfig fboConfig, BaseFBOsManager fboManager, int clearingMask) {
        super(nodeUri, context);

        boolean argumentsAreValid = validateArguments(fboConfig, fboManager, clearingMask);

        if (argumentsAreValid) {
            FBO fbo = requiresFBO(fboConfig, fboManager);
            addDesiredStateChange(new BindFbo(fbo));
            this.clearingMask = clearingMask;
        } else {
            throw new IllegalArgumentException("Illegal argument(s): see the log for details.");
        }
    }

    public BufferClearingNode(String nodeUri, Context context, FBO fbo, int clearingMask) {
        super(nodeUri, context);

        boolean argumentsAreValid = validateArguments(fbo, clearingMask);

        if (argumentsAreValid) {
            addDesiredStateChange(new BindFbo(fbo));
            this.clearingMask = clearingMask;
        } else {
            throw new IllegalArgumentException("Illegal argument(s): see the log for details.");
        }
    }


    /**
     * Clears the buffers selected by the mask provided in setRequiredObjects, with default values.
     * <p>
     * This method is executed within a NodeTask in the Render Tasklist.
     */
    @Override
    public void process() {
        glClear(clearingMask);
    }

    private boolean validateArguments(FBOConfig fboConfig, BaseFBOsManager fboManager, int clearingMask) {
        boolean argumentsAreValid = true;

        if (fboConfig == null) {
            argumentsAreValid = false;
            logger.warn("Illegal argument: fboConfig shouldn't be null.");
        }

        if (fboManager == null) {
            argumentsAreValid = false;
            logger.warn("Illegal argument: fboManager shouldn't be null.");
        }

        if (clearingMask == 0) {
            argumentsAreValid = false;
            logger.warn("Illegal argument: clearingMask can't be 0.");
        }

        return argumentsAreValid;
    }

    private boolean validateArguments(FBO fbo, int clearingMask) {
        boolean argumentsAreValid = true;

        if (fbo == null) {
            argumentsAreValid = false;
            logger.warn("Illegal argument: fbo shouldn't be null.");
        }

        if (clearingMask == 0) {
            argumentsAreValid = false;
            logger.warn("Illegal argument: clearingMask can't be 0.");
        }

        return argumentsAreValid;
    }
}
