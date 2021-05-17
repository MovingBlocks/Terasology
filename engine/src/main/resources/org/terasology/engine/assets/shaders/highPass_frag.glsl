#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform sampler2D tex;
uniform float highPassThreshold;

void main() {
    vec4 color = texture(tex, v_uv0.xy);

    // vec3 brightColor = max(color.rgb - vec3(highPassThreshold), vec3(0.0));
    float relativeLuminance = dot(vec3(0.2126, 0.7152, 0.0722),color.rgb - vec3(highPassThreshold));
    // bright = smoothstep(0.0, 0.5, bright);

    if(relativeLuminance * highPassThreshold > 1.0) {
        gl_FragData[0].rgba = vec4(color.rgb, 1);
    } else {
        gl_FragData[0].rgba = vec4(0);
    }
}
