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
#ifdef BLOOM
uniform sampler2D texBloom;
#endif
#if !defined (NO_BLUR)
uniform sampler2D texBlur;
uniform float blurFocusDistance;
uniform float blurStart;
uniform float blurLength;
#endif
#ifdef VIGNETTE
uniform sampler2D texVignette;
#endif
#ifdef FILM_GRAIN
uniform sampler2D texNoise;
uniform vec2 noiseSize = vec2(64.0, 64.0);
uniform vec2 renderTargetSize = vec2(1280.0, 720.0);
#endif

#ifdef FILM_GRAIN
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

    float currentDepth = texture2D(texDepth, gl_TexCoord[0].xy).x;

#ifndef NO_BLUR
    float depthLin = linDepthVDist(currentDepth);
    float blur = 0.0;

    float finalBlurStart = blurFocusDistance / viewingDistance + blurStart;
    if (depthLin > finalBlurStart && !swimming) {
       blur = clamp((depthLin - finalBlurStart) / blurLength, 0.0, 1.0);
    } else if (swimming) {
       blur = 1.0;
    }
#endif

    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);

#if defined (MOTION_BLUR)
    vec4 screenSpaceNorm = vec4(gl_TexCoord[0].x, gl_TexCoord[0].y, currentDepth, 1.0);
    vec4 screenSpacePos = screenSpaceNorm * vec4(2.0, 2.0, 1.0, 1.0) - vec4(1.0, 1.0, 0.0, 0.0);
#endif

#ifdef MOTION_BLUR
    vec4 worldSpacePos = invViewProjMatrix * screenSpacePos;
    vec4 normWorldSpacePos = worldSpacePos / worldSpacePos.w;
    vec4 prevScreenSpacePos = prevViewProjMatrix * normWorldSpacePos;
    prevScreenSpacePos /= prevScreenSpacePos.w;

    vec2 velocity = (screenSpacePos.xy - prevScreenSpacePos.xy) / 16.0;
    velocity = clamp(velocity, vec2(-0.025), vec2(0.025));

    vec2 blurTexCoord = gl_TexCoord[0].xy;
    blurTexCoord += velocity;
    for(int i = 1; i < MOTION_BLUR_SAMPLES; ++i, blurTexCoord += velocity)
    {
      vec4 currentColor = texture2D(texScene, blurTexCoord);
# ifndef NO_BLUR
      vec4 currentColorBlur = texture2D(texBlur, blurTexCoord);
# endif

      color += currentColor;
# ifndef NO_BLUR
      colorBlur += currentColorBlur;
# endif
    }

    color /= MOTION_BLUR_SAMPLES;
# ifndef NO_BLUR
    colorBlur /= MOTION_BLUR_SAMPLES;
# endif
#endif

#ifndef NO_BLUR
    vec4 finalColor = mix(color, colorBlur, blur);
#else
    vec4 finalColor = color;
#endif

#ifdef BLOOM
    vec4 colorBloom = texture2D(texBloom, gl_TexCoord[0].xy);
    finalColor += colorBloom;
#endif

#ifdef FILM_GRAIN
    vec3 noise = texture2D(texNoise, renderTargetSize * (gl_TexCoord[0].xy + noiseOffset) / noiseSize).xyz * 2.0 - 1.0;
    finalColor.rgb += vec3(noise) * grainIntensity;
#endif

#ifdef VIGNETTE
    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;

    if (!swimming) {
        finalColor.rgb *= vig;
    } else {
        finalColor.rgb *= vig * vig * vig;
        finalColor.rgb *= vec3(0.1, 0.2, 0.2);
    }
#endif

    gl_FragData[0].rgba = finalColor;
}
