#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform vec4 croppingBoundaries;
uniform sampler2D texture;

varying vec2 relPos;

in vec2 v_relPos;
in vec2 v_uv0;
in vec4 v_color0;

void main(){
    if (v_relPos.x < croppingBoundaries.x || v_relPos.x > croppingBoundaries.y || v_relPos.y < croppingBoundaries.z || v_relPos.y > croppingBoundaries.w) {
        discard;
    }
    vec4 diffColor = texture2D(texture, v_uv0);
    gl_FragData[0].rgba = diffColor * v_color0;
}
