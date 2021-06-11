#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

layout (location = 0) in vec3 in_vert;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

//out vec4 v_pos;

void main() {
	gl_Position = projectionMatrix * modelViewMatrix *  vec4(in_vert, 1.0);

}
