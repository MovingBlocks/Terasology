/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    vec4 colorBlur = texture2D(texBlur, gl_TexCoord[0].xy);
#endif

    float currentDepth = texture2D(texDepth, gl_TexCoord[0].xy).x * 2.0 - 1.0;
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

    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);

#if defined (MOTION_BLUR)
    vec4 screenSpaceNorm = vec4(gl_TexCoord[0].x, gl_TexCoord[0].y, currentDepth, 1.0);
    vec4 screenSpacePos = screenSpaceNorm * vec4(2.0, 2.0, 1.0, 1.0) - vec4(1.0, 1.0, 0.0, 0.0);

    vec4 worldSpacePos = invViewProjMatrix * screenSpacePos;
    vec4 normWorldSpacePos = worldSpacePos / worldSpacePos.w;
    vec4 prevScreenSpacePos = prevViewProjMatrix * normWorldSpacePos;
    prevScreenSpacePos /= prevScreenSpacePos.w;

    vec2 velocity = (screenSpacePos.xy - prevScreenSpacePos.xy) / 128.0;
    velocity = clamp(velocity, vec2(-0.01), vec2(0.01));

    vec2 blurTexCoord = gl_TexCoord[0].xy;
    blurTexCoord += velocity;
    for(int i = 1; i < MOTION_BLUR_SAMPLES; ++i, blurTexCoord += velocity)
    {
      vec4 currentColor = texture2D(texScene, blurTexCoord);
#ifndef NO_BLUR
      vec4 currentColorBlur = texture2D(texBlur, blurTexCoord);
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
    vec3 noise = texture2D(texNoise, renderTargetSize * (gl_TexCoord[0].xy + noiseOffset) / noiseSize).xyz * 2.0 - 1.0;
    finalColor.rgb += clamp(noise.xxx * grainIntensity, 0.0, 1.0);
#endif

    // In the case the color is > 1.0 or < 0.0 despite tonemapping
    finalColor.rgb = clamp(finalColor.rgb, 0.0, 1.0);
    vec3 lutScale = vec3(15.0 / 16.0);

    // Color grading
    vec3 lutOffset = vec3(1.0 / 32.0);
    finalColor.rgb = texture3D(texColorGradingLut, lutScale * finalColor.rgb + lutOffset).rgb;

    gl_FragData[0].rgba = finalColor;
}
