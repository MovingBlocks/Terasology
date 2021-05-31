#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
//varying vec4 positionProj;

in vec4 v_pos;

void main() {
    gl_FragDepth = v_pos.z / v_pos.w;
}
