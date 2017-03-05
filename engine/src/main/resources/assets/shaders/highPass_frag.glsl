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

uniform sampler2D tex;
//uniform sampler2D texDepth;

uniform float highPassThreshold;

void main() {
    vec4 color = texture2D(tex, gl_TexCoord[0].xy);
    //float depth = texture2D(texDepth, gl_TexCoord[0].xy).x * 2.0 - 1.0;

    // Don't bloom the sky
    //if (epsilonEqualsOne(depth)) {
    //    discard;
    //}

   // vec3 brightColor = max(color.rgb - vec3(highPassThreshold), vec3(0.0));
    float relativeLuminance = dot(vec3(0.2126, 0.7152, 0.0722),color.rgb - vec3(highPassThreshold));
    //bright = smoothstep(0.0, 0.5, bright);

    if(relativeLuminance * highPassThreshold > 1.0)
    {
        gl_FragData[0].rgba = vec4( color.rgb,1);
    }
    else
    {
        gl_FragData[0].rgba = vec4(0);
    }
}
