#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

layout (location = 0) in vec3 in_vert;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv0;
layout (location = 4) in vec4 in_color0;

uniform vec2 offset;
uniform vec4 color;
uniform vec2 scale;
uniform vec2 texOffset;
uniform vec2 texSize;
uniform vec4 croppingBoundaries;

uniform mat4 modelView;
uniform mat4 proj;

out vec2 v_relPos;
out vec2 v_uv0;
out vec4 v_color0;

void main()
{
    vec3 pos = in_vert;
    pos.xy *= scale;
    pos.xy += offset;
    v_relPos = pos.xy;
	gl_Position = (proj * modelView) * vec4(pos,1.0);
    v_uv0 = texOffset + in_uv0 * (texSize);
    v_color0 = color;
}
