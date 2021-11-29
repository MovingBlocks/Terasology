#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform sampler2D diffuse;

uniform float blockLight = 1.0;
uniform float sunlight = 1.0;

uniform vec3 colorOffset;
uniform bool textured;

in vec3 v_normal;
in vec2 v_uv0;
in vec4 v_color0;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outNormal;
layout(location = 2) out vec4 outLight;

void main(){
    vec4 color;

    if (textured) {
        color = texture(diffuse, v_uv0);
        color.rgb *= colorOffset.rgb;
        outColor.rgba = color;
    } else {
        color = vec4(colorOffset.rgb, 1.0);
        outColor.rgba = color;
    }

    outNormal.rgba = vec4(v_normal.x / 2.0 + 0.5, v_normal.y / 2.0 + 0.5, v_normal.z / 2.0 + 0.5, 0.0);
    outLight.rgba = vec4(blockLight, sunlight, 0.0, 0.0);
}
