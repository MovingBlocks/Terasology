#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0



layout (location = 0) in vec3 in_vert;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv0;
layout (location = 4) in vec4 in_color0;

uniform float alpha;
uniform vec2 offset;

out vec2 v_relPos;
out vec2 v_uv0;
out vec4 v_color0;

uniform mat4 modelView;
uniform mat4 proj;

void main() {
    vec4 pos = vec4(in_vert, 1.0);
    pos.xy += offset;
    v_relPos = pos.xy;
    v_uv0 = in_uv0;
    v_color0 = vec4(in_color0.rgb, in_color0.a * alpha);

    gl_Position = proj * modelView * pos;
}
