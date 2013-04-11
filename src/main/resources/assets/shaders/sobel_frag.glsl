/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

uniform sampler2D texDepth;

uniform float texelWidth;
uniform float texelHeight;

uniform float pixelOffsetX;
uniform float pixelOffsetY;

float fetchDepth(float x, float y) {
    return linDepth(texture2D(texDepth, gl_TexCoord[0].xy + vec2(x*texelWidth*pixelOffsetX, y*texelHeight*pixelOffsetY)).x);
}

void main() {
    mat3 depthMatrix;
    depthMatrix[0][0] = fetchDepth(-1.0, -1.0);
    depthMatrix[1][0] = fetchDepth(0.0, -1.0);
    depthMatrix[2][0] = fetchDepth(1.0, -1.0);
    depthMatrix[0][1] = fetchDepth(-1.0, 0.0);
    depthMatrix[2][1] = fetchDepth(1.0, 0.0);
    depthMatrix[0][2] = fetchDepth(-1.0, 1.0);
    depthMatrix[1][2] = fetchDepth(0.0, 1.0);
    depthMatrix[2][2] = fetchDepth(1.0, 1.0);

    float gx, gy;
    gx = -1.0*depthMatrix[0][0]-2.0*depthMatrix[1][0]-1.0*depthMatrix[2][0]+1.0*depthMatrix[0][2]+2.0*depthMatrix[1][2]+1.0*depthMatrix[2][2];
    gy = -1.0*depthMatrix[0][0]-2.0*depthMatrix[0][1]-1.0*depthMatrix[0][2]+1.0*depthMatrix[2][0]+2.0*depthMatrix[2][1]+1.0*depthMatrix[2][2];

    float result = sqrt(gx*gx + gy*gy);
    gl_FragData[0].rgba = vec4(result, result, result, 1.0);
}
