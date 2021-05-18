#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform sampler2D tex;

uniform float radius = 16.0;
uniform vec2 texelSize = vec2(1.0/1024.0, 1.0/1024.0);

const vec2 taps[12] = vec2[12](
    vec2(-0.326212,-0.40581), vec2(-0.840144,-0.07358),
    vec2(-0.695914,0.457137), vec2(-0.203345,0.620716),
    vec2(0.96234,-0.194983), vec2(0.473434,-0.480026),
    vec2(0.519456,0.767022), vec2(0.185461,-0.893124),
    vec2(0.507431,0.064425), vec2(0.89642,0.412458),
    vec2(-0.32194,-0.932615), vec2(-0.791559,-0.59771)
);

void main() {
    vec4 sampleAccum = vec4(0.0, 0.0, 0.0, 0.0);

    for (int nTapIndex = 0; nTapIndex < 12; nTapIndex++) {
        vec2 tapcoord = v_uv0.xy + texelSize * taps[nTapIndex] * radius;
        sampleAccum += texture2D(tex, tapcoord);
    }

    gl_FragData[0].rgba = sampleAccum / 12.0;
}
