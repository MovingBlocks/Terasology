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
package org.terasology.rendering.opengl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;


/**
 * The FrameBuffersManager generates and maintains a number of Frame Buffer Objects (FBOs) used throughout the
 * rendering engine.
 * <p>
 * In most instances Frame Buffers can be thought of as 2D arrays of pixels in GPU memory: shaders write to them or
 * read from them. Some buffers are static and never change for the lifetime of the manager. Some buffers are dynamic:
 * they get disposed and regenerated, i.e. in case the display resolution changes. Some buffers hold intermediate
 * steps of the rendering process and the content of one buffer, "sceneFinal", is eventually sent to the display.
 * <br/>
 * At this stage no buffer can be added or deleted: the list of buffers and their characteristics is hardcoded.
 * <br/>
 * The existing set of public methods is primarily intended to allow communication between this manager and other parts
 * of the rendering engine, most notably the PostProcessor and the GraphicState instances and the shaders system.
 * <br/>
 * An important exception is the takeScreenshot() method which prompts the renderer to eventually (not immediately)
 * redirect its output to a file. This is the only public method that is intended to be used from outside the
 * rendering engine.
 * <p>
 * Default FBOs:
 * sceneOpaque:  Primary FBO: most visual information eventually ends up here
 * sceneOpaquePingPong:  The sceneOpaque FBOs are swapped every frame, to use one for reading and the other for writing
 * Notice that these two FBOs hold a number of buffers, for color, depth, normals, etc.
 * sceneSkyBand0:  two buffers used to generate a depth cue: things in the distance fades into the atmosphere's color.
 * sceneSkyBand1:
 * sceneReflectiveRefractive:  used to render reflective and refractive surfaces, most obvious case being the water surface
 * sceneReflected:  the water surface displays a reflected version of the scene. This version is stored here.
 * outline:  greyscale depth-based rendering of object outlines
 * ssao:  greyscale screen-space ambient occlusion rendering
 * ssaoBlurred:  greyscale screen-space ambient occlusion rendering - blurred version
 * scenePrePost:  intermediate step, combining a number of renderings made available so far
 * lightShafts:  light shafts rendering
 * sceneHighPass:  a number of buffers to create the bloom effect
 * sceneBloom0:
 * sceneBloom1:
 * sceneBloom2:
 * sceneBlur0:  a pair of buffers holding blurred versions of the rendered scene,
 * sceneBlur1:  also used for the bloom effect, but not only.
 * ocUndistorted:  if OculusRift support is enabled this buffer holds the side-by-side views
 * for each eye, with no lens distortion applied.
 * sceneFinal:  the content of this buffer is eventually shown on the display or sent to a file if taking a screenshot
 */

/**
 * TODO: fix above, add javadocs
 */
public abstract class AbstractFBOsManager implements BaseFBOsManager {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractFBOsManager.class);
    protected Map<ResourceUrn, FBOConfig> fboConfigs = Maps.newHashMap();
    protected Map<ResourceUrn, FBO> fboLookup = Maps.newHashMap();
    protected Map<ResourceUrn, Integer> fboUsageCountMap = Maps.newHashMap();

    private Set<FBOManagerSubscriber> fboManagerSubscribers = Sets.newHashSet();

    protected FBO generateWithDimensions(FBOConfig fboConfig, FBO.Dimensions dimensions) {
        fboConfig.setDimensions(dimensions);
        FBO fbo = FBO.create(fboConfig);

        // At this stage it's unclear what should be done in this circumstances as I (manu3d) do not know what
        // the effects of using an incomplete FrameBuffer are. Throw an exception? Live with visual artifacts?
        if (fbo.getStatus() == FBO.Status.INCOMPLETE) {
            logger.error("FBO " + fboConfig.getName() + " is incomplete. Look earlier in the log for details.");
        } else if (fbo.getStatus() == FBO.Status.UNEXPECTED) {
            logger.error("FBO " + fboConfig.getName() + " has generated an unexpected status code. Look earlier in the log for details.");
        }
        fboLookup.put(fboConfig.getName(), fbo);
        fboConfigs.put(fboConfig.getName(), fboConfig);
        return fbo;
    }

    protected void notifySubscribers() {
        for (FBOManagerSubscriber subscriber : fboManagerSubscribers) {
            subscriber.update();
        }
    }


    protected void retain(ResourceUrn resourceUrn) {
        if (fboUsageCountMap.containsKey(resourceUrn)) {
            int usageCount = fboUsageCountMap.get(resourceUrn) + 1;
            fboUsageCountMap.put(resourceUrn, usageCount);
        } else {
            fboUsageCountMap.put(resourceUrn, 1);
        }
    }

    /**
     * TODO: add javadoc
     *
     * @param fboName
     */
    @Override
    public void release(ResourceUrn fboName) {
        Preconditions.checkArgument(fboUsageCountMap.containsKey(fboName), "The given fbo is not used.");

        if (fboUsageCountMap.get(fboName) != 1) {
            int usageCount = fboUsageCountMap.get(fboName);
            fboUsageCountMap.put(fboName, usageCount - 1);
        } else {
            get(fboName).dispose();
            fboLookup.remove(fboName);
            if (fboConfigs.containsKey(fboName)) {
                fboConfigs.remove(fboName);
            }
        }
    }

    /**
     * Binds the color texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the urn of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    public boolean bindFboColorTexture(ResourceUrn fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindTexture();
            return true;
        }

        logger.error("Failed to bind FBO color texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Binds the depth texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the urn of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    public boolean bindFboDepthTexture(ResourceUrn fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindDepthTexture();
            return true;
        }

        logger.error("Failed to bind FBO depth texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Binds the normals texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the urn of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    @Override
    public boolean bindFboNormalsTexture(ResourceUrn fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindNormalsTexture();
            return true;
        }

        logger.error("Failed to bind FBO normals texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }

    /**
     * Binds the light buffer texture of the FBO with the given name and returns true.
     *
     * If no FBO is associated with the given name, false is returned and an error is logged.
     *
     * @param fboName the urn of an FBO
     * @return True if an FBO associated with the given name exists. False otherwise.
     */
    @Override
    public boolean bindFboLightBufferTexture(ResourceUrn fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo != null) {
            fbo.bindLightBufferTexture();
            return true;
        }

        logger.error("Failed to bind FBO light buffer texture since the requested " + fboName + " FBO could not be found!");
        return false;
    }


    /**
     * Returns an FBO given its name.
     *
     * If no FBO maps to the given name, null is returned and an error is logged.
     *
     * @param fboName
     * @return an FBO or null
     */
    @Override
    public FBO get(ResourceUrn fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo == null) {
            logger.error("Failed to retrieve FBO '" + fboName + "'!");
        }

        return fbo;
    }

    /**
     * TODO: add javadocs
     *
     * @param subscriber
     */
    @Override
    public boolean subscribe(FBOManagerSubscriber subscriber) {
        return fboManagerSubscribers.add(subscriber);
    }

    /**
     * TODO: add javadocs
     *
     * @param subscriber
     * @return
     */
    @Override
    public boolean unsubscribe(FBOManagerSubscriber subscriber) {
        return fboManagerSubscribers.remove(subscriber);
    }
}
