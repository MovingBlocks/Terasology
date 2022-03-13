#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

void main()
{
	gl_Position = ftransform();
	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}
