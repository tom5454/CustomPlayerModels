#version 300 es
precision mediump float;

in vec4 thecolor;
in highp vec2 texuv;

out vec4 color;

uniform sampler2D uSampler;

void main() {
    color = thecolor * texture(uSampler, texuv);
    if(color.a < 0.1)discard;
}