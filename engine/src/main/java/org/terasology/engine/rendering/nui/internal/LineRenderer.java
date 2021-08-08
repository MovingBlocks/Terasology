// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.internal;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;


public final class LineRenderer {
    private static StandardMeshData lineMeshData = new StandardMeshData(DrawingMode.TRIANGLE_STRIP, AllocationType.STREAM);
    private static Mesh lineMesh = null;

    private LineRenderer() {

    }

    /**
     * Draws a 2D line segment in OpenGL.
     *
     * @param x1         The X-coordinate of the segment's start point.
     * @param y1         The Y-coordinate of the segment's start point.
     * @param x2         The X-coordinate of the segment's end point.
     * @param y2         The Y-coordinate of the segment's end point.
     * @param width      Thickness of the line in pixels.
     * @param color      The line color.
     * @param background The background color. Ignored if alpha blending is used.
     * @param alpha      The alpha channel. If set to 0, alpha blending is not used.
     * @see <a href="http://artgrammer.blogspot.de/2011/05/drawing-nearly-perfect-2d-line-segments.html">
     * Drawing nearly perfect 2D line segments in OpenGL
     * </a>
     */
    public static void draw(float x1, float y1, float x2, float y2, float width, Colorc color, Colorc background, float alpha) {
        if(lineMesh == null) {
            lineMesh = Assets.generateAsset(lineMeshData, Mesh.class);
        }
        GL11.glDisable(GL11.GL_CULL_FACE);

        float t = 0;
        float r = 0;
        float f = width - (int) width;
        float a;
        boolean alphaBlend = alpha > 0;
        float cRed = color.rf();
        float cGreen = color.gf();
        float cBlue = color.bf();
        float bRed = background.rf();
        float bGreen = background.gf();
        float bBlue = background.bf();

        if (alphaBlend) {
            a = alpha;
        } else {
            a = 1.f;
        }

        if (width >= 0.0 && width < 1.0) {
            t = 0.05f;
            r = 0.48f + 0.32f * f;
            if (!alphaBlend) {
                cRed += 0.88f * (1 - f);
                cGreen += 0.88f * (1 - f);
                cBlue += 0.88f * (1 - f);
                if (cRed > 1.0f) {
                    cRed = 1.0f;
                }
                if (cGreen > 1.0f) {
                    cGreen = 1.0f;
                }
                if (cBlue > 1.0f) {
                    cBlue = 1.0f;
                }
            } else {
                a *= f;
            }
        } else if (width >= 1.0 && width < 2.0) {
            t = 0.05f + f * 0.33f;
            r = 0.768f + 0.312f * f;
        } else if (width >= 2.0 && width < 3.0) {
            t = 0.38f + f * 0.58f;
            r = 1.08f;
        } else if (width >= 3.0 && width < 4.0) {
            t = 0.96f + f * 0.48f;
            r = 1.08f;
        } else if (width >= 4.0 && width < 5.0) {
            t = 1.44f + f * 0.46f;
            r = 1.08f;
        } else if (width >= 5.0 && width < 6.0) {
            t = 1.9f + f * 0.6f;
            r = 1.08f;
        } else if (width >= 6.0) {
            float ff = width - 6.0f;
            t = 2.5f + ff * 0.50f;
            r = 1.08f;
        }

        //determine angle of the line to horizontal
        float tx = 0; //core thinkness of a line
        float ty = 0;
        float rx = 0; //fading edge of a line
        float ry = 0;
        float cx = 0; //cap of a line
        float cy = 0;
        float epsilon = 0.01f;
        float dx = x2 - x1;
        float dy = y2 - y1;
        if (Math.abs(dx) < epsilon) {
            //vertical
            tx = t;
            ty = 0;
            rx = r;
            ry = 0;
            if (width > 0.0 && width < 1.0) {
                tx *= 8;
            } else if (width == 1.0) {
                tx *= 10;
            }
        } else if (Math.abs(dy) < epsilon) {
            //horizontal
            tx = 0;
            ty = t;
            rx = 0;
            ry = r;
            if (width > 0.0 && width < 1.0) {
                ty *= 8;
            } else if (width == 1.0) {
                ty *= 10;
            }
        } else {
            if (width < 3) { //approximate to make things even faster
                float m = dy / dx;
                //and calculate tx,ty,rx,ry
                if (m > -0.4142 && m <= 0.4142) {
                    // -22.5< angle <= 22.5, approximate to 0 (degree)
                    tx = t * 0.1f;
                    ty = t;
                    rx = r * 0.6f;
                    ry = r;
                } else if (m > 0.4142 && m <= 2.4142) {
                    // 22.5< angle <= 67.5, approximate to 45 (degree)
                    tx = t * -0.7071f;
                    ty = t * 0.7071f;
                    rx = r * -0.7071f;
                    ry = r * 0.7071f;
                } else if (m > 2.4142 || m <= -2.4142) {
                    // 67.5 < angle <=112.5, approximate to 90 (degree)
                    tx = t;
                    ty = t * 0.1f;
                    rx = r;
                    ry = r * 0.6f;
                } else if (m > -2.4142 && m < -0.4142) {
                    // 112.5 < angle < 157.5, approximate to 135 (degree)
                    tx = t * 0.7071f;
                    ty = t * 0.7071f;
                    rx = r * 0.7071f;
                    ry = r * 0.7071f;
                }
            } else { //calculate to exact
                dx = y1 - y2;
                dy = x2 - x1;
                float len = (float) Math.sqrt((double) dx * dx + (double) dy * dy);
                dx /= len;
                dy /= len;
                cx = -0.6f * dy;
                cy = 0.6f * dx;
                tx = t * dx;
                ty = t * dy;
                rx = r * dx;
                ry = r * dy;
            }
        }

        lineMeshData.reallocate(0, 0);
        lineMeshData.indices.rewind();
        lineMeshData.position.rewind();
        lineMeshData.color0.rewind();


        Vector3f v1 = new Vector3f();
        Vector4f v2 = new Vector4f();

        lineMeshData.position.put(v1.set(x1 - tx - rx, y1 - ty - ry, 0.0f));
        lineMeshData.position.put(v1.set(x2 - tx - rx, y2 - ty - ry, 0.0f));
        lineMeshData.position.put(v1.set(x1 - tx, y1 - ty,   0.0f));
        lineMeshData.position.put(v1.set(x2 - tx, y2 - ty, 0.0f));
        lineMeshData.position.put(v1.set(x1 + tx, y1 + ty, 0.0f));
        lineMeshData.position.put(v1.set(x2 + tx, y2 + ty, 0.0f));

        if (!((Math.abs(dx) < epsilon || Math.abs(dy) < epsilon) && width <= 1.0)) {
            lineMeshData.position.put(v1.set(x1 + tx + rx, y1 + ty + ry,  0.0f));
            lineMeshData.position.put(v1.set(x2 + tx + rx, y2 + ty + ry,  0.0f));
        }

        Color c = new Color();
        if (!alphaBlend) {
            lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
            lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
            lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
            lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
            lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
            lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
            lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
        } else {
            lineMeshData.color0.put(c.set(v2.set(bRed, bGreen, bBlue, 0.0f)));
            lineMeshData.color0.put(c.set(v2.set(bRed, bGreen, bBlue, 0.0f)));
            lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, a)));
            lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, a)));
            lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, a)));
            lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, 0.0f)));
            lineMeshData.color0.put(c.set(v2.set(bRed, bGreen, bBlue, 0.0f)));

        }
        lineMesh.reload(lineMeshData);
        lineMesh.render();

        //cap (do not draw if too thin)
        if (width >= 3) {
            lineMeshData.reallocate(0, 0);
            lineMeshData.indices.rewind();
            lineMeshData.position.rewind();
            lineMeshData.color0.rewind();

            lineMeshData.position.put(v1.set( x1 - rx + cx, y1 - ry + cy, 0.0f));
            lineMeshData.position.put(v1.set( x1 + rx + cx, y1 + ry + cy, 0.0f));
            lineMeshData.position.put(v1.set( x1 - tx - rx, y1 - ty - ry, 0.0f));
            lineMeshData.position.put(v1.set( x1 + tx + rx, y1 + ty + ry, 0.0f));
            lineMeshData.position.put(v1.set( x2 - rx - cx, y2 - ry - cy, 0.0f));
            lineMeshData.position.put(v1.set( x2 + rx - cx, y2 + ry - cy, 0.0f));
            lineMeshData.position.put(v1.set( x2 - tx - rx, y2 - ty - ry, 0.0f));
            lineMeshData.position.put(v1.set( x2 + tx + rx, y2 + ty + ry, 0.0f));

            if (!alphaBlend) {
                lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
                lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
                lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
                lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
                lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
                lineMeshData.color0.put(c.set(v1.set(bRed, bGreen, bBlue)));
                lineMeshData.color0.put(c.set(v1.set(cRed, cGreen, cBlue)));
            } else {

                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, 0)));
                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, 0)));
                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, a)));
                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, a)));
                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, 0)));
                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, 0)));
                lineMeshData.color0.put(c.set(v2.set(cRed, cGreen, cBlue, a)));
            }
            lineMesh.reload(lineMeshData);
            lineMesh.render();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);

    }

}
