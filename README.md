CRTerm
======

CRTerm is a [Java](https://en.wikipedia.org/wiki/Java_(programming_language))/[OpenGL](https://en.wikipedia.org/wiki/OpenGL) [library](https://en.wikipedia.org/wiki/Library_(computing)) aimed at helping the development of [terminal emulators](https://en.wikipedia.org/wiki/Terminal_emulator). This library doesn't tackle all functionality related to terminal emulation, but only the area of visual representation. 

Demo on Youtube:
[![Demo on Youtube](/doc/img/cterm_youtube.png)]
(https://www.youtube.com/watch?v=SO9MrM9zTuk)

## Dependencies

- The only dependency is the [LWJGL library](https://www.lwjgl.org/). These files need to be linked with the project:
    - **Windows:** lwjgl.jar, lwjgl-natives-windows.jar, lwjgl-glfw.jar, lwjgl-glfw-natives-windows.jar, lwjgl-opengl.jar, lwjgl-opengl-natives-windows.jar
    - **Linux:** lwjgl.jar, lwjgl-natives-linux.jar, lwjgl-glfw.jar, lwjgl-glfw-natives-linux.jar, lwjgl-opengl.jar, lwjgl-opengl-natives-linux.jar
    - **MacOS:** lwjgl.jar, lwjgl-natives-macos.jar, lwjgl-glfw.jar, lwjgl-glfw-natives-macos.jar, lwjgl-opengl.jar, lwjgl-opengl-natives-macos.jar

## Features

- Currently uses the original character set of the Kaypro II luggable computers.
- The texture atlas of the font is generated according to input parameters. Both the distance between the scanlines and the width of the dots can be changed.
- The characters have a modest shining appearance, applied through a bloom post-processing effect.
- A scanline passes through the screen in fixed intervals, simulating a common problem of old CRT displays.
- All characters can be fully changed for each frame render. Provides direct access to the internal character array for efficient manipulation.
- Simulates the gradual fade-out of the characters.

The font is generated on the fly according to the input parameters. This is an example render, based on the Kaypro II character set:

![Character set - texture atlas](/doc/img/character_set.png)

## Usage

- Instantiate the CRTerm.Terminal to display the main window. See the doc folder about the parameters.
- Use the "setCharacters" or directly manipulate the character array accessed through "getCharacterArray". After the modifications upload the characters to the GPU, using "uploadCharacterArray".
- See an example application at: [/src/TestApp/Main.java](https://github.com/bolner/CRTerm/blob/master/src/TestApp/Main.java) 
- Handle the keyboard inputs using the LWJGL functions. (Using the Window ID)
