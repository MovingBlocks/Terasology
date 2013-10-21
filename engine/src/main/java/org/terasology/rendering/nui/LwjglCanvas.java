/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.math.AABB;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadMatrix;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glScissor;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * @author Immortius
 */
public class LwjglCanvas implements Canvas {

    private static final Logger logger = LoggerFactory.getLogger(LwjglCanvas.class);
    private static final int MAX_TEXT_WIDTH = 16777216;
    private static final Quat4f IDENTITY_ROT = new Quat4f(0, 0, 0, 1);
    private static final Vector3f ZERO_VECTOR = new Vector3f();

    private CanvasState state;

    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    private Deque<LwjglSubRegion> subregionStack = Queues.newArrayDeque();

    private Mesh billboard = Assets.getMesh("engine:UIBillboard");
    private Material textureMat = Assets.getMaterial("engine:UITexture");
    private Material meshMat = Assets.getMaterial("engine:UILitMesh");

    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private Deque<InteractionRegion> interactionRegions = Queues.newArrayDeque();
    private InteractionRegion mouseOverRegion;

    public LwjglCanvas() {
    }

    public void preRender() {
        state = new CanvasState(Rect2i.createFromMinAndSize(0, 0, Display.getWidth(), Display.getHeight()));
        glScissor(0, 0, Display.getWidth(), Display.getHeight());
        glEnable(GL_SCISSOR_TEST);
        glEnable(GL_CULL_FACE);
    }

    public void postRender() {
        Util.checkGLError();
        glDisable(GL_SCISSOR_TEST);
        if (!subregionStack.isEmpty()) {
            logger.error("UI Subregions are not being correctly ended");
            while (!subregionStack.isEmpty()) {
                subregionStack.pop().close();
            }
        }
        Iterator<Map.Entry<TextCacheKey, Map<Material, Mesh>>> textIterator = cachedText.entrySet().iterator();
        while (textIterator.hasNext()) {
            Map.Entry<TextCacheKey, Map<Material, Mesh>> entry = textIterator.next();
            if (!usedText.contains(entry.getKey())) {
                for (Mesh mesh : entry.getValue().values()) {
                    Assets.dispose(mesh);
                }
                textIterator.remove();
            }
        }
        usedText.clear();
    }

    @Override
    public SubRegion subRegion(Rect2i region, boolean crop) {
        return new LwjglSubRegion(region, crop);
    }

    @Override
    public Vector2i size() {
        return new Vector2i(state.drawRegion.width(), state.drawRegion.height());
    }

    @Override
    public void setTextCursor(Vector2i pos) {
        state.textCursorPos.set(pos);
    }

    @Override
    public void setTextCursor(int x, int y) {
        state.textCursorPos.set(x, y);
    }

    @Override
    public Vector2i getTextCursor() {
        return new Vector2i(state.textCursorPos);
    }

    @Override
    public void setAlpha(float value) {
        state.alpha = value;
    }

    @Override
    public void setTextColor(Color color) {
        state.textColor = color;
    }

    @Override
    public void drawText(Font font, String text) {
        drawTextShadowed(font, text, Color.TRANSPARENT);
    }

    @Override
    public void drawText(Font font, String text, int drawWidth) {
        drawTextShadowed(font, text, drawWidth, Color.TRANSPARENT);
    }

    @Override
    public void drawText(Font font, String text, int drawWidth, HorizontalAlignment alignment) {
        drawTextShadowed(font, text, drawWidth, alignment, Color.TRANSPARENT);
    }

    @Override
    public void drawTextShadowed(Font font, String text, Color shadowColor) {
        drawTextShadowed(font, text, MAX_TEXT_WIDTH, shadowColor);
    }

    @Override
    public void drawTextShadowed(Font font, String text, int drawWidth, Color shadowColor) {
        drawTextShadowed(font, text, drawWidth, HorizontalAlignment.LEFT, shadowColor);
    }

    @Override
    public void drawTextShadowed(Font font, String text, int drawWidth, HorizontalAlignment alignment, Color shadowColor) {
        TextCacheKey key = new TextCacheKey(text, font, drawWidth, alignment);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = LineBuilder.getLines(font, text, drawWidth);
        if (!state.cropRegion.overlaps(Rect2i.createFromMinAndSize(state.getAbsoluteOffsetX(), state.getAbsoluteOffsetY(), drawWidth, lines.size() * font.getLineHeight()))) {
            return;
        }
        if (fontMesh == null) {
            fontMesh = font.createTextMesh(lines, drawWidth, alignment);
            cachedText.put(key, fontMesh);
        }

        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            if (shadowColor.a() != 0) {
                entry.getKey().setFloat2("offset", state.getAbsoluteOffsetX() + 1, state.getAbsoluteOffsetY() + 1);
                Vector4f shadowValues = shadowColor.toVector4f();
                shadowValues.w *= state.getAlpha();
                entry.getKey().setFloat4("color", shadowValues);
                entry.getValue().render();
            }

            entry.getKey().setFloat2("offset", state.getAbsoluteOffsetX(), state.getAbsoluteOffsetY());
            Vector4f colorValues = state.textColor.toVector4f();
            colorValues.w *= state.getAlpha();
            entry.getKey().setFloat4("color", colorValues);
            entry.getValue().render();
        }

        state.textCursorPos.y += lines.size() * font.getLineHeight();
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea) {
        drawTexture(texture, toArea, ScaleMode.STRETCH);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, int ux, int uy, int uw, int uh) {
        drawTexture(texture, toArea, ScaleMode.STRETCH, ux, uy, uw, uh);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, float ux, float uy, float uw, float uh) {
        drawTexture(texture, toArea, ScaleMode.STRETCH, uw, uy, uw, uh);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode) {
        drawTexture(texture, toArea, mode, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, int ux, int uy, int uw, int uh) {
        drawTexture(texture, toArea, mode,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTexture(Texture texture, Rect2i toArea, ScaleMode mode, float ux, float uy, float uw, float uh) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(toArea))) {
            return;
        }
        if (mode == ScaleMode.TILED) {
            drawTextureTiled(texture, toArea, ux, uy, uw, uh);
        } else {
            Vector2f scale = mode.scaleForRegion(toArea, texture.getWidth(), texture.getHeight());
            textureMat.setFloat2("scale", scale);
            textureMat.setFloat2("offset",
                    state.drawRegion.minX() + toArea.minX() + 0.5f * (toArea.width() - scale.x),
                    state.drawRegion.minY() + toArea.minY() + 0.5f * (toArea.height() - scale.y));
            textureMat.setFloat2("texOffset", ux, uy);
            textureMat.setFloat2("texSize", uw, uh);
            textureMat.setTexture("texture", texture);
            textureMat.setFloat4("color", 1.0f, 1.0f, 1.0f, state.getAlpha());
            textureMat.bindTextures();
            if (mode == ScaleMode.SCALE_FILL) {
                Rect2i cropRegion = relativeToAbsolute(toArea).intersect(state.cropRegion);
                if (!cropRegion.equals(state.cropRegion)) {
                    crop(cropRegion);
                    billboard.render();
                    crop(state.cropRegion);
                } else {
                    billboard.render();
                }
            } else {
                billboard.render();
            }
        }
    }

    private void drawTextureTiled(Texture texture, Rect2i toArea, float ux, float uy, float uw, float uh) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(toArea))) {
            return;
        }
        Util.checkGLError();
        int tileW = (int) (uw * texture.getWidth());
        int tileH = (int) (uh * texture.getHeight());
        if (tileW != 0 && tileH != 0) {
            int horizTiles = TeraMath.fastAbs((toArea.width() - 1) / tileW) + 1;
            int vertTiles = TeraMath.fastAbs((toArea.height() - 1) / tileH) + 1;

            int offsetX = toArea.width() - horizTiles * tileW;
            int offsetY = toArea.height() - vertTiles * tileH;

            Rect2i drawRegion = relativeToAbsolute(toArea).intersect(state.cropRegion);
            if (!drawRegion.equals(state.cropRegion)) {
                crop(drawRegion);
                for (int tileY = 0; tileY < vertTiles; tileY++) {
                    for (int tileX = 0; tileX < horizTiles; tileX++) {
                        Rect2i tileArea = Rect2i.createFromMinAndSize(toArea.minX() + offsetX + tileW * tileX, toArea.minY() + offsetY + tileH * tileY, tileW, tileH);
                        drawTexture(texture, tileArea, ScaleMode.STRETCH, ux, uy, uw, uh);
                    }
                }
                crop(state.cropRegion);
            }
        }
        Util.checkGLError();
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile) {
        drawTextureBordered(texture, toArea, border, tile, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile, int ux, int uy, int uw, int uh) {
        drawTextureBordered(texture, toArea, border, tile,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i toArea, Border border, boolean tile, float ux, float uy, float uw, float uh) {
        float top = (float) border.getTop() / texture.getHeight();
        float left = (float) border.getLeft() / texture.getWidth();
        float bottom = (float) border.getBottom() / texture.getHeight();
        float right = (float) border.getRight() / texture.getWidth();
        int centerHoriz = toArea.width() - border.getLeft() - border.getRight();
        int centerVert = toArea.height() - border.getTop() - border.getBottom();

        if (border.getTop() != 0) {
            // TOP-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX(), toArea.minY(), border.getLeft(), border.getTop()), ScaleMode.STRETCH,
                        ux, uy, left, top);
            }
            // TOP BORDER
            Rect2i topArea = Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.minY(), centerHoriz, border.getTop());
            if (tile) {
                drawTextureTiled(texture, topArea, ux + left, uy, uw - left - right, top);
            } else {
                drawTexture(texture, topArea, ScaleMode.STRETCH, ux + left, uy, uw - left - right, top);
            }
            // TOP-RIGHT CORNER
            if (border.getRight() != 0) {
                Rect2i area = Rect2i.createFromMinAndSize(toArea.maxX() - border.getRight(), toArea.minY(), border.getRight(), border.getTop());
                drawTexture(texture, area, ScaleMode.STRETCH, ux + uw - right, uy, right, top);
            }
        }
        // LEFT BORDER
        if (border.getLeft() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(toArea.minX(), toArea.minY() + border.getTop(), border.getLeft(), centerVert);
            if (tile) {
                drawTextureTiled(texture, area, ux, uy + top, left, uh - top - bottom);
            } else {
                drawTexture(texture, area, ScaleMode.STRETCH, ux, uy + top, left, uh - top - bottom);
            }
        }
        // CENTER
        if (tile) {
            drawTextureTiled(texture, Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.minY() + border.getTop(), centerHoriz, centerVert),
                    ux + left, uy + top, uw - left - right, uh - top - bottom);
        } else {
            drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.minY() + border.getTop(), centerHoriz, centerVert), ScaleMode.STRETCH,
                    ux + left, uy + top, uw - left - right, uh - top - bottom);
        }

        // RIGHT BORDER
        if (border.getRight() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(toArea.maxX() - border.getRight(), toArea.minY() + border.getTop(), border.getRight(), centerVert);
            if (tile) {
                drawTextureTiled(texture, area, ux + uw - right, uy + top, right, uh - top - bottom);
            } else {
                drawTexture(texture, area, ScaleMode.STRETCH, ux + uw - right, uy + top, right, uh - top - bottom);
            }
        }
        if (border.getBottom() != 0) {
            // BOTTOM-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(toArea.minX(), toArea.maxY() - border.getBottom(), border.getLeft(), border.getBottom()), ScaleMode.STRETCH,
                        ux, uy + uw - bottom, left, bottom);
            }
            // BOTTOM BORDER
            Rect2i bottomArea = Rect2i.createFromMinAndSize(toArea.minX() + border.getLeft(), toArea.maxY() - border.getBottom(), centerHoriz, border.getBottom());
            if (tile) {
                drawTextureTiled(texture, bottomArea, ux + left, uy + uw - bottom, uw - left - right, bottom);
            } else {
                drawTexture(texture, bottomArea, ScaleMode.STRETCH, ux + left, uy + uw - bottom, uw - left - right, bottom);
            }
            // BOTTOM-RIGHT CORNER
            if (border.getRight() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(toArea.maxX() - border.getRight(), toArea.maxY() - border.getBottom(), border.getRight(), border.getBottom()),
                        ScaleMode.STRETCH, ux + uw - right, uy + uw - bottom, right, bottom);
            }
        }
    }

    @Override
    public void drawMaterial(Material material, Rect2i toArea) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(toArea))) {
            return;
        }
        material.setFloat("alpha", state.getAlpha());
        material.bindTextures();
        glPushMatrix();
        glTranslatef(state.drawRegion.minX() + toArea.minX(), state.drawRegion.minY() + toArea.minY(), 0f);
        glScalef(toArea.width(), toArea.height(), 1);
        billboard.render();
        glPopMatrix();
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        if (material == null) {
            logger.warn("Attempted to draw with nonexistent material");
            return;
        }
        if (mesh == null) {
            logger.warn("Attempted to draw nonexistent mesh");
            return;
        }

        if (!state.cropRegion.overlaps(relativeToAbsolute(region))) {
            return;
        }

        AABB meshAABB = mesh.getAABB();
        Vector3f meshExtents = meshAABB.getExtents();
        float fitScale = 0.45f * Math.min(region.width(), region.height()) / Math.max(meshExtents.x, Math.max(meshExtents.y, meshExtents.z));
        Vector3f centerOffset = meshAABB.getCenter();
        centerOffset.scale(-1.0f);

        // Roll 180 degrees because the Y-Axis is reversed
        Quat4f fixRotation = new Quat4f();
        QuaternionUtil.setEuler(fixRotation, 0, 0, TeraMath.PI);

        Matrix4f centerTransform = new Matrix4f(IDENTITY_ROT, centerOffset, 1.0f);
        Matrix4f userTransform = new Matrix4f(rotation, offset, scale);
        Matrix4f fixRotationTransform = new Matrix4f(fixRotation, ZERO_VECTOR, fitScale);
        Matrix4f translateTransform = new Matrix4f(IDENTITY_ROT,
                new Vector3f(state.drawRegion.minX() + region.minX() + region.width() / 2,
                        state.drawRegion.minY() + region.minY() + region.height() / 2, 0), 1);

        userTransform.mul(centerTransform);
        fixRotationTransform.mul(userTransform);
        translateTransform.mul(fixRotationTransform);

        Transform transform = new Transform(translateTransform);
        float[] data = new float[16];
        transform.getOpenGLMatrix(data);

        matrixBuffer.put(data);
        matrixBuffer.rewind();

        Rect2i cropRegion = relativeToAbsolute(region).intersect(state.cropRegion);

        crop(cropRegion);
        glEnable(GL11.GL_DEPTH_TEST);
        glClear(GL11.GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL11.GL_MODELVIEW);
        glPushMatrix();
        glLoadMatrix(matrixBuffer);
        matrixBuffer.rewind();

        boolean matrixStackSupported = material.supportsFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
        if (matrixStackSupported) {
            material.activateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
        }
        material.setFloat("alpha", state.getAlpha());
        material.bindTextures();
        mesh.render();
        if (matrixStackSupported) {
            material.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
        }

        glPopMatrix();

        glDisable(GL11.GL_DEPTH_TEST);

        crop(state.cropRegion);
    }

    @Override
    public void drawMesh(Mesh mesh, Texture texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        meshMat.setTexture("texture", texture);
        drawMesh(mesh, meshMat, region, rotation, offset, scale);
    }

    @Override
    public void addInteractionRegion(Rect2i region, InteractionListener listener) {
        interactionRegions.addLast(new InteractionRegion(region, listener));
    }

    private Rect2i relativeToAbsolute(Rect2i region) {
        return Rect2i.createFromMinAndSize(region.minX() + state.drawRegion.minX(), region.minY() + state.drawRegion.minY(), region.width(), region.height());
    }

    private void crop(Rect2i region) {
        crop(region.minX(), region.minY(), region.width(), region.height());
    }

    private void crop(int x, int y, int w, int h) {
        glScissor(x, Display.getHeight() - y - h, w, h);
    }

    /**
     * The state of the canvas
     */
    private static class CanvasState {
        public Rect2i drawRegion;
        public Rect2i cropRegion;
        public Color textColor = Color.WHITE;

        public Vector2i textCursorPos = new Vector2i();
        private float alpha = 1.0f;
        private float baseAlpha = 1.0f;

        public CanvasState(Rect2i drawRegion) {
            this(drawRegion, drawRegion);
        }

        public CanvasState(Rect2i drawRegion, Rect2i cropRegion) {
            this.drawRegion = drawRegion;
            this.cropRegion = cropRegion;
        }

        public int getAbsoluteOffsetX() {
            return textCursorPos.x + drawRegion.minX();
        }

        public int getAbsoluteOffsetY() {
            return textCursorPos.y + drawRegion.minY();
        }

        public float getAlpha() {
            return alpha * baseAlpha;
        }

    }

    /**
     * A SubRegion implementation for this canvas.
     */
    private class LwjglSubRegion implements SubRegion {

        public boolean croppingRegion;
        private CanvasState previousState;
        private boolean disposed;

        public LwjglSubRegion(Rect2i region, boolean crop) {
            previousState = state;
            subregionStack.push(this);

            int left = region.minX() + state.drawRegion.minX();
            int right = region.maxX() + state.drawRegion.minX();
            int top = region.minY() + state.drawRegion.minY();
            int bottom = region.maxY() + state.drawRegion.minY();
            Rect2i subRegion = Rect2i.createFromMinAndMax(left, top, right, bottom);
            if (crop) {
                Rect2i cropRegion = subRegion.intersect(state.cropRegion);
                if (cropRegion.isEmpty()) {
                    state = new CanvasState(subRegion, cropRegion);
                } else if (!cropRegion.equals(state.cropRegion)) {
                    state = new CanvasState(subRegion, cropRegion);
                    crop(cropRegion);
                    croppingRegion = true;
                } else {
                    state = new CanvasState(subRegion);
                }
            } else {
                state = new CanvasState(subRegion);
            }
            state.baseAlpha = previousState.getAlpha();
        }

        @Override
        public void close() {
            if (!disposed) {
                Util.checkGLError();
                disposed = true;
                LwjglSubRegion region = subregionStack.pop();
                while (!region.equals(this)) {
                    logger.error("UI SubRegions being closed in an incorrect order");
                    region.close();
                    region = subregionStack.pop();
                }
                if (croppingRegion) {
                    glScissor(previousState.drawRegion.minX(), previousState.drawRegion.minY(), previousState.drawRegion.width(), previousState.drawRegion.height());
                }
                state = previousState;
            }
        }
    }

    /**
     * A key that identifies an entry in the text cache. It contains the elements that affect the generation of mesh for text rendering.
     */
    private static class TextCacheKey {
        private String text;
        private Font font;
        private int width;
        private HorizontalAlignment alignment;

        public TextCacheKey(String text, Font font, int maxWidth, HorizontalAlignment alignment) {
            this.text = text;
            this.font = font;
            this.width = maxWidth;
            this.alignment = alignment;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TextCacheKey) {
                TextCacheKey other = (TextCacheKey) obj;
                return Objects.equals(text, other.text) && Objects.equals(font, other.font)
                        && Objects.equals(width, other.width) && Objects.equals(alignment, other.alignment);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, font, width, alignment);
        }
    }

    private static class InteractionRegion {
        public InteractionListener listener;
        public Rect2i region;

        public InteractionRegion(Rect2i region, InteractionListener listener) {
            this.listener = listener;
            this.region = region;
        }

    }
}
