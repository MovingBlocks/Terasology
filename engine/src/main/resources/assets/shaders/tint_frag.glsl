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

uniform vec3 inLiquidTint;
uniform sampler2D texScene;
uniform vec3 tint = vec3(1.0,1.0,1.0);

void main() {
    if (!swimming) {
        gl_FragData[0].rgba = texture2D(texScene, gl_TexCoord[0].xy).rgba * vec4(tint, 1); 
    } else {
        gl_FragData[0].rgba = gl_FragData[0].rgba; 
    }    
}
