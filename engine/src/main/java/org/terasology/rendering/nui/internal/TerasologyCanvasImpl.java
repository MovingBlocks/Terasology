/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.Time;
import org.terasology.input.InputSystem;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;
import org.terasology.math.geom.Border;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.nui.asset.font.Font;
import org.terasology.nui.canvas.CanvasImpl;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Color;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.InteractionListener;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.SubRegion;
import org.terasology.nui.TabbingManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.VerticalAlign;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.events.NUIMouseDoubleClickEvent;
import org.terasology.nui.events.NUIMouseDragEvent;
import org.terasology.nui.events.NUIMouseOverEvent;
import org.terasology.nui.events.NUIMouseReleaseEvent;
import org.terasology.nui.events.NUIMouseWheelEvent;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.skin.UIStyle;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UITooltip;
import org.terasology.rendering.opengl.FrameBufferObject;
import org.terasology.utilities.Assets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
            Rect2i drawRegion = relativeToAbsolute(region);
            if (!state.cropRegion.overlaps(drawRegion)) {
                return;
            }
            material.setFloat("alpha", state.getAlpha());
            material.bindTextures();
            ((TerasologyCanvasRenderer)renderer).drawMaterialAt(material, drawRegion);
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

        Rect2i drawRegion = relativeToAbsolute(region);
        if (!state.cropRegion.overlaps(drawRegion)) {
            return;
        }

        ((TerasologyCanvasRenderer)renderer).drawMesh(mesh, material, drawRegion, drawRegion.intersect(state.cropRegion), rotation, offset, scale, state.getAlpha());
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

            fbo = ((TerasologyCanvasRenderer)renderer).getFBO(uri, size);
            state = new CanvasState(state, Rect2i.createFromMinAndSize(new Vector2i(), size));
            fbo.bindFrame();
        }

        @Override
        public void close() {
            fbo.unbindFrame();
            state = previousState;
        }
    }
}
