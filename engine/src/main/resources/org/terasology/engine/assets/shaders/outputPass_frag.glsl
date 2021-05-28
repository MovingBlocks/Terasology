#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform sampler2D target;

void main() {
    vec4 diffColor = texture(target, v_uv0.xy);

    #if defined (FEATURE_ALPHA_REJECT)
    if (diffColor.a < 0.1) {
        discard;
    }
    #endif

    gl_FragData[0].rgba = diffColor;
}
