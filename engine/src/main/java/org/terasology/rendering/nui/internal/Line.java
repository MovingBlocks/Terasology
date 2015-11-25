/*
 * Copyright 2013 MovingBlocks
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.terasology.rendering.nui.Color;

import java.nio.FloatBuffer;

/**
 * see http://artgrammer.blogspot.de/2011/05/drawing-nearly-perfect-2d-line-segments.html
 */
public class Line {
    public void draw(float x1, float y1, float x2, float y2, float width, Color color, Color background, float alpha) {
        GL20.glUseProgram(0);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

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
                cRed += 0.88 * (1 - f);
                cGreen += 0.88 * (1 - f);
                cBlue += 0.88 * (1 - f);
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
                float len = (float) Math.sqrt(dx * dx + dy * dy);
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

        //draw the line by triangle strip
        float[] lineVertex =
                {
                        x1 - tx - rx, y1 - ty - ry,    //fading edge1
                        x2 - tx - rx, y2 - ty - ry,
                        x1 - tx, y1 - ty,        //core
                        x2 - tx, y2 - ty,
                        x1 + tx, y1 + ty,
                        x2 + tx, y2 + ty,
                        x1 + tx + rx, y1 + ty + ry,    //fading edge2
                        x2 + tx + rx, y2 + ty + ry
                };
        GL11.glVertexPointer(2, 0, wrap(lineVertex));

        if (!alphaBlend) {
            float[] lineColor =
                    {
                            bRed, bGreen, bBlue,
                            bRed, bGreen, bBlue,
                            cRed, cGreen, cBlue,
                            cRed, cGreen, cBlue,
                            cRed, cGreen, cBlue,
                            cRed, cGreen, cBlue,
                            bRed, bGreen, bBlue,
                            bRed, bGreen, bBlue
                    };
            GL11.glColorPointer(3, 0, wrap(lineColor));
        } else {
            float[] lineColor =
                    {
                            cRed, cGreen, cBlue, 0,
                            cRed, cGreen, cBlue, 0,
                            cRed, cGreen, cBlue, a,
                            cRed, cGreen, cBlue, a,
                            cRed, cGreen, cBlue, a,
                            cRed, cGreen, cBlue, a,
                            cRed, cGreen, cBlue, 0,
                            cRed, cGreen, cBlue, 0
                    };
            GL11.glColorPointer(4, 0, wrap(lineColor));
        }

        if ((Math.abs(dx) < epsilon || Math.abs(dy) < epsilon) && width <= 1.0) {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 6);
        } else {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 8);
        }

        //cap (do not draw if too thin)
        if (width >= 3) {
            //draw cap
            lineVertex = new float[]
                    {
                            x1 - rx + cx, y1 - ry + cy,    //cap1
                            x1 + rx + cx, y1 + ry + cy,
                            x1 - tx - rx, y1 - ty - ry,
                            x1 + tx + rx, y1 + ty + ry,
                            x2 - rx - cx, y2 - ry - cy,    //cap2
                            x2 + rx - cx, y2 + ry - cy,
                            x2 - tx - rx, y2 - ty - ry,
                            x2 + tx + rx, y2 + ty + ry
                    };
            GL11.glVertexPointer(2, 0, wrap(lineVertex));

            if (!alphaBlend) {
                float[] lineColor =
                        {
                                bRed, bGreen, bBlue,    //cap1
                                bRed, bGreen, bBlue,
                                cRed, cGreen, cBlue,
                                cRed, cGreen, cBlue,
                                bRed, bGreen, bBlue,    //cap2
                                bRed, bGreen, bBlue,
                                cRed, cGreen, cBlue,
                                cRed, cGreen, cBlue
                        };
                GL11.glColorPointer(3, 0, wrap(lineColor));
            } else {
                float[] lineColor =
                        {
                                cRed, cGreen, cBlue, 0,    //cap1
                                cRed, cGreen, cBlue, 0,
                                cRed, cGreen, cBlue, a,
                                cRed, cGreen, cBlue, a,
                                cRed, cGreen, cBlue, 0,    //cap2
                                cRed, cGreen, cBlue, 0,
                                cRed, cGreen, cBlue, a,
                                cRed, cGreen, cBlue, a
                        };
                GL11.glColorPointer(4, 0, wrap(lineColor));
            }

            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 4, 4);
        }

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glEnable(GL11.GL_CULL_FACE);

    }

    private FloatBuffer wrap(float[] data) {
        FloatBuffer buf = BufferUtils.createFloatBuffer(data.length);
        buf.put(data);
        buf.rewind();
        return buf;
    }
}
