#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0


layout (location = 0) in vec3 position_ws;
layout (location = 1) in vec3 scale;
layout (location = 2) in vec4 color;
layout (location = 3) in vec2 texture_offset;

out vec3 scale_vs;
out vec4 color_vs;
out vec2 texture_offset_vs;

void main() {
    gl_Position = vec4(position_ws, 1);
    scale_vs = scale;
    color_vs = color;
    texture_offset_vs = texture_offset;
}
