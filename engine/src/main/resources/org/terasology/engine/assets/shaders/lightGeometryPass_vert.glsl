#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

layout (location = 0) in vec3 in_vert;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv0;
layout (location = 4) in vec4 in_color0;

out vec2 v_uv0;
out vec4 vertexProjPos;

uniform mat4 modelMatrix;
uniform mat4 viewProjMatrix;

void main() {
#if defined (FEATURE_LIGHT_POINT)
    vertexProjPos = (viewProjMatrix * modelMatrix) * vec4(in_vert, 1.0);
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    vertexProjPos =  vec4(in_vert, 1.0);
#endif

	gl_Position = vertexProjPos;
    v_uv0 = in_uv0;
}
