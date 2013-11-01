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
import org.terasology.input.MouseInput;
import org.terasology.math.AABB;
import org.terasology.math.MatrixUtils;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.skin.UIStyle;

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
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glLoadMatrix;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.Util.checkGLError;

/**
 * @author Immortius
 */
public class LwjglCanvas implements Canvas {

    private static final Logger logger = LoggerFactory.getLogger(LwjglCanvas.class);
    private static final Quat4f IDENTITY_ROT = new Quat4f(0, 0, 0, 1);

    private CanvasState state;

    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    private Deque<LwjglSubRegion> subregionStack = Queues.newArrayDeque();

    private Mesh billboard = Assets.getMesh("engine:UIBillboard");
    private Material textureMat = Assets.getMaterial("engine:UITexture");
    private Material meshMat = Assets.getMaterial("engine:UILitMesh");

    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private Deque<InteractionRegion> interactionRegions = Queues.newArrayDeque();
    private Set<InteractionRegion> mouseOverRegions = Sets.newLinkedHashSet();
    private InteractionRegion clickedRegion;
    private int dragMouseButton;

    private Matrix4f modelView;

    public LwjglCanvas() {
    }

    public void preRender() {
        interactionRegions.clear();
        state = new CanvasState(null, Rect2i.createFromMinAndSize(0, 0, Display.getWidth(), Display.getHeight()));
        glEnable(GL_CULL_FACE);

        checkGLError();
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        checkGLError();
        glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 0, 2048f);
        checkGLError();
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        modelView = new Matrix4f();
        modelView.setIdentity();
        modelView.setTranslation(new Vector3f(0, 0, -1024f));

        MatrixUtils.matrixToFloatBuffer(modelView, matrixBuffer);
        glLoadMatrix(matrixBuffer);
        matrixBuffer.rewind();
    }

    public void postRender() {
        Util.checkGLError();
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
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        checkGLError();
    }

    public void processMouseOver(Vector2i position) {
        Set<InteractionRegion> newMouseOverRegions = Sets.newLinkedHashSet();
        Iterator<InteractionRegion> iter = interactionRegions.descendingIterator();
        while (iter.hasNext()) {
            InteractionRegion next = iter.next();
            if (next.region.contains(position)) {
                Vector2i relPos = new Vector2i(position).sub(next.region.min());
                next.listener.onMouseOver(relPos, newMouseOverRegions.isEmpty());
                newMouseOverRegions.add(next);
            }
        }

        for (InteractionRegion region : mouseOverRegions) {
            if (!newMouseOverRegions.contains(region) && interactionRegions.contains(region)) {
                region.listener.onMouseLeave();
            }
        }

        if (clickedRegion != null && !interactionRegions.contains(clickedRegion)) {
            clickedRegion = null;
        }

        mouseOverRegions = newMouseOverRegions;
    }

    public void processMouseClick(MouseInput button, Vector2i pos) {
        for (InteractionRegion next : mouseOverRegions) {
            if (next.region.contains(pos)) {
                Vector2i relPos = new Vector2i(pos).sub(next.region.min());
                if (next.listener.onMouseClick(button, relPos)) {
                    clickedRegion = next;
                    break;
                }
            }
        }
    }

    public void processMouseRelease(MouseInput button, Vector2i pos) {
        if (clickedRegion != null) {
            clickedRegion.listener.onMouseRelease(button, pos);
            clickedRegion = null;
        }
    }

    public void processMouseWheeled(int amount, Vector2i pos) {
        for (InteractionRegion next : mouseOverRegions) {
            if (next.region.contains(pos)) {
                Vector2i relPos = new Vector2i(pos).sub(next.region.min());
                if (next.listener.onMouseWheeled(amount, relPos)) {
                    break;
                }
            }
        }
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
    public void setAlpha(float value) {
        state.alpha = value;
    }

    @Override
    public void setSkin(UISkin skin) {
        state.skin = skin;
    }

    @Override
    public void setWidget(Class<? extends UIWidget> widgetClass) {
        state.widget = widgetClass;
        state.mode = "";
    }

    @Override
    public void setFamily(String familyName) {
        state.family = familyName;
    }

    @Override
    public void setMode(String mode) {
        state.mode = mode;
    }

    @Override
    public UIStyle getCurrentStyle() {
        return state.getCurrentStyle();
    }

    @Override
    public void drawText(String text) {
        drawText(text, state.getRelativeRegion());
    }

    @Override
    public void drawText(String text, Rect2i region) {
        Rect2i drawRegion = applyStyleToRegion(region);
        UIStyle style = getCurrentStyle();
        if (style.isTextShadowed()) {
            drawTextShadowed(text, style.getFont(), style.getTextColor(), style.getTextShadowColor(), drawRegion, style.getTextAlignmentH(), style.getTextAlignmentV());
        } else {
            drawText(text, style.getFont(), style.getTextColor(), drawRegion, style.getTextAlignmentH(), style.getTextAlignmentV());
        }
    }

    @Override
    public void drawTexture(Texture texture) {
        drawTexture(texture, state.getRelativeRegion());
    }

    @Override
    public void drawTexture(Texture texture, int ux, int uy, int uw, int uh) {
        drawTexture(texture, state.getRelativeRegion(), ux, uy, uw, uh);
    }

    @Override
    public void drawTexture(Texture texture, float ux, float uy, float uw, float uh) {
        drawTexture(texture, state.getRelativeRegion(), ux, uy, uw, uh);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i region) {
        drawTexture(texture, applyStyleToRegion(region), ScaleMode.STRETCH);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i region, float ux, float uy, float uw, float uh) {
        drawTexture(texture, applyStyleToRegion(region), ScaleMode.STRETCH, uw, uy, uw, uh);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i region, int ux, int uy, int uw, int uh) {
        drawTexture(texture, applyStyleToRegion(region), ScaleMode.STRETCH, ux, uy, uw, uh);
    }

    private Rect2i applyStyleToRegion(Rect2i region) {
        UIStyle style = getCurrentStyle();
        if (!style.getMargin().isEmpty()) {
            return Rect2i.createFromMinAndMax(region.minX() + style.getMargin().getLeft(), region.minY() + style.getMargin().getTop(),
                    region.maxX() - style.getMargin().getRight(), region.maxY() - style.getMargin().getBottom());
        }
        return region;
    }

    @Override
    public void drawBackground() {
        drawBackground(Rect2i.createFromMinAndMax(0, 0, state.drawRegion.width(), state.drawRegion.height()));
    }

    @Override
    public void drawBackground(Rect2i region) {
        UIStyle style = getCurrentStyle();
        if (style.getBackground() != null) {
            if (style.getBackgroundBorder().isEmpty()) {
                drawTexture(style.getBackground(), region, style.getBackgroundScaleMode());
            } else {
                drawTextureBordered(style.getBackground(), region, style.getBackgroundBorder(), style.getBackgroundScaleMode() == ScaleMode.TILED);
            }
        }
    }

    @Override
    public void drawText(String text, Font font, Color color) {
        drawTextShadowed(text, font, color, Color.TRANSPARENT);
    }

    @Override
    public void drawText(String text, Font font, Color color, Rect2i region) {
        drawTextShadowed(text, font, color, Color.TRANSPARENT, region);
    }

    @Override
    public void drawText(String text, Font font, Color color, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign) {
        drawTextShadowed(text, font, color, Color.TRANSPARENT, region, hAlign, vAlign);
    }

    @Override
    public void drawTextShadowed(String text, Font font, Color color, Color shadowColor) {
        drawTextShadowed(text, font, color, shadowColor, state.drawRegion);
    }

    @Override
    public void drawTextShadowed(String text, Font font, Color color, Color shadowColor, Rect2i region) {
        drawTextShadowed(text, font, color, shadowColor, region, HorizontalAlign.LEFT, VerticalAlign.TOP);
    }

    @Override
    public void drawTextShadowed(String text, Font font, Color color, Color shadowColor, Rect2i region, HorizontalAlign hAlign, VerticalAlign vAlign) {
        TextCacheKey key = new TextCacheKey(text, font, region.width(), hAlign);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = LineBuilder.getLines(font, text, region.width());
        Rect2i absoluteRegion = relativeToAbsolute(region);
        Rect2i croppingRegion = absoluteRegion.intersect(state.cropRegion);
        if (croppingRegion.isEmpty()) {
            return;
        }
        if (fontMesh == null) {
            fontMesh = font.createTextMesh(lines, absoluteRegion.width(), hAlign);
            cachedText.put(key, fontMesh);
        }

        Vector2i offset = new Vector2i(absoluteRegion.minX(), absoluteRegion.minY());
        offset.y += vAlign.getOffset(region.height(), lines.size() * font.getLineHeight());


        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            entry.getKey().setFloat4("croppingBoundaries", croppingRegion.minX(), croppingRegion.maxX() + 1, croppingRegion.minY(), croppingRegion.maxY() + 1);
            if (shadowColor.a() != 0) {
                entry.getKey().setFloat2("offset", offset.x + 1, offset.y + 1);
                Vector4f shadowValues = shadowColor.toVector4f();
                shadowValues.w *= state.getAlpha();
                entry.getKey().setFloat4("color", shadowValues);
                entry.getValue().render();
            }

            entry.getKey().setFloat2("offset", offset.x, offset.y);
            Vector4f colorValues = color.toVector4f();
            colorValues.w *= state.getAlpha();
            entry.getKey().setFloat4("color", colorValues);
            entry.getValue().render();
        }
    }

    @Override
    public void drawTexture(Texture texture, Rect2i region, ScaleMode mode) {
        drawTexture(texture, region, mode, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTexture(Texture texture, Rect2i region, ScaleMode mode, int ux, int uy, int uw, int uh) {
        drawTexture(texture, region, mode,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTexture(Texture texture, Rect2i region, ScaleMode mode, float ux, float uy, float uw, float uh) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(region))) {
            return;
        }
        if (mode == ScaleMode.TILED) {
            drawTextureTiled(texture, region, ux, uy, uw, uh);
        } else {
            Vector2f scale = mode.scaleForRegion(region, texture.getWidth(), texture.getHeight());
            textureMat.setFloat2("scale", scale);
            textureMat.setFloat2("offset",
                    state.drawRegion.minX() + region.minX() + 0.5f * (region.width() - scale.x),
                    state.drawRegion.minY() + region.minY() + 0.5f * (region.height() - scale.y));
            textureMat.setFloat2("texOffset", ux, uy);
            textureMat.setFloat2("texSize", uw, uh);
            textureMat.setTexture("texture", texture);
            textureMat.setFloat4("color", 1.0f, 1.0f, 1.0f, state.getAlpha());
            textureMat.bindTextures();
            if (mode == ScaleMode.SCALE_FILL) {
                Rect2i cropRegion = relativeToAbsolute(region).intersect(state.cropRegion);
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

    private void crop(Rect2i cropRegion) {
        textureMat.setFloat4("croppingBoundaries", cropRegion.minX(), cropRegion.maxX() + 1, cropRegion.minY(), cropRegion.maxY() + 1);
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
    public void drawTextureBordered(Texture texture, Rect2i region, Border border, boolean tile) {
        drawTextureBordered(texture, region, border, tile, 0f, 0f, 1f, 1f);
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i region, Border border, boolean tile, int ux, int uy, int uw, int uh) {
        drawTextureBordered(texture, region, border, tile,
                (float) ux / texture.getWidth(), (float) uy / texture.getHeight(),
                (float) uw / texture.getWidth(), (float) uh / texture.getHeight());
    }

    @Override
    public void drawTextureBordered(Texture texture, Rect2i region, Border border, boolean tile, float ux, float uy, float uw, float uh) {
        float top = (float) border.getTop() / texture.getHeight();
        float left = (float) border.getLeft() / texture.getWidth();
        float bottom = (float) border.getBottom() / texture.getHeight();
        float right = (float) border.getRight() / texture.getWidth();
        int centerHoriz = region.width() - border.getLeft() - border.getRight();
        int centerVert = region.height() - border.getTop() - border.getBottom();

        if (border.getTop() != 0) {
            // TOP-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(region.minX(), region.minY(), border.getLeft(), border.getTop()), ScaleMode.STRETCH,
                        ux, uy, left, top);
            }
            // TOP BORDER
            Rect2i topArea = Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.minY(), centerHoriz, border.getTop());
            if (tile) {
                drawTextureTiled(texture, topArea, ux + left, uy, uw - left - right, top);
            } else {
                drawTexture(texture, topArea, ScaleMode.STRETCH, ux + left, uy, uw - left - right, top);
            }
            // TOP-RIGHT CORNER
            if (border.getRight() != 0) {
                Rect2i area = Rect2i.createFromMinAndSize(region.maxX() - border.getRight(), region.minY(), border.getRight(), border.getTop());
                drawTexture(texture, area, ScaleMode.STRETCH, ux + uw - right, uy, right, top);
            }
        }
        // LEFT BORDER
        if (border.getLeft() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(region.minX(), region.minY() + border.getTop(), border.getLeft(), centerVert);
            if (tile) {
                drawTextureTiled(texture, area, ux, uy + top, left, uh - top - bottom);
            } else {
                drawTexture(texture, area, ScaleMode.STRETCH, ux, uy + top, left, uh - top - bottom);
            }
        }
        // CENTER
        if (tile) {
            drawTextureTiled(texture, Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.minY() + border.getTop(), centerHoriz, centerVert),
                    ux + left, uy + top, uw - left - right, uh - top - bottom);
        } else {
            drawTexture(texture, Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.minY() + border.getTop(), centerHoriz, centerVert), ScaleMode.STRETCH,
                    ux + left, uy + top, uw - left - right, uh - top - bottom);
        }

        // RIGHT BORDER
        if (border.getRight() != 0) {
            Rect2i area = Rect2i.createFromMinAndSize(region.maxX() - border.getRight(), region.minY() + border.getTop(), border.getRight(), centerVert);
            if (tile) {
                drawTextureTiled(texture, area, ux + uw - right, uy + top, right, uh - top - bottom);
            } else {
                drawTexture(texture, area, ScaleMode.STRETCH, ux + uw - right, uy + top, right, uh - top - bottom);
            }
        }
        if (border.getBottom() != 0) {
            // BOTTOM-LEFT CORNER
            if (border.getLeft() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(region.minX(), region.maxY() - border.getBottom(), border.getLeft(), border.getBottom()), ScaleMode.STRETCH,
                        ux, uy + uw - bottom, left, bottom);
            }
            // BOTTOM BORDER
            Rect2i bottomArea = Rect2i.createFromMinAndSize(region.minX() + border.getLeft(), region.maxY() - border.getBottom(), centerHoriz, border.getBottom());
            if (tile) {
                drawTextureTiled(texture, bottomArea, ux + left, uy + uw - bottom, uw - left - right, bottom);
            } else {
                drawTexture(texture, bottomArea, ScaleMode.STRETCH, ux + left, uy + uw - bottom, uw - left - right, bottom);
            }
            // BOTTOM-RIGHT CORNER
            if (border.getRight() != 0) {
                drawTexture(texture, Rect2i.createFromMinAndSize(region.maxX() - border.getRight(), region.maxY() - border.getBottom(), border.getRight(), border.getBottom()),
                        ScaleMode.STRETCH, ux + uw - right, uy + uw - bottom, right, bottom);
            }
        }
    }

    @Override
    public void drawMaterial(Material material, Rect2i region) {
        if (!state.cropRegion.overlaps(relativeToAbsolute(region))) {
            return;
        }
        material.setFloat("alpha", state.getAlpha());
        material.bindTextures();
        glPushMatrix();
        glTranslatef(state.drawRegion.minX() + region.minX(), state.drawRegion.minY() + region.minY(), 0f);
        glScalef(region.width(), region.height(), 1);
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
        float fitScale = 0.35f * Math.min(region.width(), region.height()) / Math.max(meshExtents.x, Math.max(meshExtents.y, meshExtents.z));
        Vector3f centerOffset = meshAABB.getCenter();
        centerOffset.scale(-1.0f);

        Matrix4f centerTransform = new Matrix4f(IDENTITY_ROT, centerOffset, 1.0f);
        Matrix4f userTransform = new Matrix4f(rotation, offset, -fitScale * scale);
        Matrix4f translateTransform = new Matrix4f(IDENTITY_ROT,
                new Vector3f(state.drawRegion.minX() + region.minX() + region.width() / 2,
                        state.drawRegion.minY() + region.minY() + region.height() / 2, 0), 1);

        userTransform.mul(centerTransform);
        translateTransform.mul(userTransform);

        Matrix4f finalMat = new Matrix4f(modelView);
        finalMat.mul(translateTransform);
        MatrixUtils.matrixToFloatBuffer(finalMat, matrixBuffer);

        Rect2i cropRegion = relativeToAbsolute(region).intersect(state.cropRegion);
        material.setFloat4("croppingBoundaries", cropRegion.minX(), cropRegion.maxX() + 1, cropRegion.minY(), cropRegion.maxY() + 1);
        material.setMatrix4("posMatrix", translateTransform);
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
    }

    @Override
    public void drawMesh(Mesh mesh, Texture texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        meshMat.setTexture("texture", texture);
        drawMesh(mesh, meshMat, region, rotation, offset, scale);
    }

    @Override
    public void addInteractionRegion(InteractionListener listener) {
        addInteractionRegion(Rect2i.createFromMinAndMax(0, 0, size().x, size().y), listener);
    }

    @Override
    public void addInteractionRegion(Rect2i region, InteractionListener listener) {
        Rect2i finalRegion = state.cropRegion.intersect(relativeToAbsolute(region));
        if (!finalRegion.isEmpty()) {
            interactionRegions.addLast(new InteractionRegion(finalRegion, listener));
        }
    }

    private Rect2i relativeToAbsolute(Rect2i region) {
        return Rect2i.createFromMinAndSize(region.minX() + state.drawRegion.minX(), region.minY() + state.drawRegion.minY(), region.width(), region.height());
    }

    /**
     * The state of the canvas
     */
    private static class CanvasState {
        public UISkin skin;
        public String family = "";
        public Class<? extends UIWidget> widget;
        public String mode = "";

        public Rect2i drawRegion;
        public Rect2i cropRegion;

        private float alpha = 1.0f;
        private float baseAlpha = 1.0f;

        public CanvasState(CanvasState previous, Rect2i drawRegion) {
            this(previous, drawRegion, drawRegion);
        }

        public CanvasState(CanvasState previous, Rect2i drawRegion, Rect2i cropRegion) {
            if (previous != null) {
                this.skin = previous.skin;
                this.family = previous.family;
                this.widget = previous.widget;
                this.mode = previous.mode;
                baseAlpha = previous.getAlpha();
            }
            this.drawRegion = drawRegion;
            this.cropRegion = cropRegion;
        }

        public float getAlpha() {
            return alpha * baseAlpha;
        }

        public UIStyle getCurrentStyle() {
            return skin.getStyleFor(family, widget, mode);
        }

        public Rect2i getRelativeRegion() {
            return Rect2i.createFromMinAndMax(0, 0, drawRegion.width(), drawRegion.height());
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
                    state = new CanvasState(state, subRegion, cropRegion);
                } else if (!cropRegion.equals(state.cropRegion)) {
                    state = new CanvasState(state, subRegion, cropRegion);
                    crop(cropRegion);
                    croppingRegion = true;
                } else {
                    state = new CanvasState(state, subRegion);
                }
            } else {
                state = new CanvasState(state, subRegion);
            }
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
                    crop(previousState.cropRegion);
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
        private HorizontalAlign alignment;

        public TextCacheKey(String text, Font font, int maxWidth, HorizontalAlign alignment) {
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

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InteractionRegion) {
                InteractionRegion other = (InteractionRegion) obj;
                return Objects.equals(other.listener, listener);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }
    }
}
