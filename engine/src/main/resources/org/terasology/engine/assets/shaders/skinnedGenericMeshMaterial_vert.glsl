#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

layout (location = 0) in vec3 in_vert;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv0;

layout (location = 4) in int in_bone[4];
layout (location = 8) in vec4 in_weight;

uniform mat3 normalMatrix;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec3 v_normal;
out vec2 v_uv0;

uniform mat4 boneTransforms[254];

void main() {
    mat4 skinMat = boneTransforms[int(in_bone[0])] * in_weight.x;
         skinMat += boneTransforms[int(in_bone[1])] * in_weight.y;
         skinMat += boneTransforms[int(in_bone[2])] * in_weight.z;
         skinMat += boneTransforms[int(in_bone[3])] * in_weight.w;

    gl_Position = (projectionMatrix * modelViewMatrix) * (skinMat * vec4(in_vert, 1.0));
    v_normal = normalMatrix * in_normal * mat3(skinMat);
    v_uv0 = in_uv0;
    v_color0 = in_color0;

}
