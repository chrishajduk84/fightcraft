/**
 * @author Chris Hajduk
 *
 * Started May 15th, 2012
 */

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
public class shaderProgram {
	
	//Shaders are essential mini sub programs used and compiled by the GPU for special graphics effects
	//2 types of very important shaders are:
	//Vertex Shaders - Build matrices in 3D/2D space
	//Fragment Shaders - Apply textures and colors to the matrices
	
	//Together these two programs make a GLSL program (defined by glslProg)

	int fragProgram, vertProgram, glslProg;
	String fileProg;
	
	public void loadShader(String filename, int type)
	{
		String code = "";
		//Loads appropriate shaders from a file in the shaders folder
		int id = GL20.glCreateShader(type);
		filename = "shader/" + filename;
		if (type == GL20.GL_FRAGMENT_SHADER)
			filename += ".frag";
		else if (type == GL20.GL_VERTEX_SHADER)
			filename += ".vert";
		//Read the openGL shader file
		try
		{
		code = readShader(filename);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		//Compile the program
		GL20.glShaderSource(id, code);
		GL20.glCompileShader(id);
		
		//////////////////////////
		// MR. McAvoy. 
		// This is the code below that I forgot to add. (The if statements)
		// My Shader code would still compile without errors, but I wouldn't be able to attach to that instance and manipulate it.
		// Thank You for reminding me to double check, even the must simplest (and fundamental) pieces of the code.
		///////////////////////////
		
		
		if (type == GL20.GL_FRAGMENT_SHADER)
		{
			fragProgram = id;
		}			
		else if (type == GL20.GL_VERTEX_SHADER)
		{
			vertProgram = id;
		}
		printLog(id);
		
	}
	public void attachShader(String filename)
	{
		loadShader(filename,GL20.GL_FRAGMENT_SHADER);
		loadShader(filename,GL20.GL_VERTEX_SHADER);
		
		//Make a GLSL program
		glslProg = GL20.glCreateProgram();
		//Link the shaders to the program
			GL20.glAttachShader(glslProg,fragProgram);
			GL20.glAttachShader(glslProg,vertProgram);
			GL20.glLinkProgram(glslProg);
			GL20.glValidateProgram(glslProg);
			fileProg = filename;
	}
	public String readShader(String filename) throws IOException
	{
		String code = "", line;
		//Reads each line of the code until a null line is reached
		BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename)));
		//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		while ((line = br.readLine()) != null)
		{
			code += line + "\n";
		}
		return code;
	}
	public int getAttribute(String name)
	{
		//Gets the attribute from the GLSL shader code
		return GL20.glGetAttribLocation(glslProg, name);
	}
	
	public int getUniform(String name)
	{
		//Gets the uniform variable from the GLSL Code
		return GL20.glGetUniformLocation(glslProg, name);
	}
	
	public void reloadShader()
	{
		dispose();
		attachShader(fileProg);
	}
	public void printLog(int id)
	{
		//Takes the shader id and checks for compilation errors
		IntBuffer logLength = BufferUtils.createIntBuffer(1);
		
		//Copies a section of GPU memory into a 1 wide buffer (essentially an int)
		GL20.glGetShader(id, GL20.GL_INFO_LOG_LENGTH, logLength);
		int length = logLength.get();
		//Checks if there is anything in the log, if not close subroutine
		if (length <= 1)
		{
			return;
		}
		//Make a buffer to extract the bytes from the GPU memory
		ByteBuffer logData = BufferUtils.createByteBuffer(length);
		//Resets the intBuffer
		logLength.flip();
		
		//Shader,length,data variable  ///Length is reupdated
		GL20.glGetShaderInfoLog(id,logLength,logData);
		//Double check the length
		length = logLength.get();
		//Converts the ByteBuffer into a Buffer Array
		byte[] infoByte = new byte[length];
		logData.get(infoByte);
		//Creates a string from the bytes
		System.out.println(new String(infoByte));
		//Prints true or false if the shader is a legit shader
		System.out.println(GL20.glIsShader(id));
	}
	public void dispose()
	{
		GL20.glDeleteShader(glslProg);
		glslProg = 0;
		GL20.glDeleteShader(fragProgram);
		fragProgram = 0;
		GL20.glDeleteShader(vertProgram);
		vertProgram = 0;
		
	}
}
