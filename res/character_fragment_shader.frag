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

in vec2 pass_TextureCoord;
in float pass_Number;

uniform sampler2D image1;
uniform int characters[4096];
uniform vec2 dimensions;            // character grid width x height
uniform float scanlinePosition;

out vec4 out_Color;


void main(void) {
    /*
        Scanline
    */
    float line = float(int(dimensions.y) - int(pass_Number) / int(dimensions.x)) + pass_TextureCoord.y;
    float plus = 0.0;
    float lineWidth = dimensions.y / 6.0;

    if (scanlinePosition > dimensions.y - lineWidth) {
        if (line < scanlinePosition + lineWidth - dimensions.y) {
            plus = 0.04;
        }
    }

    if (line > scanlinePosition && line < scanlinePosition + lineWidth) {
        plus = 0.04;
    }

    /*
        Map texture of character
    */
    int char = characters[int(pass_Number)];

    float x = float(char % 16) / 16 + 0.015625;
    float y = float(char / 16) / 8;

	out_Color = texture(image1, vec2(
        x + pass_TextureCoord.x * 0.046875,
        y + pass_TextureCoord.y / 8
	)) + vec4(0, 1, 0, 1) * plus;
}
