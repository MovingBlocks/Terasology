#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
in vec2 v_uv0;

uniform sampler2D texDepth;

uniform float texelWidth;
uniform float texelHeight;

uniform float pixelOffsetX;
uniform float pixelOffsetY;

float fetchDepth(float x, float y) {
    return linDepth(texture2D(texDepth, v_uv0.xy + vec2(x*texelWidth*pixelOffsetX, y*texelHeight*pixelOffsetY)).x);
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
