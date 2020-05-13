#version 330 core

/*
 * Copyright 2020 MovingBlocks
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

layout (points) in;
layout (triangle_strip) out;
layout (max_vertices = 4) out;

in vec3 scale_vs;

out vec2 uv;

uniform mat4 view_projection;
uniform vec3 camera_position;

void main() {
    vec3 position = gl_in[0].gl_Position.xyz;

    vec3 to_camera = normalize(camera_position - position);
    vec3 right = cross(to_camera, vec3(0.0, 1.0, 0.0));

    position += right * 0.5 * scale_vs.x;
    position.y -= 0.5 * scale_vs.y;
    gl_Position = view_projection * vec4(position, 1);
    uv = vec2(0, 0);
    EmitVertex();

    position.y += scale_vs.y;
    gl_Position = view_projection * vec4(position, 1);
    uv = vec2(0, 1);
    EmitVertex();

    position -= right * scale_vs.x;
    position.y -= scale_vs.y;
    gl_Position = view_projection * vec4(position, 1);
    uv = vec2(1, 0);
    EmitVertex();

    position.y += scale_vs.y;
    gl_Position = view_projection * vec4(position, 1);
    uv = vec2(1, 1);
    EmitVertex();

    EndPrimitive();
}
