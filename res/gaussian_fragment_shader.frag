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

uniform vec2 in_direction;  // direction and size of a unit step (this decides between vertical and horizontal)
uniform sampler2D image;
uniform float gauss[16] = float[] (0.0957645635, 0.093045881, 0.0853442192, 0.0738985137, 0.0604062584, 0.0466136173, 0.0339569218, 0.0233522354, 0.0151604902, 0.0092914323, 0.0053757211, 0.002936131, 0.0015139055, 0.0007368969, 0.0003386097, 0.0001468847);

out vec4 out_Color;


void main()
{
    vec2 unit_offset = in_direction / textureSize(image, 0);
    vec3 result = texture(image, pass_TextureCoord).rgb * gauss[0];
    vec2 offset;

    for(int i = 1; i < 16; i++)
    {
        offset = unit_offset * float(i);

        result += (texture(image, pass_TextureCoord + offset).rgb + texture(image, pass_TextureCoord - offset).rgb) * gauss[i];
    }

    out_Color = vec4(result, 1.0);
}
