// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;

import java.util.Map;

/**
 * Abstract class providing the default implementation for a number of FBO manager's methods.
 */
public abstract class AbstractFboManager extends AbstractSubscribable implements BaseFboManager {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractFboManager.class);
    protected Map<SimpleUri, FboConfig> fboConfigs = Maps.newHashMap();
    protected Map<SimpleUri, FBO> fboLookup = Maps.newHashMap();
    protected Map<SimpleUri, Integer> fboUsageCountMap = Maps.newHashMap();

    /**
     * Generates and returns an FBO as characterized by the FboConfig and the dimensions arguments.
     *
     * Notice that if the name of the FBO being generated matches the name of an FBO already stored
     * by the manager, the latter will be overwritten. However, the GPU-side Frame Buffer associated
     * with the overwritten FBO is not disposed by this method.
     *
     * As such, this method should be used only after the relevant checks are made and any
     * pre-existing FBO with the same name as the new one is appropriately disposed.
     *
     * This method produces errors in the log in case the FBO generation process results in
     * FBO.Status.INCOMPLETE or FBO.Status.UNEXPECTED.
     *
     * @param fboConfig an FboConfig object providing FBO configuration details.
     * @param dimensions an FBO.Dimensions instance providing the dimensions of the FBO.
     * @return an FBO instance
     */
    protected FBO generateWithDimensions(FboConfig fboConfig, FBO.Dimensions dimensions) {
        fboConfig.setDimensions(dimensions);
        FBO fbo = FBO.create(fboConfig);
        SimpleUri fboConfigName = fboConfig.getName();

        // At this stage it's unclear what should be done in this circumstances as I (manu3d) do not know what
        // the effects of using an incomplete FrameBuffer are. Throw an exception? Live with visual artifacts?
        if (fbo.getStatus() == FBO.Status.INCOMPLETE) {
            logger.error("FBO {} is incomplete. Look earlier in the log for details.", fboConfigName);
        } else if (fbo.getStatus() == FBO.Status.UNEXPECTED) {
            logger.error("FBO {} has generated an unexpected status code. Look earlier in the log for details.", fboConfigName);
        }
        fboLookup.put(fboConfigName, fbo);
        fboConfigs.put(fboConfigName, fboConfig);
        return fbo;
    }

    /**
     * Increases the usage count for a given FBO.
     *
     * When the usage count for a given FBO goes down to zero, it can be safely disposed as it is no longer in use.
     *
     * @param fboName a SimpleUri uniquely identifying an FBO stored in the manager.
     */
    protected void retain(SimpleUri fboName) {
        if (fboUsageCountMap.containsKey(fboName)) {
            int usageCount = fboUsageCountMap.get(fboName) + 1;
            fboUsageCountMap.put(fboName, usageCount);
        } else {
            fboUsageCountMap.put(fboName, 1);
        }
    }

    /**
     * Decreases the usage count for a given FBO and triggers its disposal if the count goes down to zero.
     *
     * @param fboName a SimpleUri uniquely identifying an FBO stored in the manager.
     */
    @Override
    public void release(SimpleUri fboName) {
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
     * Returns an FBO given its name.
     *
     * If no FBO maps to the given name, null is returned and an error is logged.
     *
     * @param fboName a SimpleUri uniquely identifying an FBO stored by the manager.
     * @return an FBO or null if no FBO with the given name is found.
     */
    @Override
    public FBO get(SimpleUri fboName) {
        FBO fbo = fboLookup.get(fboName);

        if (fbo == null) {
            logger.warn("Failed to retrieve FBO '{}'!", fboName);
        }

        return fbo;
    }

    /**
     * Returns an FboConfig given its name.
     *
     * If no FboConfig maps to the given name, null is returned and an error is logged.
     *
     * @param fboName a SimpleUri representing the name of an FBO
     * @return an FboConfig instance if one is found associated with the given fboName, null otherwise
     */
    @Override
    public FboConfig getFboConfig(SimpleUri fboName) {
        FboConfig fboConfig = fboConfigs.get(fboName);

        if (fboConfig == null) {
            logger.warn("Failed to retrieve FboConfig '{}'!", fboName);
        }

        return fboConfig;
    }
}
