// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.annotation.IndexInherited;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.rendering.dag.dependencyConnections.BufferPair;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.opengl.FboConfig;
import org.terasology.engine.rendering.opengl.ScalingFactors;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.properties.Range;

@RegisterSystem
@IndexInherited
public abstract class ModuleRendering {
    protected static final Logger logger = LoggerFactory.getLogger(ModuleRendering.class);

    // private static List<Class> renderingModules = new ArrayList<>();

    // @In
    protected Context context;
    protected ModuleManager moduleManager;
    protected Name providingModule;
    protected RenderGraph renderGraph;
    protected WorldRenderer worldRenderer;
    protected Boolean isEnabled = true;

    // Lower number, higher priority. 1 goes first
    @Range(min = 1, max = 100)
    protected int initializationPriority = 2;

    public ModuleRendering(Context context) {
        this.context = context;
        moduleManager = context.get(ModuleManager.class);
        providingModule = moduleManager.getEnvironment().getModuleProviding(this.getClass());
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setContext(Context newContext) {
        this.context = newContext;
    }

    public void setProvidingModule(Name providingModule) {
        this.providingModule = providingModule;
    }

    public void setInitializationPriority(int initPriority) {
        initializationPriority = initPriority;
    }

    public void toggleEnabled() {
        isEnabled = !isEnabled;
    }

    public void initialise() {
        renderGraph = context.get(RenderGraph.class);
        worldRenderer = context.get(WorldRenderer.class);
    }

    public Name getProvidingModule() {
        return this.providingModule;
    }

    public int getInitPriority() {
        return initializationPriority;
    }

    public void setInitPriority(int initPriority) {
        initializationPriority = initPriority;
    }

    protected void setProvidingModule(Class implementingClass) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        this.providingModule = moduleManager.getEnvironment().getModuleProviding(implementingClass);
    }

    protected BufferPair createBufferPair(String primaryBufferName, String secondaryBufferName,
                                          ScalingFactors sharedBufferScale, FBO.Type sharedBufferType, FBO.Dimensions scale) {

        FBO buffer1 = generateWithDimensions(new FboConfig(
                new SimpleUri(providingModule + ":fbo." + primaryBufferName), sharedBufferScale, sharedBufferType)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), scale);
        FBO buffer2 = generateWithDimensions(new FboConfig(
                new SimpleUri(providingModule + ":fbo." + secondaryBufferName), sharedBufferScale, sharedBufferType)
                .useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer(), scale);
        return new BufferPair(buffer1, buffer2);
    }

    /**TODO UPDATE the javadoc, this method has been taken from abstractfbomanager during DAG Enhancement project
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

        // At this stage it's unclear what should be done in this circumstances as I (manu3d) do not know what
        // the effects of using an incomplete FrameBuffer are. Throw an exception? Live with visual artifacts?
        if (fbo.getStatus() == FBO.Status.INCOMPLETE) {
            logger.error("FBO {} is incomplete. Look earlier in the log for details.", fboConfig.getName()); //NOPMD
        } else if (fbo.getStatus() == FBO.Status.UNEXPECTED) {
            logger.error("FBO {} has generated an unexpected status code. Look earlier in the log for details.", fboConfig.getName()); //NOPMD
        }
        return fbo;
    }

}
