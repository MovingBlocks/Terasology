#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec3 uv0;
layout (location = 4) in vec3 color0;

varying vec3 normal;

uniform mat3 normalMatrix;
uniform mat4 worldViewMatrix;
uniform mat4 projectionMatrix;

void main() {
    normal = normalMatrix * normal;
	gl_Position = (projectionMatrix * worldViewMatrix) * vec4(position, 1.0);

    gl_TexCoord[0] = uv0;
    gl_FrontColor = color0;
}

