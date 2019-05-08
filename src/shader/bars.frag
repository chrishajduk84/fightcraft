#version 110
varying vec3 texCoord;
uniform sampler2D texture;
uniform sampler2D block;
void main(void)
{
//Draw the screen template texture
	if (texCoord.z == 0.0)
		gl_FragColor =  texture2D(texture,vec2(texCoord.x / 2.0 - 0.5,texCoord.y / -2.0 - 0.5));
	else if(texCoord.z != 0.0) 
	{
		///Maps the block texture to the lower right corner
		float xt = texCoord.x / 0.3 - 2.2;
		float yt = texCoord.y / 0.27 - 0.85;
		gl_FragColor = texture2D (block, vec2((xt + texCoord.z - 1.0) / 16.0, -yt));
	}


}