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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.math.AABB;
import org.terasology.math.Border;
import org.terasology.math.MatrixUtils;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseQuat4f;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontMeshBuilder;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshBuilder;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.opengl.FrameBufferObject;
import org.terasology.rendering.opengl.LwjglFrameBufferObject;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
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

/**
 */
public class LwjglCanvasRenderer implements CanvasRenderer {

    private static final String CROPPING_BOUNDARIES_PARAM = "croppingBoundaries";
    private static final Rect2f FULL_REGION = Rect2f.createFromMinAndSize(0, 0, 1, 1);
    private Matrix4f modelView;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private Mesh billboard;
    private Line line = new Line();

    private Material textureMat;

    private final FontMeshBuilder fontMeshBuilder;

    // Text mesh caching
    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    // Texutre mesh caching
    private Map<TextureCacheKey, Mesh> cachedTextures = Maps.newLinkedHashMap();
    private Set<TextureCacheKey> usedTextures = Sets.newHashSet();

    private Rect2i requestedCropRegion;
    private Rect2i currentTextureCropRegion;

    private Map<ResourceUrn, LwjglFrameBufferObject> fboMap = Maps.newHashMap();


    public LwjglCanvasRenderer(Context context) {
        // TODO use context to get assets instead of static methods
        this.textureMat = Assets.getMaterial("engine:UITexture").get();
        this.billboard = Assets.getMesh("engine:UIBillboard").get();
        this.fontMeshBuilder = new FontMeshBuilder(context.get(AssetManager.class).getAsset("engine:UIUnderline", Material.class).get());
        // failure to load these can be due to failing shaders or missing resources
    }

    @Override
    public void preRender() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 0, 2048f);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        modelView = new Matrix4f();
        modelView.setIdentity();
        modelView.setTranslation(new Vector3f(0, 0, -1024f));

        MatrixUtils.matrixToFloatBuffer(modelView, matrixBuffer);
        glLoadMatrix(matrixBuffer);
        matrixBuffer.rewind();

        requestedCropRegion = Rect2i.createFromMinAndSize(0, 0, Display.getWidth(), Display.getHeight());
        currentTextureCropRegion = requestedCropRegion;
        textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX(), requestedCropRegion.maxX() + 1
                , requestedCropRegion.minY(), requestedCropRegion.maxY() + 1);
    }

    @Override
    public void postRender() {
        Iterator<Map.Entry<TextCacheKey, Map<Material, Mesh>>> textIterator = cachedText.entrySet().iterator();
        while (textIterator.hasNext()) {
            Map.Entry<TextCacheKey, Map<Material, Mesh>> entry = textIterator.next();
            if (!usedText.contains(entry.getKey())) {
                entry.getValue().values().forEach(Mesh::dispose);
                textIterator.remove();
            }
        }
        usedText.clear();

        Iterator<Map.Entry<TextureCacheKey, Mesh>> textureIterator = cachedTextures.entrySet().iterator();
        while (textureIterator.hasNext()) {
            Map.Entry<TextureCacheKey, Mesh> entry = textureIterator.next();
            if (!usedTextures.contains(entry.getKey())) {
                entry.getValue().dispose();
                textureIterator.remove();
            }
        }
        usedTextures.clear();

        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rect2i drawRegion, Rect2i cropRegion, Quat4f rotation, Vector3f offset, float scale, float alpha) {
        if (!material.isRenderable()) {
            return;
        }

        AABB meshAABB = mesh.getAABB();
        Vector3f meshExtents = meshAABB.getExtents();
        float fitScale = 0.35f * Math.min(drawRegion.width(), drawRegion.height()) / Math.max(meshExtents.x, Math.max(meshExtents.y, meshExtents.z));
        Vector3f centerOffset = meshAABB.getCenter();
        centerOffset.scale(-1.0f);

        Matrix4f centerTransform = new Matrix4f(BaseQuat4f.IDENTITY, centerOffset, 1.0f);
        Matrix4f userTransform = new Matrix4f(rotation, offset, -fitScale * scale);
        Matrix4f translateTransform = new Matrix4f(BaseQuat4f.IDENTITY,
                new Vector3f(drawRegion.minX() + drawRegion.width() / 2,
                        drawRegion.minY() + drawRegion.height() / 2, 0), 1);

        userTransform.mul(centerTransform);
        translateTransform.mul(userTransform);

        Matrix4f finalMat = new Matrix4f(modelView);
        finalMat.mul(translateTransform);
        MatrixUtils.matrixToFloatBuffer(finalMat, matrixBuffer);

        material.setFloat4(CROPPING_BOUNDARIES_PARAM, cropRegion.minX(), cropRegion.maxX() + 1, cropRegion.minY(), cropRegion.maxY() + 1);
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
        material.setFloat("alpha", alpha);
        material.bindTextures();
        mesh.render();
        if (matrixStackSupported) {
            material.deactivateFeature(ShaderProgramFeature.FEATURE_USE_MATRIX_STACK);
        }

        glPopMatrix();
        glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public Vector2i getTargetSize() {
        return new Vector2i(Display.getWidth(), Display.getHeight());
    }

    @Override
    public void drawMaterialAt(Material material, Rect2i drawRegion) {
        glPushMatrix();
        glTranslatef(drawRegion.minX(), drawRegion.minY(), 0f);
        glScalef(drawRegion.width(), drawRegion.height(), 1);
        billboard.render();
        glPopMatrix();
    }

    @Override
    public void drawLine(int sx, int sy, int ex, int ey, Color color) {
        line.draw(sx, sy, ex, ey, 2, color, color, 0);
    }

    @Override
    public void crop(Rect2i cropRegion) {
        requestedCropRegion = cropRegion;
    }

    @Override
    public FrameBufferObject getFBO(ResourceUrn urn, BaseVector2i size) {
        LwjglFrameBufferObject frameBufferObject = fboMap.get(urn);
        if (frameBufferObject == null || !Assets.getTexture(urn).isPresent()) {
            // If a FBO exists, but no texture, then the texture was disposed
            // TODO: update fboMap whenever a texture is disposed (or convert FBO instances to assets?)
            if (frameBufferObject != null) {
                frameBufferObject.dispose();
            }
            frameBufferObject = new LwjglFrameBufferObject(urn, size);
            fboMap.put(urn, frameBufferObject);
        }
        return frameBufferObject;
    }

    @Override
    public void drawTexture(TextureRegion texture, Color color, ScaleMode mode, Rect2i absoluteRegion,
                            float ux, float uy, float uw, float uh, float alpha) {
        if (!texture.getTexture().isLoaded()) {
            return;
        }

        if (!currentTextureCropRegion.equals(requestedCropRegion)
                && !(currentTextureCropRegion.contains(absoluteRegion) && requestedCropRegion.contains(absoluteRegion))) {
            textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX(), requestedCropRegion.maxX() + 1,
                    requestedCropRegion.minY(), requestedCropRegion.maxY() + 1);
            currentTextureCropRegion = requestedCropRegion;
        }

        Vector2f scale = mode.scaleForRegion(absoluteRegion, texture.getWidth(), texture.getHeight());
        Rect2f textureArea = texture.getRegion();
        Mesh mesh = billboard;
        switch (mode) {
            case TILED: {
                TextureCacheKey key = new TextureCacheKey(texture.size(), absoluteRegion.size());
                usedTextures.add(key);
                mesh = cachedTextures.get(key);
                if (mesh == null || mesh.isDisposed()) {
                    MeshBuilder builder = new MeshBuilder();
                    addTiles(builder, absoluteRegion, FULL_REGION, texture.size(), FULL_REGION);
                    mesh = builder.build();
                    cachedTextures.put(key, mesh);
                }
                textureMat.setFloat2("scale", scale);
                textureMat.setFloat2("offset",
                        absoluteRegion.minX(),
                        absoluteRegion.minY());

                textureMat.setFloat2("texOffset", textureArea.minX() + ux * textureArea.width(), textureArea.minY() + uy * textureArea.height());
                textureMat.setFloat2("texSize", uw * textureArea.width(), uh * textureArea.height());
                break;
            }
            case SCALE_FILL: {
                textureMat.setFloat2("offset", absoluteRegion.minX(), absoluteRegion.minY());
                textureMat.setFloat2("scale", absoluteRegion.width(), absoluteRegion.height());

                float texBorderX = (scale.x - absoluteRegion.width()) / scale.x * uw;
                float texBorderY = (scale.y - absoluteRegion.height()) / scale.y * uh;

                textureMat.setFloat2("texOffset", textureArea.minX() + (ux + 0.5f * texBorderX) * textureArea.width()
                        , textureArea.minY() + (uy + 0.5f * texBorderY) * textureArea.height());
                textureMat.setFloat2("texSize", (uw - texBorderX) * textureArea.width(), (uh - texBorderY) * textureArea.height());
                break;
            }
            default: {
                textureMat.setFloat2("scale", scale);
                textureMat.setFloat2("offset",
                        absoluteRegion.minX() + 0.5f * (absoluteRegion.width() - scale.x),
                        absoluteRegion.minY() + 0.5f * (absoluteRegion.height() - scale.y));

                textureMat.setFloat2("texOffset", textureArea.minX() + ux * textureArea.width(), textureArea.minY() + uy * textureArea.height());
                textureMat.setFloat2("texSize", uw * textureArea.width(), uh * textureArea.height());
                break;
            }
        }

        textureMat.setTexture("texture", texture.getTexture());
        textureMat.setFloat4("color", color.rf(), color.gf(), color.bf(), color.af() * alpha);
        textureMat.bindTextures();
        mesh.render();
    }

    @Override
    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rect2i absoluteRegion,
                         Color color, Color shadowColor, float alpha, boolean underlined) {
        TextCacheKey key = new TextCacheKey(text, font, absoluteRegion.width(), hAlign, color, shadowColor, underlined);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = TextLineBuilder.getLines(font, text, absoluteRegion.width());
        if (fontMesh != null) {
            for (Mesh mesh : fontMesh.values()) {
                if (mesh.isDisposed()) {
                    fontMesh = null;
                    break;
                }
            }
        }
        if (fontMesh == null) {
            fontMesh = fontMeshBuilder.createTextMesh(font, lines, absoluteRegion.width(), hAlign, color, shadowColor, underlined);
            cachedText.put(key, fontMesh);
        }

        Vector2i offset = new Vector2i(absoluteRegion.minX(), absoluteRegion.minY());
        offset.y += vAlign.getOffset(lines.size() * font.getLineHeight(), absoluteRegion.height());

        fontMesh.entrySet().stream().filter(entry -> entry.getKey().isRenderable()).forEach(entry -> {
            entry.getKey().bindTextures();
            entry.getKey().setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX(), requestedCropRegion.maxX() + 1,
                    requestedCropRegion.minY(), requestedCropRegion.maxY() + 1);
            entry.getKey().setFloat2("offset", offset.x, offset.y);
            entry.getKey().setFloat("alpha", alpha);
            entry.getValue().render();
        });
    }

    @Override
    public void drawTextureBordered(TextureRegion texture, Rect2i region, Border border, boolean tile, float ux, float uy, float uw, float uh, float alpha) {
        if (!texture.getTexture().isLoaded()) {
            return;
        }

        if (!currentTextureCropRegion.equals(requestedCropRegion)
                && !(currentTextureCropRegion.contains(region) && requestedCropRegion.contains(region))) {
            textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX(), requestedCropRegion.maxX() + 1,
                    requestedCropRegion.minY(), requestedCropRegion.maxY() + 1);
            currentTextureCropRegion = requestedCropRegion;
        }

        Vector2i textureSize = new Vector2i(TeraMath.ceilToInt(texture.getWidth() * uw), TeraMath.ceilToInt(texture.getHeight() * uh));

        TextureCacheKey key = new TextureCacheKey(textureSize, region.size(), border, tile);
        usedTextures.add(key);
        Mesh mesh = cachedTextures.get(key);
        if (mesh == null || mesh.isDisposed()) {
            MeshBuilder builder = new MeshBuilder();


            float topTex = (float) border.getTop() / textureSize.y;
            float leftTex = (float) border.getLeft() / textureSize.x;
            float bottomTex = 1f - (float) border.getBottom() / textureSize.y;
            float rightTex = 1f - (float) border.getRight() / textureSize.x;
            int centerHoriz = region.width() - border.getTotalWidth();
            int centerVert = region.height() - border.getTotalHeight();

            float top = (float) border.getTop() / region.height();
            float left = (float) border.getLeft() / region.width();
            float bottom = 1f - (float) border.getBottom() / region.height();
            float right = 1f - (float) border.getRight() / region.width();

            if (border.getTop() != 0) {
                if (border.getLeft() != 0) {
                    addRectPoly(builder, 0, 0, left, top, 0, 0, leftTex, topTex);
                }
                if (tile) {
                    addTiles(builder, Rect2i.createFromMinAndSize(border.getLeft(), 0, centerHoriz, border.getTop()), Rect2f.createFromMinAndMax(left, 0, right, top),
                            new Vector2i(textureSize.x - border.getTotalWidth(), border.getTop()),
                            Rect2f.createFromMinAndMax(leftTex, 0, rightTex, topTex));
                } else {
                    addRectPoly(builder, left, 0, right, top, leftTex, 0, rightTex, topTex);
                }
                if (border.getRight() != 0) {
                    addRectPoly(builder, right, 0, 1, top, rightTex, 0, 1, topTex);
                }
            }

            if (border.getLeft() != 0) {
                if (tile) {
                    addTiles(builder, Rect2i.createFromMinAndSize(0, border.getTop(), border.getLeft(), centerVert), Rect2f.createFromMinAndMax(0, top, left, bottom),
                            new Vector2i(border.getLeft(), textureSize.y - border.getTotalHeight()),
                            Rect2f.createFromMinAndMax(0, topTex, leftTex, bottomTex));
                } else {
                    addRectPoly(builder, 0, top, left, bottom, 0, topTex, leftTex, bottomTex);
                }
            }

            if (tile) {
                addTiles(builder, Rect2i.createFromMinAndSize(border.getLeft(), border.getTop(), centerHoriz, centerVert),
                        Rect2f.createFromMinAndMax(left, top, right, bottom),
                        new Vector2i(textureSize.x - border.getTotalWidth(), textureSize.y - border.getTotalHeight()),
                        Rect2f.createFromMinAndMax(leftTex, topTex, rightTex, bottomTex));
            } else {
                addRectPoly(builder, left, top, right, bottom, leftTex, topTex, rightTex, bottomTex);
            }

            if (border.getRight() != 0) {
                if (tile) {
                    addTiles(builder, Rect2i.createFromMinAndSize(region.width() - border.getRight(), border.getTop(), border.getRight(), centerVert),
                            Rect2f.createFromMinAndMax(right, top, 1, bottom),
                            new Vector2i(border.getRight(), textureSize.y - border.getTotalHeight()),
                            Rect2f.createFromMinAndMax(rightTex, topTex, 1, bottomTex));
                } else {
                    addRectPoly(builder, right, top, 1, bottom, rightTex, topTex, 1, bottomTex);
                }
            }

            if (border.getBottom() != 0) {
                if (border.getLeft() != 0) {
                    addRectPoly(builder, 0, bottom, left, 1, 0, bottomTex, leftTex, 1);
                }
                if (tile) {
                    addTiles(builder, Rect2i.createFromMinAndSize(border.getLeft(), region.height() - border.getBottom(), centerHoriz, border.getBottom()),
                            Rect2f.createFromMinAndMax(left, bottom, right, 1),
                            new Vector2i(textureSize.x - border.getTotalWidth(), border.getBottom()),
                            Rect2f.createFromMinAndMax(leftTex, bottomTex, rightTex, 1));
                } else {
                    addRectPoly(builder, left, bottom, right, 1, leftTex, bottomTex, rightTex, 1);
                }
                if (border.getRight() != 0) {
                    addRectPoly(builder, right, bottom, 1, 1, rightTex, bottomTex, 1, 1);
                }
            }

            mesh = builder.build();
            cachedTextures.put(key, mesh);
        }
        textureMat.setFloat2("scale", region.width(), region.height());
        textureMat.setFloat2("offset", region.minX(), region.minY());

        Rect2f textureArea = texture.getRegion();
        textureMat.setFloat2("texOffset", textureArea.minX() + ux * textureArea.width(), textureArea.minY() + uy * textureArea.height());
        textureMat.setFloat2("texSize", uw * textureArea.width(), uh * textureArea.height());

        textureMat.setTexture("texture", texture.getTexture());
        textureMat.setFloat4("color", 1, 1, 1, alpha);
        textureMat.bindTextures();
        mesh.render();
    }

    private void addRectPoly(MeshBuilder builder, float minX, float minY, float maxX, float maxY, float texMinX, float texMinY, float texMaxX, float texMaxY) {
        builder.addPoly(new Vector3f(minX, minY, 0), new Vector3f(maxX, minY, 0), new Vector3f(maxX, maxY, 0), new Vector3f(minX, maxY, 0));
        builder.addTexCoord(texMinX, texMinY);
        builder.addTexCoord(texMaxX, texMinY);
        builder.addTexCoord(texMaxX, texMaxY);
        builder.addTexCoord(texMinX, texMaxY);
    }

    private void addTiles(MeshBuilder builder, Rect2i drawRegion, Rect2f subDrawRegion, Vector2i textureSize, Rect2f subTextureRegion) {
        int tileW = textureSize.x;
        int tileH = textureSize.y;
        int horizTiles = TeraMath.fastAbs((drawRegion.width() - 1) / tileW) + 1;
        int vertTiles = TeraMath.fastAbs((drawRegion.height() - 1) / tileH) + 1;

        int offsetX = (drawRegion.width() - horizTiles * tileW) / 2;
        int offsetY = (drawRegion.height() - vertTiles * tileH) / 2;

        for (int tileY = 0; tileY < vertTiles; tileY++) {
            for (int tileX = 0; tileX < horizTiles; tileX++) {
                int left = offsetX + tileW * tileX;
                int top = offsetY + tileH * tileY;

                float vertLeft = subDrawRegion.minX() + subDrawRegion.width() * Math.max((float) left / drawRegion.width(), 0);
                float vertTop = subDrawRegion.minY() + subDrawRegion.height() * Math.max((float) top / drawRegion.height(), 0);
                float vertRight = subDrawRegion.minX() + subDrawRegion.width() * Math.min((float) (left + tileW) / drawRegion.width(), 1);
                float vertBottom = subDrawRegion.minY() + subDrawRegion.height() * Math.min((float) (top + tileH) / drawRegion.height(), 1);
                float texCoordLeft = subTextureRegion.minX() + subTextureRegion.width() * (Math.max(left, 0) - left) / tileW;
                float texCoordTop = subTextureRegion.minY() + subTextureRegion.height() * (Math.max(top, 0) - top) / tileH;
                float texCoordRight = subTextureRegion.minX() + subTextureRegion.width() * (Math.min(left + tileW, drawRegion.width()) - left) / tileW;
                float texCoordBottom = subTextureRegion.minY() + subTextureRegion.height() * (Math.min(top + tileH, drawRegion.height()) - top) / tileH;

                addRectPoly(builder, vertLeft, vertTop, vertRight, vertBottom, texCoordLeft, texCoordTop, texCoordRight, texCoordBottom);
            }
        }
    }

    /**
     * A key that identifies an entry in the text cache. It contains the elements that affect the generation of mesh for text rendering.
     */
    private static class TextCacheKey {
        private final String text;
        private final Font font;
        private final int width;
        private final HorizontalAlign alignment;
        private final Color baseColor;
        private final Color shadowColor;
        private final boolean underlined;

        public TextCacheKey(String text, Font font, int maxWidth, HorizontalAlign alignment, Color baseColor, Color shadowColor, boolean underlined) {
            this.text = text;
            this.font = font;
            this.width = maxWidth;
            this.alignment = alignment;
            this.baseColor = baseColor;
            this.shadowColor = shadowColor;
            this.underlined = underlined;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TextCacheKey) {
                TextCacheKey other = (TextCacheKey) obj;
                return Objects.equals(text, other.text) && Objects.equals(font, other.font)
                        && Objects.equals(width, other.width) && Objects.equals(alignment, other.alignment)
                        && Objects.equals(baseColor, other.baseColor) && Objects.equals(shadowColor, other.shadowColor)
                        && Objects.equals(underlined, other.underlined);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, font, width, alignment, baseColor, shadowColor, underlined);
        }
    }

    /**
     * A key that identifies an entry in the texture cache. It contains the elements that affect the generation of mesh for texture rendering.
     */
    private static class TextureCacheKey {

        private Vector2i textureSize;
        private Vector2i areaSize;
        private Border border;
        private boolean tiled;

        public TextureCacheKey(Vector2i textureSize, Vector2i areaSize) {
            this.textureSize = new Vector2i(textureSize);
            this.areaSize = new Vector2i(areaSize);
            this.border = Border.ZERO;
            this.tiled = true;
        }

        public TextureCacheKey(Vector2i textureSize, Vector2i areaSize, Border border, boolean tiled) {
            this.textureSize = new Vector2i(textureSize);
            this.areaSize = new Vector2i(areaSize);
            this.border = border;
            this.tiled = tiled;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TextureCacheKey) {
                TextureCacheKey other = (TextureCacheKey) obj;
                return Objects.equals(textureSize, other.textureSize) && Objects.equals(areaSize, other.areaSize)
                        && Objects.equals(border, other.border) && tiled == other.tiled;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(textureSize, areaSize, border, tiled);
        }
    }
}
