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

uniform sampler2D texture;

uniform float light = 1.0;

void main(){
    vec4 color = texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));

    color.a = color.r;

    // Fade the clouds at the horizon
    color.a *= clamp(1.0 - (length(gl_TexCoord[0].xy - 0.5) / 0.4), 0.0, 1.0);

    // Fade out the clouds at night
    if (light < 0.25)
        color.a *= 1.0 - clamp(abs(0.25 - expLightValue(light)) / 0.25, 0.0, 1.0);

    gl_FragData[0].rgba = color;
}
