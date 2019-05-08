#version 110
attribute vec4 coord4d;
uniform vec3 pos;
uniform vec3 rot;
uniform vec3 secTrans;

varying vec4 texCoord;
varying vec3 Normal;
 void main(void) { 
 	texCoord = coord4d;
 	
 	//POSITION AND ROTATION CALCULATED BASED ON MATRIX MULTIPLICATION
 	mat4 position = mat4(1.0);
 	position[3].x = pos.x;
 	position[3].y = pos.y;
 	position[3].z = pos.z;
 	
 	mat4 heading = mat4(1.0);
 	heading[0][0] = cos(rot.x);
 	heading[0][2] = -(sin(rot.x));
 	heading[2][0] = sin(rot.x);
 	heading[2][2] = cos(rot.x);
 	
 	mat4 pitch = mat4(1.0);
 	pitch[1][1] = cos(-rot.y);
 	pitch[1][2] = sin(-rot.y);
 	pitch[2][1] = -(sin(-rot.y));
 	pitch[2][2] = cos(-rot.y);
 	
 	//NOT NEEDED BUT I'LL INCLUDE FOR FUTURE USE MAYBE
 	mat4 roll = mat4(1.0); 	
 	roll[0][0] = cos(rot.z);
 	roll[0][1] = sin(rot.z);
 	roll[1][0] = -sin(rot.z);
 	roll[1][1] = cos(rot.z);
 	
 	mat4 sectorTrans = mat4(1.0);
 	sectorTrans[3].x = secTrans.x;
 	sectorTrans[3].y = secTrans.y;
 	sectorTrans[3].z = secTrans.z;
 	
 	//Convert vertex points to camera coordinate system (by multiplying by modelview matrix)
 	//Then Apply position matrix (to center rotation on camera)
 	//Then apply the rotations
 	//Then multiply by the projection matrix for screen coordinate system
   gl_Position =  gl_ProjectionMatrix * heading * pitch * roll * position * gl_ModelViewMatrix * sectorTrans * vec4(coord4d.xyz,1);
   Normal = normalize(gl_NormalMatrix * gl_Normal);
    }