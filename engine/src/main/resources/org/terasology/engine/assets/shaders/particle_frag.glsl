#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0


in vec4 color_gs;
in vec2 uv;

out vec4 out_color;

uniform bool use_texture = false;
uniform sampler2D texture_sampler;

void main() {
    if (use_texture) {
        out_color = texture(texture_sampler, uv);
    } else {
        out_color = color_gs;
    }

    if (out_color.a < 0.01) {
        discard;
    }
}
