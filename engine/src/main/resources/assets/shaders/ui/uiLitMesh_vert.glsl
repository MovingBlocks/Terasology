#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

layout (location = 0) in vec3 in_vert;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv0;
layout (location = 4) in vec4 in_color0;

out vec3 v_normal;
out vec2 v_relPos;
out vec2 v_uv0;
out vec4 v_color0;

uniform mat4 modelView;
uniform mat4 proj;

uniform float alpha;

void main()
{
    mat4 normalMatrix = transpose(inverse(modelView));

    gl_Position = (proj * modelView) * vec4(in_vert, 1.0);
    v_relPos = gl_Position.xy;

    v_normal = (normalMatrix * vec4(in_normal,1.0)).xyz;
    v_uv0 = in_uv0;
    v_color0 = vec4(in_color0.rgb, in_color0.a * alpha);
}
