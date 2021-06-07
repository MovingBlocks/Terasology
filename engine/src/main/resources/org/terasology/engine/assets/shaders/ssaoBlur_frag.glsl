#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform sampler2D tex;
uniform vec2 texelSize;

in vec2 v_uv0;

void main() {
    float result = 0.0;
    for (int i=-2; i<2; ++i) {
        for (int j=-2; j<2; ++j) {
            vec2 offset = vec2(texelSize.x * float(j), texelSize.y * float(i));
            result += texture(tex, v_uv0.xy + offset).r;
        }
    }

    gl_FragData[0].rgba = vec4(result / 16.0);
}
