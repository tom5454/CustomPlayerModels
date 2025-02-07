#version 300 es
precision mediump float;

in vec4 thecolor;

out vec4 color;

void main() {
    color = thecolor;
}