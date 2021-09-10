#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projMatrix;
uniform mat4 viewProjMatrix;

layout (location = 0) in vec3 in_vert;

void main()
{
	gl_Position = (viewProjMatrix * modelMatrix) * vec4(in_vert, 1.0);
}
