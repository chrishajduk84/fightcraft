/**
 * @author Chris Hajduk
 *
 * Started May 14th, 2012
 */
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f; 
import org.lwjgl.util.vector.Vector4f; 
import org.newdawn.slick.opengl.Texture;

public class buffer {

	public static IntBuffer makeIntBuffer(int[] array)
	{
		IntBuffer intBuf = BufferUtils.createIntBuffer(array.length);
		
		for (int i:array) ///For each i within the array
		{
			intBuf.put(i); ///Place the value into the buffer
		}
		intBuf.flip();
		return intBuf;
	}
	public static FloatBuffer makeFloatBuffer(float[] array)
	{
		FloatBuffer floBuf = BufferUtils.createFloatBuffer(array.length);
		for (float i:array)
		{
			floBuf.put(i);
		}
		floBuf.flip();
		return floBuf;
	}
	public static ByteBuffer makeByteBuffer(byte[] array)
	{
		ByteBuffer byteBuf = BufferUtils.createByteBuffer(array.length);
		for (byte i:array)
		{
			byteBuf.put(i);
		}
		byteBuf.flip();
		return byteBuf;
	}
	public static FloatBuffer makeFloatBuffer(Matrix4f matrix)
	{
		//This converts a matrix into a float array and applies the above function
		float[] floNum = {matrix.m00,matrix.m01,matrix.m02,matrix.m03,matrix.m10,matrix.m11,matrix.m12,matrix.m13,matrix.m20,matrix.m21,matrix.m22,matrix.m23,matrix.m30,matrix.m31,matrix.m32,matrix.m33};
		
		return makeFloatBuffer(floNum);
	}
	public static int getBuffer(int target, FloatBuffer bufferData)
	{
		//target must be either GL_ARRAY_BUFFER or GL_ELEMENT_ARRAY_BUFFER
		IntBuffer bufferid = BufferUtils.createIntBuffer(1); //Buffer with size of 1
		GL15.glGenBuffers(bufferid);
		int buffer = bufferid.get(0); //Captures the ID of the buffer from the IntBuffer
		GL15.glBindBuffer(target, buffer); //Attaches to the buffer
		GL15.glBufferData(target,bufferData,GL15.GL_STATIC_DRAW); //Writes the Data to the buffer
		GL15.glBindBuffer(target, 0); //Disassociates from the buffer
		return buffer;
	}
	public static int getBuffer(int target, ByteBuffer bufferData)
	{
		//target must be either GL_ARRAY_BUFFER or GL_ELEMENT_ARRAY_BUFFER
		IntBuffer bufferid = BufferUtils.createIntBuffer(1); //Buffer with size of 1
		GL15.glGenBuffers(bufferid);
		int buffer = bufferid.get(0); //Captures the ID of the buffer from the IntBuffer
		GL15.glBindBuffer(target, buffer); //Attaches to the buffer
		GL15.glBufferData(target,bufferData,GL15.GL_STATIC_DRAW); //Writes the Data to the buffer
		GL15.glBindBuffer(target, 0); //Disassociates from the buffer
		return buffer;
	}
	public static int getBuffer(int target, IntBuffer bufferData)
	{
		IntBuffer bufferid = BufferUtils.createIntBuffer(1); //Buffer of size of 1
		GL15.glGenBuffers(bufferid);
		int buffer = bufferid.get(0); //Captures the ID of the buffer from the IntBuffer
		GL15.glBindBuffer(target, buffer); //Attaches to the buffer
		GL15.glBufferData(target, bufferData,GL15.GL_STATIC_DRAW); //Writes the Data to the buffer
		GL15.glBindBuffer(target, 0); //Disassociates from the buffer
		return buffer;
	}

}
