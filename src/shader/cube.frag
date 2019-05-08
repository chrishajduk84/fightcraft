#version 110
varying vec4 texCoord;
varying vec3 Normal;
uniform sampler2D texture;
void main(void)
{
 //Takes the X Coordinates from the block, makes it equal between 0 and 1, and then adds the 
 //specified number of pixels from the block id, which is divided by 16 (width of texture atlas),
 //to get the individual pictures

/////////////////////
//Calculate Lighting
/////////////////////
vec3 vertex = texCoord.xyz;
//New Variable type
struct lightSource {
vec4 position;
vec4 diffuse;
//Distance Depreciation Values
float constantDepreciation, linearDepreciation, quadraticDepreciation ;
float spotCutoff, spotExponent;
vec3 spotDirection;
};

lightSource sun = lightSource (
	vec4(5.0,1.0,5.0,0.0),
	vec4(1.0,1.0,1.0,1.0),
	0.0,0.1,0.0,
	80.0,20.0,
	vec3(0.0,-10.0,0.0)
	);
	
	float depreciation;
	vec3 lightDirection;
//	if (sun.position.w == 0.0) //Diffuse Light
//	{
		depreciation = 1.0;
		lightDirection = normalize(vec3(sun.position.xyz));
//	}
//	else
//	{
//		vec3 lightToVertex = vec3(sun.position.xyz - vertex);
//		float distance = length(lightToVertex);
//		lightDirection = normalize(lightToVertex);
//		depreciation = 1.0/(sun.constantDepreciation + sun.linearDepreciation * distance + sun.quadraticDepreciation * distance * distance);
//	}
	
	
	vec3 diffuseReflection = depreciation * vec3(sun.diffuse) * max(0.0,dot(Normal,lightDirection));



///////////////////////////
//Calculate Texture Maps
///////////////////////////

vec4 textureColor;
 
 //FRACT - Calculates the fractional portion of the point ---ie.  x = 2.5  x = 2.5 - 2  x = 0.5

//Textures facing the x/z directions 
 if (texCoord.w > 0.0)
  textureColor = texture2D(texture, vec2((fract(-texCoord.x + -texCoord.z) + texCoord.w - 1.0) / 16.0, -texCoord.y));
//Textures facing y direction
 else
  textureColor = texture2D(texture, vec2((fract(-texCoord.x) + -texCoord.w - 1.0) / 16.0, -texCoord.z));
	
	
gl_FragColor = vec4(textureColor.rgb * diffuseReflection ,textureColor.a);
}