/*
Copyright 2017 Tamas Bolner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

#version 150 core

uniform sampler2D texture_diffuse;
uniform int characters[4096];

in vec2 pass_TextureCoord;
in float pass_Number;

out vec4 out_Color;


void main(void) {
    int char = characters[int(pass_Number)];

    float x = float(char % 16) / 16 + 0.015625;
    float y = float(char / 16) / 8;

	out_Color = texture(texture_diffuse, vec2(
        x + pass_TextureCoord.x * 0.046875,
        y + pass_TextureCoord.y / 8
	));
}
