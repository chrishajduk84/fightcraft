#version 110
varying vec2 texCoord;
attribute vec2 coord2d;
void main(void)
{
	texCoord = coord2d;
	gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(coord2d,0.0,1.0);
}