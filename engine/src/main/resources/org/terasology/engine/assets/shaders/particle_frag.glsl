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

in vec4 color_gs;
in vec2 uv;

out vec4 out_color;

uniform bool use_texture = false;
uniform sampler2D texture_sampler;

void main() {
    if (use_texture) {
        out_color = texture(texture_sampler, uv);
    } else {
        out_color = color_gs;
    }

    if (out_color.a < 0.01) {
        discard;
    }
}
