#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform float weight;
uniform float decay;
uniform float exposure;
uniform float density;

uniform sampler2D texScene;

uniform float lightDirDotViewDir;
uniform vec2 lightScreenPos;

void main() {
    gl_FragData[0].rgba = vec4(0.0, 0.0, 0.0, 1.0);

    if (lightDirDotViewDir > 0.0) {
        vec2 uv0 = v_uv0;
        vec2 deltaTexCoord = (1.0 / float(LIGHT_SHAFT_SAMPLES)) * density * vec2(uv0.xy - lightScreenPos.xy);

        float dist = length(deltaTexCoord.xy);

        // TODO: This shouldn't be hardcoded
        float threshold = 0.01;
        if (dist > threshold) {
            deltaTexCoord.xy /= (dist / threshold);
        }

        float illuminationDecay = 1.0;
        for(int i=0; i < LIGHT_SHAFT_SAMPLES; i++) {
            uv0 -= deltaTexCoord;
            vec3 sampler = texture(texScene, uv0).rgb;

            sampler *= illuminationDecay * weight;
            gl_FragData[0].rgb += sampler;
            illuminationDecay *= decay;
        }

        gl_FragData[0].rgb *= exposure * lightDirDotViewDir;
    }
}
