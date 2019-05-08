/**
 * @author Chris Hajduk
 *
 * Started June 10th, 2012
 */
import org.lwjgl.BufferUtils;
import org.lwjgl.util.glu.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
public class unProject {


	public static boolean collisionCheck(Vector3f cameraDirection, Vector3f cameraPosition)
	{
		cameraPosition.y -= 1f;
		Vector3f test = new Vector3f();
		Vector3f.add(cameraDirection, cameraPosition, test);
		float x = test.x;
		float y = test.y;
		float z = test.z;
		int quadrant = getQuadrant(new float[]{x,y,z});
		int blockid = updateGraphics.quadrants[quadrant - 1].get((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));
		if(blockid != 0)
		{
			return false;
		}
		return true;
		
	}
	public static void rayCasting(Vector3f cameraDirection, Vector3f cameraPosition, int mode)
	{
		//Keeps adding the same vector until it hits a block
		//cameraDirection.normalise(cameraDirection);
		Vector3f test = cameraPosition;
		Vector3f last = cameraPosition;
		int blockid = 0;
		int quadrant;
		for (int i = 0; i < 5; i++)
		{
			last = test;
			Vector3f.add(last, cameraDirection, test);
			//Rounds down the values
			float x = test.x;
			float y = test.y;
			float z = test.z;
			quadrant = getQuadrant(new float[]{x,y,z});
			blockid = updateGraphics.quadrants[quadrant - 1].get((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));
			if(blockid != 0)
			{
				System.out.println("BlockID: " + blockid);
				System.out.println("X: " + Math.floor(test.x) + " Y: " + Math.floor(test.y) + " Z: " + Math.floor(test.z));
				if (mode == 1) //Destroy Blocks
					removeBlocks(new float[]{test.x,test.y,test.z,blockid});
				else if (mode == 2) //Place Blocks
					placeBlocks(new float[]{test.x,test.y,test.z},new float[]{cameraDirection.x,cameraDirection.y,cameraDirection.z});
				break;
			}
		}
		
	}
	public static void removeBlocks(float[] block)
	{
		int x = (int)Math.floor(block[0]);
		int y = (int)Math.floor(block[1]);
		int z = (int)Math.floor(block[2]);
		int quadrant = getQuadrant(new float[]{x,y,z});
			updateGraphics.quadrants[quadrant - 1].set(x, y,z, 0, false);
			
		int x1 = Math.abs(x);
		int y1 = Math.abs(y);
		int z1 = Math.abs(z);
			//Update Surrounding chunks
			//if (z1%16 == 15)
//			updateGraphics.quadrants[quadrant - 1].sec[x1/16][y1/16][z1/16 + 1].update();
//			//if (z1%16 == 0 && z1 > 0)
//			if (z1 > 0)
//			updateGraphics.quadrants[quadrant - 1].sec[x1/16][y1/16][z1/16 - 1].update();
//			//if (x1%16 == 15)
//			updateGraphics.quadrants[quadrant - 1].sec[x1/16 + 1][y1/16][z1/16].update();
//			//if (x1%16 == 0 && x1 > 0)
//			if (x1 > 0)
//			updateGraphics.quadrants[quadrant - 1].sec[x1/16 - 1][y1/16][z1/16].update();
//			//if (y1%16 == 15)
//			updateGraphics.quadrants[quadrant - 1].sec[x1/16][y1/16 + 1][z1/16].update();
//			//if (y1%16 == 0 && y1 > 0)
//			if (y1 > 0)
//			updateGraphics.quadrants[quadrant - 1].sec[x1/16][y1/16 - 1][z1/16].update();

	}
	public static void placeBlocks(float[] block,float[] camera)
	{
		int cx = (int)Math.floor(block[0]);
		int cy = (int)Math.floor(block[1]);
		int cz = (int)Math.floor(block[2]);
		
		//Find the change in the x,y,z values
		//The one that changes the least indicates the direction
		//TODO: Take abs values of camera
		
		if (camera[0] > camera[1] && camera[0] > camera[2] && camera[0] > 0)
		{
			//Facing the the +X direction
			//Thus, the block is facing the -X direction
			int quadrant = getQuadrant(new float[]{cx - 1,cy,cz});
			updateGraphics.quadrants[quadrant - 1].set(cx - 1, cy,cz, playerStatus.selectedBlock, false);
		}
		else if (camera[0] < camera[1] && camera[0] < camera[2] && camera[0] < 0)
		{
			//Facing the the -X direction
			//Thus, the block is facing the +X direction
			int quadrant = getQuadrant(new float[]{cx + 1,cy,cz});
			updateGraphics.quadrants[quadrant - 1].set(cx + 1, cy,cz, playerStatus.selectedBlock, false);
		}
		else if (camera[1] < camera[0] && camera[1] < camera[2] && camera[1] < 0)
		{
			//Facing the the -Y direction
			//Thus, the block is facing the +Y direction
			int quadrant = getQuadrant(new float[]{cx,cy + 1,cz});
			updateGraphics.quadrants[quadrant - 1].set(cx, cy + 1,cz, playerStatus.selectedBlock, false);
		}
		else if (camera[1] > camera[0] && camera[1] > camera[2] && camera[1] > 0)
		{
			//Facing the the +Y direction
			//Thus, the block is facing the -Y direction
			int quadrant = getQuadrant(new float[]{cx,cy - 1,cz});
			updateGraphics.quadrants[quadrant - 1].set(cx, cy - 1,cz, playerStatus.selectedBlock, false);
		}
		else if (camera[2] > camera[0] && camera[2] > camera[1] && camera[1] > 0)
		{
			//Facing the the +Z direction
			//Thus, the block is facing the -Z direction
			int quadrant = getQuadrant(new float[]{cx,cy,cz - 1});
			updateGraphics.quadrants[quadrant - 1].set(cx, cy,cz - 1, playerStatus.selectedBlock, false);
		}
		else if (camera[2] < camera[0] && camera[2] < camera[1] && camera[1] < 0)
		{
			//Facing the the -Z direction
			//Thus, the block is facing the +Z direction
			int quadrant = getQuadrant(new float[]{cx,cy,cz + 1});
			updateGraphics.quadrants[quadrant - 1].set(cx, cy,cz + 1, playerStatus.selectedBlock, false);
			
		}
		
	}
	
	public static int getQuadrant(float[] block)
	{
		int quadrant = 0;
		if (block[0] >= 0 && block[2] >= 0) //QUADRANT 1
			quadrant = 1;
		else if (block[0] >= 0 && block[2] < 0) //QUADRANT 2
			quadrant = 2;
		else if (block[0] < 0 && block[2] < 0) //QUADRANT 3
			quadrant = 3;
		else if (block[0] < 0 && block[2] >= 0) //Quadrant 4
			quadrant = 4;
		
		return quadrant;
	}
	
	public static float getFractionalValue(float i)
	{
		return i - Math.round(i);
	}
	

}