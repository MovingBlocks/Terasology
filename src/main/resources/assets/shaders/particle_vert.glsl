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

varying vec4 vertexViewPos;
varying vec3 normal;

void main()
{
	vertexViewPos = gl_ModelViewMatrix * gl_Vertex;

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	normal = gl_NormalMatrix * vec3(0.0f, 1.0f, 0.0f);

    gl_TexCoord[0] = gl_MultiTexCoord0;
}
