/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
uniform sampler2D texBloom;
uniform sampler2D texBlur;
uniform sampler2D texVignette;
uniform sampler2D texDepth;

uniform bool swimming;

uniform float viewingDistance;

#define Z_NEAR 0.1
#define BLUR_START 0.25
#define BLUR_LENGTH 0.25

float linDepth() {
    float z = texture2D(texDepth, gl_TexCoord[0].xy).x;
    return (2.0 * Z_NEAR) / (viewingDistance + Z_NEAR - z * (viewingDistance - Z_NEAR));
}

void main() {
    /* BLUR */
    vec4 colorBlur = texture2D(texBlur, gl_TexCoord[0].xy);

    float depth = linDepth();
    float blur = 0.0;

    if (depth > 0.1 && !swimming)
       blur = clamp((depth - BLUR_START) / BLUR_LENGTH, 0.0, 1.0);
    else if (swimming)
       blur = 1.0;

    /* COLOR AND BLOOM */
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
    vec4 colorBloom = texture2D(texBloom, gl_TexCoord[0].xy);

    color = clamp(color + colorBloom, 0.0, 1.0);
    colorBlur = clamp(colorBlur + colorBloom, 0.0, 1.0);

    /* FINAL MIX */
    vec4 finalColor = mix(color, colorBlur, blur);

    /* VIGNETTE */
    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;

    if (!swimming)
        finalColor.rgb *= vig;
    else
        finalColor.rgb *= vig / 2.0;

    gl_FragColor = finalColor;
}
