#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

#ifdef BLOOM
uniform float bloomFactor;

uniform sampler2D texBloom;
#endif

uniform sampler2D texScene;

#ifdef LIGHT_SHAFTS
uniform sampler2D texLightShafts;
#endif

in vec2 v_uv0;

void main() {

    vec4 color = texture(texScene, v_uv0.xy);
#ifdef LIGHT_SHAFTS
    vec4 colorShafts = texture(texLightShafts, v_uv0.xy);
    color.rgb += colorShafts.rgb;
#endif

#ifdef BLOOM
    vec4 colorBloom = texture(texBloom, v_uv0.xy);
    color += colorBloom * bloomFactor;
#endif

    gl_FragData[0].rgba = color.rgba;
}
