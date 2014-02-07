/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.asset.Assets;
import org.terasology.math.AABB;
import org.terasology.math.MatrixUtils;
import org.terasology.math.Quat4fUtil;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontMeshBuilder;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.VerticalAlign;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
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
 * @author Immortius
 */
public class LwjglCanvasRenderer implements CanvasRenderer {

    private static final String CROPPING_BOUNDARIES_PARAM = "croppingBoundaries";
    private Matrix4f modelView;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private Mesh billboard = Assets.getMesh("engine:UIBillboard");
    private Line line = new Line();

    private Material textureMat = Assets.getMaterial("engine:UITexture");

    // Text mesh caching
    private Map<TextCacheKey, Map<Material, Mesh>> cachedText = Maps.newLinkedHashMap();
    private Set<TextCacheKey> usedText = Sets.newHashSet();

    private Rect2i requestedCropRegion;
    private Rect2i currentTextureCropRegion;

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
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    @Override
    public void drawMesh(Mesh mesh, Material material, Rect2i drawRegion, Rect2i cropRegion, Quat4f rotation, Vector3f offset, float scale, float alpha) {
        AABB meshAABB = mesh.getAABB();
        Vector3f meshExtents = meshAABB.getExtents();
        float fitScale = 0.35f * Math.min(drawRegion.width(), drawRegion.height()) / Math.max(meshExtents.x, Math.max(meshExtents.y, meshExtents.z));
        Vector3f centerOffset = meshAABB.getCenter();
        centerOffset.scale(-1.0f);

        Matrix4f centerTransform = new Matrix4f(Quat4fUtil.IDENTITY, centerOffset, 1.0f);
        Matrix4f userTransform = new Matrix4f(rotation, offset, -fitScale * scale);
        Matrix4f translateTransform = new Matrix4f(Quat4fUtil.IDENTITY,
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

    public void drawTexture(TextureRegion texture, Color color, ScaleMode mode, Rect2i absoluteRegion,
                            float ux, float uy, float uw, float uh, float alpha) {


        if (!currentTextureCropRegion.equals(requestedCropRegion)
                && !(currentTextureCropRegion.encompasses(absoluteRegion) && requestedCropRegion.encompasses(absoluteRegion))) {
            textureMat.setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX(), requestedCropRegion.maxX() + 1,
                    requestedCropRegion.minY(), requestedCropRegion.maxY() + 1);
            currentTextureCropRegion = requestedCropRegion;
        }

        Vector2f scale = mode.scaleForRegion(absoluteRegion, texture.getWidth(), texture.getHeight());
        Rect2f textureArea = texture.getRegion();
        if (mode == ScaleMode.SCALE_FILL) {
            textureMat.setFloat2("offset", absoluteRegion.minX(), absoluteRegion.minY());
            textureMat.setFloat2("scale", absoluteRegion.width(), absoluteRegion.height());

            float texBorderX = (scale.x - absoluteRegion.width()) / scale.x * uw;
            float texBorderY = (scale.y - absoluteRegion.height()) / scale.y * uh;

            textureMat.setFloat2("texOffset", textureArea.minX() + (ux + 0.5f * texBorderX) * textureArea.width()
                    , textureArea.minY() + (uy + 0.5f * texBorderY) * textureArea.height());
            textureMat.setFloat2("texSize", (uw - texBorderX) * textureArea.width(), (uh - texBorderY) * textureArea.height());
        } else {
            textureMat.setFloat2("scale", scale);
            textureMat.setFloat2("offset",
                    absoluteRegion.minX() + 0.5f * (absoluteRegion.width() - scale.x),
                    absoluteRegion.minY() + 0.5f * (absoluteRegion.height() - scale.y));

            textureMat.setFloat2("texOffset", textureArea.minX() + ux * textureArea.width(), textureArea.minY() + uy * textureArea.height());
            textureMat.setFloat2("texSize", uw * textureArea.width(), uh * textureArea.height());
        }
        textureMat.setTexture("texture", texture.getTexture());
        textureMat.setFloat4("color", color.rf(), color.gf(), color.bf(), color.af() * alpha);
        textureMat.bindTextures();
        billboard.render();
    }

    public void drawText(String text, Font font, HorizontalAlign hAlign, VerticalAlign vAlign, Rect2i absoluteRegion,
                         Color color, Color shadowColor, float alpha) {
        TextCacheKey key = new TextCacheKey(text, font, absoluteRegion.width(), hAlign, color, shadowColor);
        usedText.add(key);
        Map<Material, Mesh> fontMesh = cachedText.get(key);
        List<String> lines = TextLineBuilder.getLines(font, text, absoluteRegion.width());
        if (fontMesh == null) {
            FontMeshBuilder meshBuilder = new FontMeshBuilder(font);
            fontMesh = meshBuilder.createTextMesh(lines, absoluteRegion.width(), hAlign, color, shadowColor);
            cachedText.put(key, fontMesh);
        }

        Vector2i offset = new Vector2i(absoluteRegion.minX(), absoluteRegion.minY());
        offset.y += vAlign.getOffset(lines.size() * font.getLineHeight(), absoluteRegion.height());

        for (Map.Entry<Material, Mesh> entry : fontMesh.entrySet()) {
            entry.getKey().bindTextures();
            entry.getKey().setFloat4(CROPPING_BOUNDARIES_PARAM, requestedCropRegion.minX(), requestedCropRegion.maxX() + 1,
                    requestedCropRegion.minY(), requestedCropRegion.maxY() + 1);
            entry.getKey().setFloat2("offset", offset.x, offset.y);
            entry.getKey().setFloat("alpha", alpha);
            entry.getValue().render();
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
        private Color baseColor;
        private Color shadowColor;

        public TextCacheKey(String text, Font font, int maxWidth, HorizontalAlign alignment, Color baseColor, Color shadowColor) {
            this.text = text;
            this.font = font;
            this.width = maxWidth;
            this.alignment = alignment;
            this.baseColor = baseColor;
            this.shadowColor = shadowColor;
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
                        && Objects.equals(baseColor, other.baseColor) && Objects.equals(shadowColor, other.shadowColor);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, font, width, alignment, baseColor, shadowColor);
        }
    }
}
