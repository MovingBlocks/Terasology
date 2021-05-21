// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.internal;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.assets.font.FontMeshBuilder;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshBuilder;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.rendering.opengl.FrameBufferObject;
import org.terasology.engine.rendering.opengl.LwjglFrameBufferObject;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.AABBfc;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.TeraMath;
import org.terasology.nui.Border;
import org.terasology.nui.Colorc;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.ScaleMode;
import org.terasology.nui.TextLineBuilder;
import org.terasology.nui.UITextureRegion;
import org.terasology.nui.VerticalAlign;
import org.terasology.nui.asset.font.Font;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

public class LwjglCanvasRenderer implements TerasologyCanvasRenderer, PropertyChangeListener {

    private static final String CROPPING_BOUNDARIES_PARAM = "croppingBoundaries";
    private static final Rectanglef FULL_REGION = new Rectanglef(0, 0, 1, 1);
    private Mesh billboard;

    private Material textureMat;

    private final FontMeshBuilder fontMeshBuilder;

    // Text mesh caching
    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    // Texture mesh caching
    private Map<TextureCacheKey, Mesh> cachedTextures = Maps.newLinkedHashMap();
    private Set<TextureCacheKey> usedTextures = Sets.newHashSet();

    private Rectanglei requestedCropRegion;
    private Rectanglei currentTextureCropRegion;

    private Map<ResourceUrn, LwjglFrameBufferObject> fboMap = Maps.newHashMap();
    private RenderingConfig renderingConfig;
    private DisplayDevice displayDevice;
    private float uiScale = 1f;

    private Matrix4fStack modelMatrixStack = new Matrix4fStack(1000);
    private Matrix4f projMatrix = new Matrix4f();

    public LwjglCanvasRenderer(Context context) {
        // TODO use context to get assets instead of static methods
        this.textureMat = Assets.getMaterial("engine:UITexture").orElseThrow(
                // Extra attention to how this is reported because it's often the first texture
                // engine tries to load; the build is probably broken.
                () -> new RuntimeException("Failing to find engine textures"));
        this.billboard = Assets.getMesh("engine:UIBillboard").get();
        this.fontMeshBuilder = new FontMeshBuilder(context.get(AssetManager.class).getAsset("engine:UIUnderline", Material.class).get());
        // failure to load these can be due to failing shaders or missing resources

        this.renderingConfig = context.get(Config.class).getRendering();
        this.displayDevice = context.get(DisplayDevice.class);
        this.uiScale = this.renderingConfig.getUiScale() / 100f;

        this.renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);
    }

    @Override
    public void preRender() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // TODO: figure out previous call to viewport scaling is handled
        // changing resolution scaling affect viewport of LWJGLCanvas causing strange scaling artifact.
        glViewport(0, 0, displayDevice.getWidth(), displayDevice.getHeight());
        projMatrix.setOrtho(0, displayDevice.getWidth(), displayDevice.getHeight(),0,0, 2048f);
        modelMatrixStack.pushMatrix();
        modelMatrixStack.set(new Matrix4f().setTranslation(0,0,-1024));
        modelMatrixStack.scale(uiScale, uiScale, uiScale);

        requestedCropRegion = new Rectanglei(0, 0,displayDevice.getWidth(), displayDevice.getHeight());
        currentTextureCropRegion = requestedCropRegion;
        textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX, requestedCropRegion.maxX,
                requestedCropRegion.minY, requestedCropRegion.maxY);
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

        modelMatrixStack.popMatrix();
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rectanglei drawRegion, Rectanglei cropRegion, Quaternionfc rotation, Vector3fc offset, float scale, float alpha) {
        if (!material.isRenderable()) {
            return;
        }

        AABBfc meshAABB = mesh.getAABB();
        Vector3f meshExtents = meshAABB.extent(new Vector3f());
        float fitScale = 0.35f * Math.min(drawRegion.getSizeX(), drawRegion.getSizeY()) / Math.max(meshExtents.x, Math.max(meshExtents.y, meshExtents.z));
        Vector3f centerOffset = meshAABB.center(new Vector3f());
        centerOffset.mul(-1.0f);

        Matrix4f centerTransform = new Matrix4f().translationRotateScale(centerOffset,new Quaternionf(),1);
        Matrix4f userTransform = new Matrix4f().translationRotateScale( offset,rotation, -fitScale * scale);
        Matrix4f translateTransform = new Matrix4f().translationRotateScale(
                new Vector3f((drawRegion.minX + drawRegion.getSizeX() / 2) * uiScale,
                        (drawRegion.minY + drawRegion.getSizeY() / 2) * uiScale, 0), new Quaternionf(), 1);

        userTransform.mul(centerTransform);
        translateTransform.mul(userTransform);

        Matrix4f finalMat = new Matrix4f().setTranslation(0, 0, -1024f);
        finalMat.mul(translateTransform);

        material.setFloat4(
                CROPPING_BOUNDARIES_PARAM,
                cropRegion.minX * uiScale,
                cropRegion.maxX * uiScale,
                cropRegion.minY * uiScale,
                cropRegion.maxY * uiScale);

        glEnable(GL11.GL_DEPTH_TEST);
        glClear(GL11.GL_DEPTH_BUFFER_BIT);

        modelMatrixStack.pushMatrix();
        modelMatrixStack.set(finalMat);
        modelMatrixStack.scale(this.uiScale, this.uiScale, this.uiScale);

        material.setMatrix4("posMatrix", translateTransform);
        material.setMatrix4("projectionMatrix", projMatrix);
        material.setMatrix4("modelViewMatrix", modelMatrixStack);
        material.setMatrix3("normalMatrix", modelMatrixStack.normal(new Matrix3f()));

        material.setFloat("alpha", alpha);
        material.bindTextures();
        mesh.render();

        modelMatrixStack.popMatrix();
        glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public org.joml.Vector2i getTargetSize() {
        return new org.joml.Vector2i(displayDevice.getWidth(), displayDevice.getHeight());
    }

    @Override
    // so this is unused??
    public void drawMaterialAt(Material material, Rectanglei drawRegion) {
        modelMatrixStack.pushMatrix();
        modelMatrixStack.translate(drawRegion.minX, drawRegion.minY, 0f);
        modelMatrixStack.scale(drawRegion.getSizeX(), drawRegion.getSizeY(), 1);

        material.setMatrix4("projectionMatrix", projMatrix);
        material.setMatrix4("modelViewMatrix", modelMatrixStack);
        billboard.render();
        modelMatrixStack.popMatrix();
    }

    @Override
    public void drawLine(int sx, int sy, int ex, int ey, Colorc color) {
        LineRenderer.draw(sx, sy, ex, ey, 2, color, color, 0);
    }

    @Override
    public void crop(Rectanglei cropRegion) {
        requestedCropRegion = new Rectanglei(cropRegion);
    }


    @Override
    public FrameBufferObject getFBO(ResourceUrn urn, Vector2ic size) {
        LwjglFrameBufferObject frameBufferObject = fboMap.get(urn);
        if (frameBufferObject == null || !Assets.getTexture(urn).isPresent()) {
            // If a FBO exists, but no texture, then the texture was disposed
            // TODO: update fboMap whenever a texture is disposed (or convert FBO instances to assets?)
            if (frameBufferObject != null) {
                frameBufferObject.dispose();
            }
            frameBufferObject = new LwjglFrameBufferObject(displayDevice, urn, size);
            fboMap.put(urn, frameBufferObject);
        }
        return frameBufferObject;
    }

    @Override
    public void drawTexture(UITextureRegion texture, Colorc color, ScaleMode mode, Rectanglei absoluteRegionRectangle,
                            float ux, float uy, float uw, float uh, float alpha) {
        if (!((TextureRegion)texture).getTexture().isLoaded()) {
            return;
        }

        Rectanglei absoluteRegion = new Rectanglei(absoluteRegionRectangle);

        if (!currentTextureCropRegion.equals(requestedCropRegion)
                && !(currentTextureCropRegion.containsRectangle(absoluteRegion) && requestedCropRegion.containsRectangle(absoluteRegion))) {
            textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX, requestedCropRegion.maxX,
                    requestedCropRegion.minY, requestedCropRegion.maxY);
            currentTextureCropRegion = requestedCropRegion;
        }

        Vector2f scale = mode.scaleForRegion(absoluteRegionRectangle, texture.getWidth(), texture.getHeight());
        Rectanglef textureArea = new Rectanglef(texture.getRegion());
        Mesh mesh = billboard;
        switch (mode) {
            case TILED: {
                Vector2i textureSize = texture.size();
                TextureCacheKey key = new TextureCacheKey(textureSize, new Vector2i(absoluteRegion.getSizeX(),absoluteRegion.getSizeY()));
                usedTextures.add(key);
                mesh = cachedTextures.get(key);
                if (mesh == null || mesh.isDisposed()) {
                    MeshBuilder builder = new MeshBuilder();
                    addTiles(builder, absoluteRegion, FULL_REGION, textureSize, FULL_REGION);
                    mesh = builder.build();
                    cachedTextures.put(key, mesh);
                }
                textureMat.setFloat2("scale", scale);
                textureMat.setFloat2("offset",
                        absoluteRegion.minX,
                        absoluteRegion.minY);

                textureMat.setFloat2("texOffset", textureArea.minX + ux * textureArea.getSizeX(), textureArea.minY + uy * textureArea.getSizeY());
                textureMat.setFloat2("texSize", uw * textureArea.getSizeX(), uh * textureArea.getSizeY());
                break;
            }
            case SCALE_FILL: {
                textureMat.setFloat2("offset", absoluteRegion.minX, absoluteRegion.minY);
                textureMat.setFloat2("scale", absoluteRegion.getSizeX(), absoluteRegion.getSizeY());

                float texBorderX = (scale.x - absoluteRegion.getSizeX()) / scale.x * uw;
                float texBorderY = (scale.y - absoluteRegion.getSizeY()) / scale.y * uh;

                textureMat.setFloat2("texOffset", textureArea.minX + (ux + 0.5f * texBorderX) * textureArea.getSizeX(),
                        textureArea.minY + (uy + 0.5f * texBorderY) * textureArea.getSizeY());
                textureMat.setFloat2("texSize", (uw - texBorderX) * textureArea.getSizeX(), (uh - texBorderY) * textureArea.getSizeY());
                break;
            }
            default: {
                textureMat.setFloat2("scale", scale);
                textureMat.setFloat2("offset",
                        absoluteRegion.minX + 0.5f * (absoluteRegion.getSizeX() - scale.x),
                        absoluteRegion.minY + 0.5f * (absoluteRegion.getSizeY() - scale.y));

                textureMat.setFloat2("texOffset", textureArea.minX + ux * textureArea.getSizeX(), textureArea.minY + uy * textureArea.getSizeY());
                textureMat.setFloat2("texSize", uw * textureArea.getSizeX(), uh * textureArea.getSizeY());
                break;
            }
        }

        textureMat.setTexture("texture", ((TextureRegion)texture).getTexture());
        textureMat.setFloat4("color", color.rf(), color.gf(), color.bf(), color.af() * alpha);

        textureMat.setMatrix4("projectionMatrix", projMatrix);
        textureMat.setMatrix4("modelViewMatrix", modelMatrixStack);

        textureMat.bindTextures();
        mesh.render();
    }

    @Override
    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rectanglei absoluteRegionRectangle,
                         Colorc color, Colorc shadowColor, float alpha, boolean underlined) {
        Rectanglei absoluteRegion = new Rectanglei(absoluteRegionRectangle);

        TextCacheKey key = new TextCacheKey(text, font, absoluteRegion.getSizeX(), hAlign, color, shadowColor, underlined);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = TextLineBuilder.getLines(font, text, absoluteRegion.getSizeX());
        if (fontMesh != null) {
            for (Mesh mesh : fontMesh.values()) {
                if (mesh.isDisposed()) {
                    fontMesh = null;
                    break;
                }
            }
        }
        if (fontMesh == null) {
            fontMesh = fontMeshBuilder.createTextMesh((org.terasology.engine.rendering.assets.font.Font)font, lines, absoluteRegion.getSizeX(), hAlign, color, shadowColor, underlined);
            cachedText.put(key, fontMesh);
        }

        Vector2i offset = new Vector2i(absoluteRegion.minX, absoluteRegion.minY);
        offset.y += vAlign.getOffset(lines.size() * font.getLineHeight(), absoluteRegion.lengthY());

        fontMesh.entrySet().stream().filter(entry -> entry.getKey().isRenderable()).forEach(entry -> {
            entry.getKey().bindTextures();
            entry.getKey().setMatrix4("projectionMatrix", projMatrix);
            entry.getKey().setMatrix4("modelViewMatrix", modelMatrixStack);
            entry.getKey().setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX, requestedCropRegion.maxX,
                    requestedCropRegion.minY, requestedCropRegion.maxY);
            entry.getKey().setFloat2("offset", offset.x, offset.y);
            entry.getKey().setFloat("alpha", alpha);
            entry.getValue().render();
        });
    }

    @Override
    public void drawTextureBordered(UITextureRegion texture, Rectanglei regionRectangle, Border border, boolean tile,
                                    float ux, float uy, float uw, float uh, float alpha) {
        if (!((TextureRegion) texture).getTexture().isLoaded()) {
            return;
        }

        Rectanglei region = new Rectanglei(regionRectangle);

        if (!currentTextureCropRegion.equals(requestedCropRegion)
            && !(currentTextureCropRegion.containsRectangle(region) && requestedCropRegion.containsRectangle(region))) {
            textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX, requestedCropRegion.maxX,
                requestedCropRegion.minY, requestedCropRegion.maxY);
            currentTextureCropRegion = requestedCropRegion;
        }

        Vector2i textureSize = new Vector2i(TeraMath.ceilToInt(texture.getWidth() * uw), TeraMath.ceilToInt(texture.getHeight() * uh));

        TextureCacheKey key = new TextureCacheKey(textureSize, new Vector2i(region.getSizeX(), region.getSizeY()), border, tile);
        usedTextures.add(key);
        Mesh mesh = cachedTextures.get(key);
        if (mesh == null || mesh.isDisposed()) {
            MeshBuilder builder = new MeshBuilder();


            float topTex = (float) border.getTop() / textureSize.y;
            float leftTex = (float) border.getLeft() / textureSize.x;
            float bottomTex = 1f - (float) border.getBottom() / textureSize.y;
            float rightTex = 1f - (float) border.getRight() / textureSize.x;
            int centerHoriz = region.getSizeX() - border.getTotalWidth();
            int centerVert = region.getSizeY() - border.getTotalHeight();

            float top = (float) border.getTop() / region.getSizeY();
            float left = (float) border.getLeft() / region.getSizeX();
            float bottom = 1f - (float) border.getBottom() / region.getSizeY();
            float right = 1f - (float) border.getRight() / region.getSizeX();

            if (border.getTop() != 0) {
                if (border.getLeft() != 0) {
                    addRectPoly(builder, 0, 0, left, top, 0, 0, leftTex, topTex);
                }
                if (tile) {

                    addTiles(builder, new Rectanglei(border.getLeft(), 0).setSize(centerHoriz, border.getTop()),
                        new Rectanglef(left, 0, right, top),
                        new Vector2i(textureSize.x - border.getTotalWidth(), border.getTop()),
                        new Rectanglef(leftTex, 0, rightTex, topTex));
                } else {
                    addRectPoly(builder, left, 0, right, top, leftTex, 0, rightTex, topTex);
                }
                if (border.getRight() != 0) {
                    addRectPoly(builder, right, 0, 1, top, rightTex, 0, 1, topTex);
                }
            }

            if (border.getLeft() != 0) {
                if (tile) {
                    addTiles(builder, new Rectanglei(0, border.getTop()).setSize(border.getLeft(), centerVert),
                        new Rectanglef(0, top, left, bottom),
                        new Vector2i(border.getLeft(), textureSize.y - border.getTotalHeight()),
                        new Rectanglef(0, topTex, leftTex, bottomTex));
                } else {
                    addRectPoly(builder, 0, top, left, bottom, 0, topTex, leftTex, bottomTex);
                }
            }

            if (tile) {
                addTiles(builder, new Rectanglei(border.getLeft(), border.getTop()).setSize(centerHoriz, centerVert),
                    new Rectanglef(left, top, right, bottom),
                    new Vector2i(textureSize.x - border.getTotalWidth(), textureSize.y - border.getTotalHeight()),
                    new Rectanglef(leftTex, topTex, rightTex, bottomTex));
            } else {
                addRectPoly(builder, left, top, right, bottom, leftTex, topTex, rightTex, bottomTex);
            }

            if (border.getRight() != 0) {
                if (tile) {
                    addTiles(builder, new Rectanglei(region.getSizeX() - border.getRight(), border.getTop()).setSize(border.getRight(), centerVert),
                        new Rectanglef(right, top, 1, bottom),
                        new Vector2i(border.getRight(), textureSize.y - border.getTotalHeight()),
                        new Rectanglef(rightTex, topTex, 1, bottomTex));
                } else {
                    addRectPoly(builder, right, top, 1, bottom, rightTex, topTex, 1, bottomTex);
                }
            }

            if (border.getBottom() != 0) {
                if (border.getLeft() != 0) {
                    addRectPoly(builder, 0, bottom, left, 1, 0, bottomTex, leftTex, 1);
                }
                if (tile) {
                    addTiles(builder, new Rectanglei(border.getLeft(), region.getSizeY() - border.getBottom()).setSize(centerHoriz, border.getBottom()),
                        new Rectanglef(left, bottom, right, 1),
                        new Vector2i(textureSize.x - border.getTotalWidth(), border.getBottom()),
                        new Rectanglef(leftTex, bottomTex, rightTex, 1));
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
        textureMat.setFloat2("scale", region.getSizeX(), region.getSizeY());
        textureMat.setFloat2("offset", region.minX, region.minY);

        Rectanglef textureArea = texture.getRegion();
        textureMat.setFloat2("texOffset", textureArea.minX + ux * textureArea.lengthX(), textureArea.minY + uy * textureArea.lengthY());
        textureMat.setFloat2("texSize", uw * textureArea.lengthX(), uh * textureArea.lengthY());

        textureMat.setTexture("texture", ((TextureRegion) texture).getTexture());
        textureMat.setFloat4("color", 1, 1, 1, alpha);
        textureMat.bindTextures();
        mesh.render();
    }

    @Override
    public void setUiScale(float uiScale) {
        // TODO: Implement? See https://github.com/MovingBlocks/TeraNUI/pull/2/commits/84ea7f936008fe123d3d6cf9d0d164b15b27cd6d
    }

    private void addRectPoly(MeshBuilder builder, float minX, float minY, float maxX, float maxY, float texMinX, float texMinY, float texMaxX, float texMaxY) {
        builder.addPoly(new Vector3f(minX, minY, 0), new Vector3f(maxX, minY, 0), new Vector3f(maxX, maxY, 0), new Vector3f(minX, maxY, 0));
        builder.addTexCoord(texMinX, texMinY);
        builder.addTexCoord(texMaxX, texMinY);
        builder.addTexCoord(texMaxX, texMaxY);
        builder.addTexCoord(texMinX, texMaxY);
    }

    private void addTiles(MeshBuilder builder, Rectanglei drawRegion, Rectanglef subDrawRegion, Vector2i textureSize, Rectanglef subTextureRegion) {
        int tileW = textureSize.x;
        int tileH = textureSize.y;
        int horizTiles = TeraMath.fastAbs((drawRegion.getSizeX() - 1) / tileW) + 1;
        int vertTiles = TeraMath.fastAbs((drawRegion.getSizeY() - 1) / tileH) + 1;

        int offsetX = (drawRegion.getSizeX() - horizTiles * tileW) / 2;
        int offsetY = (drawRegion.getSizeY() - vertTiles * tileH) / 2;

        for (int tileY = 0; tileY < vertTiles; tileY++) {
            for (int tileX = 0; tileX < horizTiles; tileX++) {
                int left = offsetX + tileW * tileX;
                int top = offsetY + tileH * tileY;

                float vertLeft = subDrawRegion.minX + subDrawRegion.getSizeX() * Math.max((float) left / drawRegion.getSizeX(), 0);
                float vertTop = subDrawRegion.minY + subDrawRegion.getSizeY() * Math.max((float) top / drawRegion.getSizeY(), 0);
                float vertRight = subDrawRegion.minX + subDrawRegion.getSizeX() * Math.min((float) (left + tileW) / drawRegion.getSizeX(), 1);
                float vertBottom = subDrawRegion.minY + subDrawRegion.getSizeY() * Math.min((float) (top + tileH) / drawRegion.getSizeY(), 1);
                float texCoordLeft = subTextureRegion.minX + subTextureRegion.getSizeX() * (Math.max(left, 0) - left) / tileW;
                float texCoordTop = subTextureRegion.minY + subTextureRegion.getSizeY() * (Math.max(top, 0) - top) / tileH;
                float texCoordRight = subTextureRegion.minX + subTextureRegion.getSizeX() * (Math.min(left + tileW, drawRegion.getSizeX()) - left) / tileW;
                float texCoordBottom = subTextureRegion.minY + subTextureRegion.getSizeY() * (Math.min(top + tileH, drawRegion.getSizeY()) - top) / tileH;

                addRectPoly(builder, vertLeft, vertTop, vertRight, vertBottom, texCoordLeft, texCoordTop, texCoordRight, texCoordBottom);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RenderingConfig.UI_SCALE)) {
            this.uiScale = this.renderingConfig.getUiScale() / 100f;
        }
    }

    /**
     * A key that identifies an entry in the text cache. It contains the elements that affect the generation of mesh for
     * text rendering.
     */
    private static class TextCacheKey {
        private final String text;
        private final Font font;
        private final int width;
        private final HorizontalAlign alignment;
        private final Colorc baseColor;
        private final Colorc shadowColor;
        private final boolean underlined;

        TextCacheKey(String text, Font font, int maxWidth, HorizontalAlign alignment, Colorc baseColor, Colorc shadowColor, boolean underlined) {
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
     * A key that identifies an entry in the texture cache. It contains the elements that affect the generation of mesh
     * for texture rendering.
     */
    private static class TextureCacheKey {

        private Vector2i textureSize;
        private Vector2i areaSize;
        private Border border;
        private boolean tiled;

        TextureCacheKey(Vector2i textureSize, Vector2i areaSize) {
            this.textureSize = new Vector2i(textureSize);
            this.areaSize = new Vector2i(areaSize);
            this.border = Border.ZERO;
            this.tiled = true;
        }

        TextureCacheKey(Vector2i textureSize, Vector2i areaSize, Border border, boolean tiled) {
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
