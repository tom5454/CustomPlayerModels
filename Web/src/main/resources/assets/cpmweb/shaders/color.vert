#version 300 es

in vec3 position;
in vec4 color;

out vec4 thecolor;

uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * vec4(position, 1.0);
    thecolor = color;
}