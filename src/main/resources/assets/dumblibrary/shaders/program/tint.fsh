#version 120

uniform sampler2D sampler;
uniform vec4 colour;
varying vec2 texCoord;

void main() {
    gl_FragColor = texture2D(sampler, texCoord) * colour;
}
