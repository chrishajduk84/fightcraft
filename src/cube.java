/**
 * @author Chris Hajduk
 *
 * Started May 6, 2012
 */
import java.awt.Color;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL11;
public class cube {
	
	int vertexID,indexID;
	int[] attributeID;
	
	//Lists the vertexes from the cube
	float cubeVertex[] = {
		//Front Side	
		-1.0f,-1.0f,1.0f,
		1.0f,-1.0f,1.0f,
		1.0f,1.0f,1.0f,
		-1.0f,1.0f,1.0f,
		//Top
		-1.0f,1.0f,1.0f,
		1.0f,1.0f,1.0f,
		1.0f,1.0f,-1.0f,
		-1.0f,1.0f,-1.0f,
		//Back Side
		-1.0f,-1.0f,-1.0f,
		1.0f,-1.0f,-1.0f,
		1.0f,1.0f,-1.0f,
		-1.0f,1.0f,-1.0f,
		//Bottom
		-1.0f,-1.0f,1.0f,
		1.0f,-1.0f,1.0f,
		1.0f,-1.0f,-1.0f,
		-1.0f,-1.0f,-1.0f,
		//Left
		-1.0f,-1.0f,1.0f,
		-1.0f,1.0f,1.0f,
		-1.0f,1.0f,-1.0f,
		-1.0f,-1.0f,-1.0f,
		//Right
		1.0f,-1.0f,1.0f,
		1.0f,1.0f,1.0f,
		1.0f,1.0f,-1.0f,
		1.0f,-1.0f,-1.0f,
	};
	int cubeElements[] = {
		//Specifies the order of the vertexes (in triangles) in relation to the above coordinates
		//Front
		0,1,2,
		2,3,0,
		//Top
		4,5,6,
		6,7,4,
		//Back
		8,9,10,
		10,11,8,
		//Bottom
		12,13,14,
		14,15,12,
		//Left
		16,17,18,
		18,19,16,
		//Right
		20,21,22,
		22,23,20
	};
	
    int[] indices = {0,1,2};//indices go in here
    float[] vertices = {0.0f,  0.8f,         
    		-0.8f, -0.8f,          
    		0.8f, -0.8f 
    		};
	
	public void initElement()
	{
		//Creates a buffer object from the vertices
		FloatBuffer fbuf = buffer.makeFloatBuffer(vertices);
		IntBuffer ibuf = buffer.makeIntBuffer(indices);


		vertexID = buffer.getBuffer(GL15.GL_ARRAY_BUFFER,fbuf);
		indexID = buffer.getBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,ibuf);
		
		//Shader for each individual cube
		shaderProgram shader = new shaderProgram();
		shader.attachShader("cube");
		GL20.glUseProgram(shader.glslProg);
		//Assign variables
		attributeID = new int[1];
		attributeID[0] = shader.getAttribute("coord3d");
		
		

	}
	

	public void draw(float posX, float posY, float posZ, int blockid)
	{
		//Bind vertices to the graphics card
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexID);
		//Loads attribute pointer variable
		GL20.glVertexAttribPointer(
				attributeID[0], 	//The attribute
				2,          		//3 elements (X,Y,Z)
				GL11.GL_FLOAT,      //Data type
				false,				//0 - or don't modify/convert the values until being used
				0,					//Byte offset between values
				0					//Pointer to the first values (incase of a header)
				);
		GL20.glEnableVertexAttribArray(attributeID[0]);
		//Binds elements to the graphics card
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER,indexID);
		GL12.glDrawRangeElements(
				GL11.GL_TRIANGLE_STRIP, 		///Drawing Mode - Draws the cube in triangles (fastest way)
				0, 						///Starting index
				3,	///Ending index
				3,	///Number of elements in the array
				GL11.GL_UNSIGNED_INT,	///Value type in the array
				0);						///Pointer to the array
		//Disable attribute
				GL20.glDisableVertexAttribArray(attributeID[0]);
			     GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);      
			     GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0); 
				
//		GL11.glTranslatef(posX, posY, posZ);
//		
//		
//		//FRONT SIDE
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures.blocks[blockid][0].getTextureID());	//Select Texture -FRONT
//		GL11.glBegin(GL11.GL_QUADS);
//		//GL11.glColor3f(1.0f,0,0);
//		GL11.glTexCoord2f(0f, 1f);
//		GL11.glVertex3f( 0, 0, 0); 
//		GL11.glTexCoord2f(0f, 0f);
//		GL11.glVertex3f(0,100, 0);          
//		//GL11.glColor3f(1.0f,0,0);  
//		GL11.glTexCoord2f(1f, 0f);
//		GL11.glVertex3f(100,100, 0);   
//		//GL11.glColor3f(1.0f,0,0); 
//		GL11.glTexCoord2f(1f, 1f);
//		GL11.glVertex3f( 100,0, 0);   
//		GL11.glEnd();
//		
//		
//		//BOTTOM
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures.blocks[blockid][5].getTextureID());	//Select Texture -FRONT
//		GL11.glBegin(GL11.GL_QUADS);
//		//GL11.glColor3f(0,1.0f,0);   
//		GL11.glTexCoord2f(0f,1f);
//		GL11.glVertex3f(0, 0, 0);         
//		//GL11.glColor3f(0,1.0f,0); 
//		GL11.glTexCoord2f(0f,0f);
//		GL11.glVertex3f(0,0,100);          
//		//GL11.glColor3f(0,1.0f,0);  
//		GL11.glTexCoord2f(1f,0f);
//		GL11.glVertex3f(100,0,100);   
//		//GL11.glColor3f(0,1.0f,0);   
//		GL11.glTexCoord2f(1f,1f);
//		GL11.glVertex3f(100,0, 0);
//		GL11.glEnd();
//		
//		
//		//LEFT SIDE
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures.blocks[blockid][1].getTextureID());	//Select Texture -FRONT
//		GL11.glBegin(GL11.GL_QUADS);
//		//GL11.glColor3f(0,0,1.0f);    
//		GL11.glTexCoord2f(0f, 1f);
//		GL11.glVertex3f(0, 0, 0);         
//		//GL11.glColor3f(0,0,1.0f);
//		GL11.glTexCoord2f(0f, 0f);
//		GL11.glVertex3f(0,100,0);          
//		//GL11.glColor3f(0,0,1.0f);  
//		GL11.glTexCoord2f(1f, 0f);
//		GL11.glVertex3f(0,100,100);   
//		//GL11.glColor3f(0,0,1.0f);  
//		GL11.glTexCoord2f(1f, 1f);
//		GL11.glVertex3f(0,0,100); 
//		GL11.glEnd();
//		
//		
//		//RIGHT SIDE
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures.blocks[blockid][3].getTextureID());	//Select Texture -FRONT
//		GL11.glBegin(GL11.GL_QUADS);
//		//GL11.glColor3f(0.5f,0.5f,0); 
//		GL11.glTexCoord2f(0f,1f);
//		GL11.glVertex3f(100, 0, 0);  
//		//GL11.glColor3f(0.5f,0.5f,0);  
//		GL11.glTexCoord2f(0f,0f);
//		GL11.glVertex3f(100,100,0);          
//		//GL11.glColor3f(0.5f,0.5f,0); 
//		GL11.glTexCoord2f(1f,0f);
//		GL11.glVertex3f(100,100,100);   
//		//GL11.glColor3f(0.5f,0.5f,0);   
//		GL11.glTexCoord2f(1f,1f);
//		GL11.glVertex3f(100,0,100);  
//		GL11.glEnd();
//		
//		
//		//BACK SIDE
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures.blocks[blockid][2].getTextureID());	//Select Texture -FRONT
//		GL11.glBegin(GL11.GL_QUADS);
//		//GL11.glColor3f(0.0f,0.5f,0.5f);  
//		GL11.glTexCoord2f(0f,1f);
//		GL11.glVertex3f( 0, 0, 100);         
//		//GL11.glColor3f(0.0f,0.5f,0.5f);  
//		GL11.glTexCoord2f(0f,0f);
//		GL11.glVertex3f(0,100, 100);          
//		//GL11.glColor3f(0.0f,0.5f,0.5f); 
//		GL11.glTexCoord2f(1f,0f);
//		GL11.glVertex3f( 100,100, 100);   
//		//GL11.glColor3f(0.0f,0.5f,0.5f); 
//		GL11.glTexCoord2f(1f,1f);
//		GL11.glVertex3f( 100,0, 100); 
//		GL11.glEnd();
//		
//		
//		//TOP
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D,textures.blocks[blockid][4].getTextureID());	//Select Texture -FRONT
//		GL11.glBegin(GL11.GL_QUADS);
//		//GL11.glColor3f(0.5f,0.5f,0.5f);   
//		GL11.glTexCoord2f(0f,1f);
//		GL11.glVertex3f( 0, 100, 0);         
//		//GL11.glColor3f(0.5f,0.5f,0.5f);  
//		GL11.glTexCoord2f(0f,0f);
//		GL11.glVertex3f(0,100,100);          
//		//GL11.glColor3f(0.5f,0.5f,0.5f); 
//		GL11.glTexCoord2f(1f,0f);      
//		GL11.glVertex3f( 100,100, 100);   
//		//GL11.glColor3f(0.5f,0.5f,0.5f); 
//		GL11.glTexCoord2f(1f,1f);
//		GL11.glVertex3f( 100,100, 0);  
//		GL11.glEnd();
//		
//		
//		GL11.glTranslatef(-posX, -posY, -posZ);
	}
	
}
