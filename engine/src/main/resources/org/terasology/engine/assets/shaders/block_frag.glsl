#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform sampler2D textureAtlas;

uniform vec3 colorOffset = vec3(1.0, 1.0, 1.0);

uniform bool textured = false;

uniform float blockLight = 1.0;
uniform float sunlight = 1.0;

uniform float alpha = 1.0;

in vec3 v_normal;
in vec2 v_uv0;
in vec4 v_color0;

void main() {
    vec4 color;
    if (textured) {
        color = v_color0 * texture2D(textureAtlas, v_uv0);
    } else {
        color = v_color0;
    }
    color.a *= alpha;

    if (color.a < 0.1) {
        discard;
    }

    color.rgb *= colorOffset.rgb;

    gl_FragData[0].rgba = color;
    gl_FragData[1].rgba = vec4(v_normal.x / 2.0 + 0.5, v_normal.y / 2.0 + 0.5, v_normal.z / 2.0 + 0.5, 0.0);
    gl_FragData[2].rgba = vec4(blockLight, sunlight, 0.0, 0.0);
}
