#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform sampler2D texScene;

#ifdef VIGNETTE
uniform sampler2D texVignette;
uniform vec3 inLiquidTint;
uniform vec3 tint = vec3(1.0,1.0,1.0);
#endif

void main(){
    vec4 color = texture(texScene, v_uv0.xy);

    #ifdef VIGNETTE
        float vig = texture(texVignette, v_uv0.xy).x;
        if (!swimming) {
            color.rgb *= vec3(vig, vig, vig) + (1 - vig) * tint.rgb;
        } else {
            color.rgb *= vig * vig * vig;
            color.rgb *= inLiquidTint;
        }
    #endif
    gl_FragData[0].rgba = color.rgba;
}
