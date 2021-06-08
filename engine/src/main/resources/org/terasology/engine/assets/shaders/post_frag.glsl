#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform sampler2D texScene;
uniform sampler2D texDepth;

uniform sampler3D texColorGradingLut;

#if !defined (NO_BLUR)
uniform sampler2D texBlur;
uniform float focalDistance;//distance from the camera to object at the center of the screen
#endif

#ifdef FILM_GRAIN
uniform sampler2D texNoise;

uniform vec2 noiseSize;
uniform vec2 renderTargetSize;

uniform float noiseOffset;
uniform float grainIntensity;
#endif

#ifdef MOTION_BLUR
uniform mat4 invViewProjMatrix;
uniform mat4 prevViewProjMatrix;
#endif

void main() {
#if !defined (NO_BLUR)
    vec4 colorBlur = texture(texBlur, v_uv0.xy);
#endif

    float currentDepth = texture(texDepth, v_uv0.xy).x * 2.0 - 1.0;
//TODO: Separate the underwater shader effect from the depth of field effect - Amrit 'Who'
/**
 * Calculate blur for depth of field effect and underwater.
 */
#ifndef NO_BLUR
    //depthLin - distance of the fragment currently being processed from the camera as a fraction of the view distance.
    float depthLin = linDepthViewingDistance(currentDepth);
    float blur = 0.0;
    //nearBoundDOF - Distance from the camera to the beginning of the area where no blur will be applied as a fraction of the view distance
    float nearBoundDOF = clamp((focalDistance - 15),0.0,focalDistance)/viewingDistance;
    //farBoundDOF - Distance from the camera to the end of the area where no blur will be applied as a fraction of the view distance
    float farBoundDOF = clamp((focalDistance + 15),focalDistance,viewingDistance)/viewingDistance;
    //if the fragment is beyond the far boundary increase the blur proportional to the fragment distance from the boundary
    if (depthLin > farBoundDOF  && !swimming)
       blur = clamp(((depthLin - farBoundDOF)/farBoundDOF),0.0,1.0);
    //else if the fragment is closer than the near boundary increase the blur proportional to the fragment distance from the boundary
    else if (depthLin < nearBoundDOF  && !swimming)
        blur = (nearBoundDOF - depthLin)/nearBoundDOF;
    else if (swimming) {
       blur = 1.0;//apply full blur if underwater.
    }
#endif

    vec4 color = texture(texScene, v_uv0.xy);

#if defined (MOTION_BLUR)
    vec4 screenSpaceNorm = vec4(v_uv0.x, v_uv0.y, currentDepth, 1.0);
    vec4 screenSpacePos = screenSpaceNorm * vec4(2.0, 2.0, 1.0, 1.0) - vec4(1.0, 1.0, 0.0, 0.0);

    vec4 worldSpacePos = invViewProjMatrix * screenSpacePos;
    vec4 normWorldSpacePos = worldSpacePos / worldSpacePos.w;
    vec4 prevScreenSpacePos = prevViewProjMatrix * normWorldSpacePos;
    prevScreenSpacePos /= prevScreenSpacePos.w;

    vec2 velocity = (screenSpacePos.xy - prevScreenSpacePos.xy) / 128.0;
    velocity = clamp(velocity, vec2(-0.01), vec2(0.01));

    vec2 blurTexCoord = v_uv0.xy;
    blurTexCoord += velocity;
    for(int i = 1; i < MOTION_BLUR_SAMPLES; ++i, blurTexCoord += velocity)
    {
      vec4 currentColor = texture(texScene, blurTexCoord);
#ifndef NO_BLUR
      vec4 currentColorBlur = texture(texBlur, blurTexCoord);
#endif

      color += currentColor;
#ifndef NO_BLUR
      colorBlur += currentColorBlur;
#endif
    }

    color /= MOTION_BLUR_SAMPLES;
#ifndef NO_BLUR
    colorBlur /= MOTION_BLUR_SAMPLES;
#endif
#endif

#ifndef NO_BLUR
    vec4 finalColor = mix(color, colorBlur, blur);
#else
    vec4 finalColor = color;
#endif

#ifdef FILM_GRAIN
    vec3 noise = texture(texNoise, renderTargetSize * (v_uv0.xy + noiseOffset) / noiseSize).xyz * 2.0 - 1.0;
    finalColor.rgb += clamp(noise.xxx * grainIntensity, 0.0, 1.0);
#endif

    // In the case the color is > 1.0 or < 0.0 despite tonemapping
    finalColor.rgb = clamp(finalColor.rgb, 0.0, 1.0);
    vec3 lutScale = vec3(15.0 / 16.0);

    // Color grading
    vec3 lutOffset = vec3(1.0 / 32.0);
    finalColor.rgb = texture(texColorGradingLut, lutScale * finalColor.rgb + lutOffset).rgb;

    gl_FragData[0].rgba = finalColor;
}
