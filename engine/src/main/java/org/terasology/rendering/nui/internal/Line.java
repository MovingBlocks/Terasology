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
    public void draw( float x1, float y1, float x2, float y2, float width, Color color, Color background, float alpha) {
        GL20.glUseProgram(0);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        float t = 0;
        float R = 0;
        float f = width - (int)width;
        float A;
        boolean alphaBlend = alpha>0;
        float Cr = color.rf(), Cg = color.gf(), Cb = color.bf();
        float Br = background.rf(), Bg = background.gf(), Bb = background.bf();
        if( alphaBlend ) {
            A = alpha;
        } else {
            A = 1.f;
        }

        if ( width>=0.0 && width<1.0) {
            t=0.05f;
            R=0.48f+0.32f*f;
            if ( !alphaBlend) {
                Cr+=0.88*(1-f);
                Cg+=0.88*(1-f);
                Cb+=0.88*(1-f);
                if ( Cr>1.0f) {
                    Cr=1.0f;
                }
                if ( Cg>1.0f){
                    Cg=1.0f;
                }
                if ( Cb>1.0f){
                    Cb=1.0f;
                }
            } else {
                A*=f;
            }
        } else if ( width>=1.0 && width<2.0) {
            t=0.05f+f*0.33f;
            R=0.768f+0.312f*f;
        } else if ( width>=2.0 && width<3.0){
            t=0.38f+f*0.58f;
            R=1.08f;
        } else if ( width>=3.0 && width<4.0){
            t=0.96f+f*0.48f;
            R=1.08f;
        } else if ( width>=4.0 && width<5.0){
            t=1.44f+f*0.46f;
            R=1.08f;
        } else if ( width>=5.0 && width<6.0){
            t=1.9f+f*0.6f;
            R=1.08f;
        } else if ( width>=6.0){
            float ff=width-6.0f;
            t=2.5f+ff*0.50f;
            R=1.08f;
        }

        //determine angle of the line to horizontal
        float tx=0,ty=0; //core thinkness of a line
        float  Rx=0,Ry=0; //fading edge of a line
        float  cx=0,cy=0; //cap of a line
        float  ALW=0.01f;
        float  dx=x2-x1;
        float  dy=y2-y1;
        if ( Math.abs(dx) < ALW) {
            //vertical
            tx=t; ty=0;
            Rx=R; Ry=0;
            if ( width>0.0 && width<1.0)
                tx*=8;
            else if ( width==1.0)
                tx*=10;
        } else if ( Math.abs(dy) < ALW) {
            //horizontal
            tx=0; ty=t;
            Rx=0; Ry=R;
            if ( width>0.0 && width<1.0)
                ty*=8;
            else if ( width==1.0)
                ty*=10;
        } else {
            if ( width < 3) { //approximate to make things even faster
                float m=dy/dx;
                //and calculate tx,ty,Rx,Ry
                if ( m>-0.4142 && m<=0.4142) {
                    // -22.5< angle <= 22.5, approximate to 0 (degree)
                    tx=t*0.1f;
                    ty=t;
                    Rx=R*0.6f;
                    Ry=R;
                } else if ( m>0.4142 && m<=2.4142) {
                    // 22.5< angle <= 67.5, approximate to 45 (degree)
                    tx=t*-0.7071f;
                    ty=t*0.7071f;
                    Rx=R*-0.7071f;
                    Ry=R*0.7071f;
                } else if ( m>2.4142 || m<=-2.4142) {
                    // 67.5 < angle <=112.5, approximate to 90 (degree)
                    tx=t;
                    ty=t*0.1f;
                    Rx=R;
                    Ry=R*0.6f;
                } else if ( m>-2.4142 && m<-0.4142) {
                    // 112.5 < angle < 157.5, approximate to 135 (degree)
                    tx=t*0.7071f;
                    ty=t*0.7071f;
                    Rx=R*0.7071f;
                    Ry=R*0.7071f;
                }
            } else { //calculate to exact
                dx=y1-y2;
                dy=x2-x1;
                float L=(float) Math.sqrt(dx*dx+dy*dy);
                dx/=L;
                dy/=L;
                cx=-0.6f*dy;
                cy=0.6f*dx;
                tx=t*dx;
                ty=t*dy;
                Rx=R*dx;
                Ry=R*dy;
            }
        }

        //draw the line by triangle strip
        float line_vertex[]=
                {
                        x1-tx-Rx, y1-ty-Ry,	//fading edge1
                        x2-tx-Rx, y2-ty-Ry,
                        x1-tx,y1-ty,		//core
                        x2-tx,y2-ty,
                        x1+tx,y1+ty,
                        x2+tx,y2+ty,
                        x1+tx+Rx, y1+ty+Ry,	//fading edge2
                        x2+tx+Rx, y2+ty+Ry
                };
        GL11.glVertexPointer(2, 0, wrap(line_vertex));

        if ( !alphaBlend) {
            float line_color[]=
                    {
                            Br,Bg,Bb,
                            Br,Bg,Bb,
                            Cr,Cg,Cb,
                            Cr,Cg,Cb,
                            Cr,Cg,Cb,
                            Cr,Cg,Cb,
                            Br,Bg,Bb,
                            Br,Bg,Bb
                    };
            GL11.glColorPointer(3, 0, wrap(line_color));
        } else {
            float line_color[]=
                    {
                            Cr,Cg,Cb,0,
                            Cr,Cg,Cb,0,
                            Cr,Cg,Cb,A,
                            Cr,Cg,Cb,A,
                            Cr,Cg,Cb,A,
                            Cr,Cg,Cb,A,
                            Cr,Cg,Cb,0,
                            Cr,Cg,Cb,0
                    };
            GL11.glColorPointer(4, 0, wrap(line_color));
        }

        if ( (Math.abs(dx) < ALW || Math.abs(dy) < ALW) && width<=1.0) {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 6);
        } else {
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 8);
        }

        //cap
        if ( width < 3) {
            //do not draw cap
        } else {
            //draw cap
            line_vertex=new float[]
                    {
                            x1-Rx+cx, y1-Ry+cy,	//cap1
                            x1+Rx+cx, y1+Ry+cy,
                            x1-tx-Rx, y1-ty-Ry,
                            x1+tx+Rx, y1+ty+Ry,
                            x2-Rx-cx, y2-Ry-cy,	//cap2
                            x2+Rx-cx, y2+Ry-cy,
                            x2-tx-Rx, y2-ty-Ry,
                            x2+tx+Rx, y2+ty+Ry
                    };
            GL11.glVertexPointer(2, 0, wrap(line_vertex));

            if ( !alphaBlend) {
                float line_color[]=
                        {
                                Br,Bg,Bb,	//cap1
                                Br,Bg,Bb,
                                Cr,Cg,Cb,
                                Cr,Cg,Cb,
                                Br,Bg,Bb,	//cap2
                                Br,Bg,Bb,
                                Cr,Cg,Cb,
                                Cr,Cg,Cb
                        };
                GL11.glColorPointer(3, 0, wrap(line_color));
            } else {
                float line_color[]=
                        {
                                Cr,Cg,Cb,0,	//cap1
                                Cr,Cg,Cb,0,
                                Cr,Cg,Cb,A,
                                Cr,Cg,Cb,A,
                                Cr,Cg,Cb,0,	//cap2
                                Cr,Cg,Cb,0,
                                Cr,Cg,Cb,A,
                                Cr,Cg,Cb,A
                        };
                GL11.glColorPointer(4, 0, wrap(line_color));
            }

            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
            GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 4, 4);
        }

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glEnable(GL11.GL_CULL_FACE);

    }

    private FloatBuffer wrap( float[] data ) {
        FloatBuffer buf =  BufferUtils.createFloatBuffer(data.length);
        buf.put(data);
        buf.rewind();
        return buf;
    }
}
