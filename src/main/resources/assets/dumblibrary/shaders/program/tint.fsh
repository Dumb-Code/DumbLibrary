#version 120

uniform sampler2D sampler;
uniform vec4 colour;
varying vec2 texCoord;

void main() {
    gl_FragColor = vec4(texture2D(sampler, texCoord).rgb * colour, colour.a);
}
