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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.math.AABB;
import org.terasology.math.MatrixUtils;
import org.terasology.math.Quat4fUtil;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.nui.Color;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import java.nio.FloatBuffer;

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
import static org.lwjgl.opengl.Util.checkGLError;

/**
 * @author Immortius
 */
public class LwjglCanvasRenderer implements CanvasRenderer {

    private Matrix4f modelView;
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private Mesh billboard = Assets.getMesh("engine:UIBillboard");
    private Line line = new Line();

    @Override
    public void preRender() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        checkGLError();
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
    }

    @Override
    public void postRender() {
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        checkGLError();
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

}
