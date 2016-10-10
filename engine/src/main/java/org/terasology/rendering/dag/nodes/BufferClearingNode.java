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
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBOConfig;

import static org.lwjgl.opengl.GL11.glClear;

/**
 * Instances of this node clear specific buffers attached to an FBOs, in accordance to a clearing mask.
 * Normally this means that all the pixels in the buffers selected by the mask are reset to a default value.
 *
 * Notice that the node is fully initialised and ready to use only after calling the initialise(Object object) method.
 *
 * This class could be inherited by a more specific class that sets the default values, via (yet to be written)
 * state changes.
 */
public class BufferClearingNode extends AbstractNode {

    private int clearingMask;

    /**
     * Throws a RuntimeException if invoked. Use initialise(Object data) instead.
     */
    public void initialise() {
        throw new RuntimeException("Please do not use initialise(). For this class use initialise(Object initialData) instead.");
    }

    /**
     * Initialises the node by requesting the creation (if necessary) of the FBO implied in the RequiredData object
     * passed as argument and by requesting for this FBO to be bound by the time process() gets executed. Also
     * stores the clearing mask contained in the RequiredData object, for later use.
     *
     * @param initializationData an instance of BufferClearingNode.RequiredData, containing an FBOConfig, an FBO manager
     *                           and a clearing mask needed by this type of nodes to operate. See contained RequireData class.
     * @throws IllegalArgumentException if the object is not an instance of BufferClearingNode.RequiredData.
     */
    @Override
    public void initialise(Object initializationData) {

        if (initializationData instanceof RequiredData) {
            RequiredData requiredData = (RequiredData) initializationData;

            requiresFBO(requiredData.getFboConfig(), requiredData.getFboManager());
            addDesiredStateChange(new BindFBO(requiredData.getFboConfig().getName(), requiredData.getFboManager()));
            this.clearingMask = requiredData.getClearingMask();

        } else {
            throw new IllegalArgumentException("Object provided is not an instance of BufferClearingNode.RequiredData");
        }
    }

    /**
     * Clears the buffers selected by the mask provided in setRequiredObjects, with default values.
     *
     * This method is executed within a NodeTask in the Render Tasklist.
     */
    @Override
    public void process() {
        glClear(clearingMask);
    }

    /**
     * A simple class validating and eventually providing appropriate data for
     * the method BufferClearingNode.initialise(Object object) method.
     */
    public static class RequiredData {
        FBOConfig fboConfig;
        BaseFBOsManager fboManager;
        int clearingMask;

        /**
         * Validates the arguments and returns an object containing the data required by BufferClearingNodes,
         * to be provided to the initialise(Object object) method.
         *
         * @param fboConfig an FBOConfig object characterizing the FBO to act upon, if necessary prompting its creation.
         * @param fboManager an instance implementing the BaseFBOsManager interface, used to retrieve and bind the FBO.
         * @param clearingMask a glClear(int)-compatible mask, selecting which FBO-attached buffers to clear,
         *                     i.e. "GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT". This argument can't be zero.
         *                     Non GL_*_BIT values will be accepted but might eventually generate an opengl error.
         * @throws IllegalArgumentException if fboConfig, fboManager are null and if clearingMask is zero.
         */
        public RequiredData(FBOConfig fboConfig, BaseFBOsManager fboManager, int clearingMask) {

            boolean argumentsAreValid = validateArguments(fboConfig, fboManager, clearingMask);
            if (argumentsAreValid) {

                this.fboConfig = fboConfig;
                this.fboManager = fboManager;
                this.clearingMask = clearingMask;

            } else {
                throw new IllegalArgumentException("Illegal argument(s): see the log for details.");
            }
        }

        private boolean validateArguments(FBOConfig anFboConfig, BaseFBOsManager anFboManager, int aClearingMask) {
            boolean argumentsAreValid = true;

            if (anFboConfig == null) {
                argumentsAreValid = false;
                logger.warn("Illegal argument: fboConfig shouldn't be null.");
            }

            if (anFboManager == null) {
                argumentsAreValid = false;
                logger.warn("Illegal argument: fboManager shouldn't be null.");
            }

            if (aClearingMask == 0) {
                argumentsAreValid = false;
                logger.warn("Illegal argument: clearingMask can't be 0.");
            }

            return argumentsAreValid;
        }

        /**
         * @return the FBOConfig instance validated and stored on construction.
         */
        public FBOConfig getFboConfig() {
            return this.fboConfig;
        }

        /**
         * @return the FBO manager instance validated and stored on construction.
         */
        public BaseFBOsManager getFboManager() {
            return this.fboManager;
        }

        /**
         * @return the clearing mask integer validated and stored on construction.
         */
        public int getClearingMask() {
            return this.clearingMask;
        }
    }
}
