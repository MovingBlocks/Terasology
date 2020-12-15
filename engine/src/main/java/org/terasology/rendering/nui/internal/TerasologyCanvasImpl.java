// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.internal;

import org.joml.Rectanglei;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.Time;
import org.terasology.input.InputSystem;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.nui.SubRegion;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.canvas.CanvasImpl;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.opengl.FrameBufferObject;
import org.terasology.utilities.Assets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 */
public class TerasologyCanvasImpl extends CanvasImpl implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyCanvasImpl.class);

    private final NUIManager nuiManager;
    private final Time time;

    private Material meshMat;

    private RenderingConfig renderingConfig;

    public TerasologyCanvasImpl(NUIManager nuiManager, Context context, TerasologyCanvasRenderer renderer) {
        super(renderer, nuiManager, context.get(InputSystem.class).getKeyboard(), context.get(InputSystem.class).getMouseDevice(),
                Assets.getTexture("engine:white").get(), null, context.get(Config.class).getRendering().getUiScale() / 100);

        this.renderer = renderer;
        this.nuiManager = nuiManager;
        this.time = context.get(Time.class);
        this.meshMat = Assets.getMaterial("engine:UILitMesh").get();

        this.renderingConfig = context.get(Config.class).getRendering();
        this.uiScale = this.renderingConfig.getUiScale() / 100f;

        this.renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);
    }

    @Override
    public void preRender() {
        super.preRender();

        if (getSkin() == null) {
            setSkin(Assets.getSkin("engine:default").get());
        }
    }

    @Override
    protected float getGameTimeInSeconds() {
        return time.getGameTime();
    }

    @Override
    protected long getGameTimeInMs() {
        return time.getGameTimeInMs();
    }

    //NOTE: now only accessible via CanvasUtility
    public SubRegion subRegionFBO(ResourceUrn uri, BaseVector2i size) {
        return new SubRegionFBOImpl(uri, size);
    }

    // NOTE: drawMaterial and drawMesh can now only be accessed through CanvasUtility
    public void drawMaterial(Material material, Rect2i region) {
        if (material.isRenderable()) {
            Rectanglei drawRegion = relativeToAbsolute(JomlUtil.from(region));
            if (!state.cropRegion.intersectsRectangle(drawRegion)) {
                return;
            }
            material.setFloat("alpha", state.getAlpha());
            material.bindTextures();
            ((TerasologyCanvasRenderer) renderer).drawMaterialAt(material, drawRegion);
        }
    }

    public void drawMesh(Mesh mesh, Material material, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        if (material == null) {
            logger.warn("Attempted to draw with nonexistent material");
            return;
        }
        if (mesh == null) {
            logger.warn("Attempted to draw nonexistent mesh");
            return;
        }

        Rectanglei drawRegion = relativeToAbsolute(JomlUtil.from(region));
        if (!state.cropRegion.intersectsRectangle(drawRegion)) {
            return;
        }

        ((TerasologyCanvasRenderer) renderer).drawMesh(
                mesh, material, drawRegion, drawRegion.intersection(state.cropRegion),
                JomlUtil.from(rotation), JomlUtil.from(offset), scale, state.getAlpha());
    }

    public void drawMesh(Mesh mesh, UITextureRegion texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        meshMat.setTexture("texture", ((TextureRegion)texture).getTexture());
        drawMesh(mesh, meshMat, region, rotation, offset, scale);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RenderingConfig.UI_SCALE)) {
            this.uiScale = this.renderingConfig.getUiScale() / 100f;
        }
    }

    private final class SubRegionFBOImpl implements SubRegion {
        private FrameBufferObject fbo;
        private CanvasState previousState;

        private SubRegionFBOImpl(ResourceUrn uri, BaseVector2i size) {
            previousState = state;

            fbo = ((TerasologyCanvasRenderer)renderer).getFBO(uri, JomlUtil.from(size));
            state = new CanvasState(state, new Rectanglei(0, 0, size.x(), size.y()));
            fbo.bindFrame();
        }

        @Override
        public void close() {
            fbo.unbindFrame();
            state = previousState;
        }
    }
}
