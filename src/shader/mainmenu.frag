#version 110
varying vec2 texCoord;
uniform sampler2D picture;
void main(void)
{
	gl_FragColor = texture2D(picture,vec2(texCoord.x / 2.0 - 0.5,texCoord.y / -2.0 - 0.5));
}