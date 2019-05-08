#version 110
attribute vec3 coord2d;
varying vec3 texCoord;
void main(void)
{
texCoord = coord2d;


gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4(coord2d.xy,0.0,1.0);

}