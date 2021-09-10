#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec4 v_color0;

layout(location = 0) out vec4 outColor;
layout(location = 1) out vec4 outNormal;
layout(location = 2) out vec4 outLight;

void main() {
    outColor = v_color0;
    outNormal = vec4(0.5, 1.0, 0.5, 1.0);
    outLight = vec4(0.0, 0.0, 0.0, 0.0);
}
