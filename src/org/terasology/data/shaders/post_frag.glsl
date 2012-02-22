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
uniform sampler2D texDepth;
uniform sampler2D texBlur;

uniform bool swimming;
uniform float exposure = 1.0;

float linDepth() {
    const float cNear = 0.1;
    const float cZFar = 512.0;
    float z = texture2D(texDepth, gl_TexCoord[0].xy).x;

    return (2.0 * cNear) / (cZFar + cNear - z * (cZFar - cNear));
}

void main(){
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
    vec4 colorBlur = texture2D(texBlur, gl_TexCoord[0].xy);

    float t = tonemapReinhard(color, 1.0, exposure);

    color *= t;
    colorBlur *= t;

    // Apply bloom
    color += texture2D(texBloom, gl_TexCoord[0].xy);
    colorBlur += texture2D(texBloom, gl_TexCoord[0].xy);

    float depth = linDepth();
    float blur = 0.0;

    if (depth > 0.1 && !swimming)
       blur = clamp((depth - 0.1) / 0.1, 0.0, 1.0);
    else if (swimming)
       blur = 1.0;

    vec4 finalColor = mix(color, colorBlur, blur);

    // Vignette
    float d = distance(gl_TexCoord[0].xy, vec2(0.5,0.5));

    if (swimming)
        finalColor.rgb *= (1.0 - d) / 4.0;
    else
        finalColor.rgb *= (1.0 - d);

    gl_FragColor = finalColor;
}
