attribute vec3 position;
uniform vec2 surfaceSize;
varying vec2 surfacePosition;

void main() {
    surfacePosition = position.xy * surfaceSize * 0.5;
    gl_Position = vec4( position, 1.0 );
}