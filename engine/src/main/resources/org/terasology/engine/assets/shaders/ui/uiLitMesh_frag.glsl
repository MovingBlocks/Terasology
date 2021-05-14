#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform vec4 croppingBoundaries;
uniform sampler2D texture;

in vec3 v_normal;
in vec2 v_relPos;
in vec2 v_uv0;
in vec4 v_color0;

void main(){
    if (v_relPos.x < croppingBoundaries.x || v_relPos.x > croppingBoundaries.y || v_relPos.y < croppingBoundaries.z || v_relPos.y > croppingBoundaries.w) {
        discard;
    }
    vec4 color = texture2D(texture, v_uv0);
    float light = min(1.0, 0.3 * max(0.0, dot(v_normal, vec3(0, -1, 0))) + 1.0 * max(0.0, dot(v_normal, vec3(0,0,1))));
    color.rgb = color.rgb * light;

    gl_FragData[0].rgba = color * v_color0;
}
