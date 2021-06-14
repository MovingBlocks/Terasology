#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0


in vec4 color_gs;
in vec2 uv;

uniform bool use_texture = false;
uniform sampler2D texture_sampler;

layout(location = 0) out vec4 outColor;

void main() {
    if (use_texture) {
        outColor = texture(texture_sampler, uv);
    } else {
        outColor = color_gs;
    }

    if (outColor.a < 0.01) {
        discard;
    }
}
