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

#version 440 core

in vec4 in_Position;
in vec2 in_TextureCoord;
in float in_Number;

out vec2 pass_TextureCoord;
out float pass_Number;


void main(void) {
	gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
	pass_TextureCoord = in_TextureCoord;
	pass_Number = in_Number;
}