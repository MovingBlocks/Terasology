#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

#define UNCHARTED_2_TONEMAP
// #define REINHARD_TONEMAP
// #define BURGESS_TONEMAP


in vec2 v_uv0;

uniform sampler2D texScene;
uniform float exposure = 1;
uniform float whitePoint = W;

void main(){
    vec4 color = srgbToLinear(texture(texScene, v_uv0.xy));

#ifdef REINHARD_TONEMAP
    float t = tonemapReinhard(2.5, exposure);
    color *= t;
#endif

#ifdef UNCHARTED_2_TONEMAP
    //HDR tone mapping using Uncharted 2 method
    // http://frictionalgames.blogspot.com/2012/09/tech-feature-hdr-lightning.html
    color.rgb = uncharted2Tonemap(color.rgb * exposure) / uncharted2Tonemap(vec3(whitePoint));
#endif

#ifdef BURGESS_TONEMAP
    color.rgb *= exposure;
    vec3 x = max(vec3(0.0),color.rgb-vec3(0.004));
    vec3 finalColor = (x*(6.2*x+.5))/(x*(6.2*x+1.7)+0.06);
    color.rgb = finalColor;
#endif

    gl_FragData[0].rgba = linearToSrgb(color);
}
