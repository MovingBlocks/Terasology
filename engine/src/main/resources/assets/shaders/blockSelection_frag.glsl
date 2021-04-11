#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform sampler2D texture;

in vec2 v_uv0;
in vec4 v_color0;

void main(){
    vec4 diffColor = texture2D(texture, v_uv0);

    #if defined (FEATURE_ALPHA_REJECT)
    if (diffColor.a < 0.1) {
        discard;
    }
        #endif

    gl_FragData[0].rgba = diffColor * v_color0;
    gl_FragData[1].rgba = vec4(0.5, 1.0, 0.5, 1.0);
    gl_FragData[2].rgba = vec4(0.0, 0.0, 0.0, 0.0);
}
