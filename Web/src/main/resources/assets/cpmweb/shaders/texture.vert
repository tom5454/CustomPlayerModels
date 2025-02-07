#version 300 es

in vec3 position;
in vec4 color;
in highp vec2 texture;
in vec3 normal;

out vec4 thecolor;
out highp vec2 texuv;
out vec3 normOut;

uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * vec4(position, 1.0);
    thecolor = color;
    texuv = texture;
    normOut = normal;
}