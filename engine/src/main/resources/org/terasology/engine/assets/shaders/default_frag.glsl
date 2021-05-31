#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec4 v_color0;

void main() {
    gl_FragData[0].rgba = v_color0;
    gl_FragData[1].rgba = vec4(0.5, 1.0, 0.5, 1.0);
    gl_FragData[2].rgba = vec4(0.0, 0.0, 0.0, 0.0);
}
