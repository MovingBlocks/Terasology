/*
 * Copyright 2015 Benjamin Glatzel <benjamin.glatzel@me.com>
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

//uniform int particleIndex;

/*
#define INDEX3_X ((particleIndex * 3) + 0)
#define INDEX3_Y ((particleIndex * 3) + 1)
#define INDEX3_Z ((particleIndex * 3) + 2)

#define INDEX4_X ((particleIndex * 4) + 0)
#define INDEX4_Y ((particleIndex * 4) + 1)
#define INDEX4_Z ((particleIndex * 4) + 2)
#define INDEX4_W ((particleIndex * 4) + 3)
*/

uniform vec3 scale;
uniform vec3 position;

uniform vec4 color;

mat4 undoRotation(in mat4 matrix) {
	mat4 noRotation = mat4(matrix);

	for(int row = 0; row < 3; row++) {
		for(int column = 0; column < 3; column++) {
			noRotation[row][column] = (row == column) ? 1 : 0;
		}
	}

	return noRotation;
}

vec4 applyScale(in vec4 vertex) {
    return vec4(
        vertex.x * scale.x,
        vertex.y * scale.y,
        vertex.z * scale.z,
        vertex.w
    );
}

void main()
{
    gl_Position = applyScale(gl_Vertex);
    gl_Position.xyz += position;
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Position;

    gl_FrontColor = color;
}
