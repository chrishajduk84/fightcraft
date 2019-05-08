/**
 * @author Chris Hajduk
 *
 * Started May 6, 2012
 */
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.lang.ArrayIndexOutOfBoundsException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class Sector {
	//This Sector id
	int SectX,SectY,SectZ;
	int quadrant;
	
	//Each sector is 16 x 16 x 16	
	int secX = 16;
	int secY = 16;
	int secZ = 16;
	byte block[][][] = new byte[secX][secY][secZ];
	byte vertex[];
	int[] indices;
	int vertexID, elementID;
	int elements;
	int attid;

	//To check if sector needs an update
	boolean update;


	public void set(int x, int y, int z, int type)
	{
		block[x][y][z] = (byte) type;
		update = true;
	}
	//Get the data-value of each block
	public synchronized byte get(int x, int y, int z)
	{
		byte value = 0;
		try
		{
			
			 value = block[x][y][z];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
		}
		
		int tx = x, ty = y, tz = z;
		
		//Compensate for reverse numeration in quadrants 2,3,4
		if (quadrant == 2)
			tz = 16 - z;
		if (quadrant == 3)
		{
			tx = 16 - x;
			tz = 16 - z;
		}
		if (quadrant == 4)
			tx = 16 - x;
		
		if (x > 15 || x < 0 || y > 15 || y < 0 || z > 15 || z < 0)
			if (SectX + tx >= 0 && SectY + ty >= 0 && SectZ + tz >= 0 && quadrant != 0)
				value = (byte)updateGraphics.quadrants[quadrant - 1].get((SectX * 16) + tx, (SectY * 16) + ty, (SectZ * 16) + tz);
			else
				value = 0;
		
		return value;
	}

	//Updates vertexes that need updating (have been changed)
	public void update()
	{
		update = false;
		vertex = new byte[secX * secY * secZ * 6 * 6 * 4]; //4 values per vertex, 6 vertices per side, 6 sides per cube, in each sector of a certain width, height, depth
		//Cycles through the sector
		int i = 0; //Counts the number of vertices
		for (byte x = 0; x < secX; x++)
		{
			for (byte y = 0; y < secY; y++)
			{
				for (byte z = 0; z < secZ; z++)
				{
					if (isVisible(x,y,z,"-x"))
					{
						///Store each vertex with (x,y,z) AND the Block Type.
						///Drawing Clockwise
						///LEFT SIDE (-X)	
						byte blockcode = block[x][y][z];
						vertex[i++] = x; 	vertex[i++] = y;				vertex[i++] = z;	 			vertex[i++] = blockcode;				//
						vertex[i++] = x;	vertex[i++] = y;   				vertex[i++] = (byte) (z + 1);	vertex[i++] = blockcode;				///First Triangle
						vertex[i++] = x;	vertex[i++] = (byte) (y + 1);	vertex[i++] = z;	 			vertex[i++] = blockcode;				//
						vertex[i++] = x;    vertex[i++] = (byte) (y + 1);	vertex[i++] = z;	 			vertex[i++] = blockcode;				//--
						vertex[i++] = x;	vertex[i++] = y;				vertex[i++] = (byte) (z + 1);	vertex[i++] = blockcode;				///Second Triangle--
						vertex[i++] = x;	vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1);	vertex[i++] = blockcode;				//
					}
				}
			}
		}

		for (byte x = 0; x < secX; x++)
		{
			for (byte y = 0; y < secY; y++)
			{
				for (byte z = 0; z < secZ; z++)
				{
					if (isVisible(x,y,z,"+x"))
					{
						byte blockcode = block[x][y][z];
						
						///RIGHT SIDE (+X)
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;				vertex[i++] = z; 	 			vertex[i++] = blockcode;				//
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = z; 	 			vertex[i++] = blockcode;				///First Triangle
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;				vertex[i++] = (byte) (z + 1); 	vertex[i++] = blockcode;				//
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = z;	 			vertex[i++] = blockcode;				//--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1); 	vertex[i++] = blockcode;				///Second Triangle--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;				vertex[i++] = (byte) (z + 1); 	vertex[i++] = blockcode;				//
					}
				}
			}
		}

		for (byte x = 0; x < secX; x++)
		{
			for (byte y = 0; y < secY; y++)
			{
				for (byte z = 0; z < secZ; z++)
				{
					if (isVisible(x,y,z,"-y"))
					{
						byte blockcode = block[x][y][z];
						if (block[x][y][z] == 2) //If it is a grass block make the bottom dirt
							blockcode = 1;

						///BOTTOM SIDE (-Y)
						vertex[i++] = x; 				vertex[i++] = y;	vertex[i++] = z;	 			vertex[i++] = (byte) -blockcode;				//
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;    vertex[i++] = z;	 			vertex[i++] = (byte) -blockcode;				///First Triangle
						vertex[i++] = x;				vertex[i++] = y;	vertex[i++] = (byte) (z + 1); 	vertex[i++] = (byte) -blockcode;				//
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;	vertex[i++] = z;	 			vertex[i++] = (byte) -blockcode;				//--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;	vertex[i++] = (byte) (z + 1); 	vertex[i++] = (byte) -blockcode;				///Second Triangle--
						vertex[i++] = x;				vertex[i++] = y;	vertex[i++] = (byte) (z + 1); 	vertex[i++] = (byte) -blockcode;				//
					}
				}
			}
		}

		for (byte x = 0; x < secX; x++)
		{
			for (byte y = 0; y < secY; y++)
			{
				for (byte z = 0; z < secZ; z++)
				{
					if (isVisible(x,y,z,"+y"))
					{
						byte blockcode = block[x][y][z];
						if (block[x][y][z] == 2) //If it is a grass block make the top grass
							blockcode = 3;

						///TOP SIDE (+Y)
						vertex[i++] = x; 				vertex[i++] = (byte) (y + 1);	vertex[i++] = z;	 			vertex[i++] = (byte) -blockcode;				//
						vertex[i++] = x;				vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1);	vertex[i++] = (byte) -blockcode;				///First Triangle
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = z;	 			vertex[i++] = (byte) -blockcode;				//
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = z;	 			vertex[i++] = (byte) -blockcode;				//--
						vertex[i++] = x;				vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1); 	vertex[i++] = (byte) -blockcode;				///Second Triangle--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1); 	vertex[i++] = (byte) -blockcode;				//
					}
				}
			}
		}
		
		for (byte x = 0; x < secX; x++)
		{
			for (byte y = 0; y < secY; y++)
			{
				for (byte z = 0; z < secZ; z++)
				{
					if (isVisible(x,y,z,"-z"))
					{
						byte blockcode = block[x][y][z];

						///FRONT SIDE (-Z)
						vertex[i++] = x; 				vertex[i++] = y;				vertex[i++] = z; vertex[i++] = blockcode;				//
						vertex[i++] = x;				vertex[i++] = (byte) (y + 1);	vertex[i++] = z; vertex[i++] = blockcode;				///First Triangle
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;				vertex[i++] = z; vertex[i++] = blockcode;				//
						vertex[i++] = x;    			vertex[i++] = (byte) (y + 1);	vertex[i++] = z; vertex[i++] = blockcode;				//--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = z; vertex[i++] = blockcode;				///Second Triangle--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;				vertex[i++] = z; vertex[i++] = blockcode;				//
					}
				}
			}
		}
		
		for (byte x = 0; x < secX; x++)
		{
			for (byte y = 0; y < secY; y++)
			{
				for (byte z = 0; z < secZ; z++)
				{
					if (isVisible(x,y,z,"+z"))
					{	
						byte blockcode = block[x][y][z];

						///BACK SIDE (+Z)
						vertex[i++] = x; 				vertex[i++] = y;				vertex[i++] = (byte) (z + 1); vertex[i++] = blockcode;				//
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;    			vertex[i++] = (byte) (z + 1); vertex[i++] = blockcode;				///First Triangle
						vertex[i++] = x;				vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1); vertex[i++] = blockcode;				//
						vertex[i++] = x;    			vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1); vertex[i++] = blockcode;				//--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = y;				vertex[i++] = (byte) (z + 1); vertex[i++] = blockcode;				///Second Triangle--
						vertex[i++] = (byte) (x + 1);	vertex[i++] = (byte) (y + 1);	vertex[i++] = (byte) (z + 1); vertex[i++] = blockcode;				//


					}
				}
			}
		}

		
		elements = i;
		//Creates a buffer object from the vertices
		ByteBuffer vbo = buffer.makeByteBuffer(vertex);
		vertexID = buffer.getBuffer(GL15.GL_ARRAY_BUFFER,vbo);
		vertex = null;
		vbo = null;


	}
	public void render()
	{
		//Update the sector
		if (update)
		{
			update();
			
			//Fix this so loading is faster
//			updateGraphics.quadrants[quadrant - 1].sec[SectX][SectY][SectZ + 1].update();
//			if (SectZ > 0)
//			updateGraphics.quadrants[quadrant - 1].sec[SectX][SectY][SectZ - 1].update();
//			updateGraphics.quadrants[quadrant - 1].sec[SectX + 1][SectY][SectZ].update();
//			if (SectX > 0)
//			updateGraphics.quadrants[quadrant - 1].sec[SectX - 1][SectY][SectZ].update();
//			updateGraphics.quadrants[quadrant - 1].sec[SectX][SectY + 1][SectZ].update();
//			if (SectY > 0)
//			updateGraphics.quadrants[quadrant - 1].sec[SectX][SectY - 1][SectZ].update();
		}
		//Then render the landspace if anything is there
		if (elements == 0)
			return;
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		//Binds Vertex data
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexID);
		//Loads attribute pointer variable
		GL20.glVertexAttribPointer(
				updateGraphics.cubeAttrib[0], 	//The attribute
				4,          		//4 elements (X,Y,Z,W)
				GL11.GL_BYTE,      //Data type
				false,				//0 - or don't modify/convert the values until being used
				0,					//Byte offset between values
				0					//Pointer to the first values (incase of a header)
				);
		GL20.glEnableVertexAttribArray(updateGraphics.cubeAttrib[0]);

		///THIS DRAWS THE CUBES
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, elements);

		GL20.glDisableVertexAttribArray(updateGraphics.cubeAttrib[0]);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

	}

	public boolean isVisible(int x1,int y1, int z1, String direction)
	{
		if (block[x1][y1][z1] == 0)
		{
			//Do not draw invisible objects
			return false;
		}
		//Check if any of the objects are covered
		//Do not draw if the object is surrounded by other cubes (in the x, y or z direction)
		if (direction == "-x" && get(x1 - 1, y1, z1) != 0)
		{
			return false;
		}
		if (direction == "+x" && get(x1 + 1, y1, z1) != 0)
		{
			return false;
		}
		if (direction == "-y" && get(x1, y1 - 1, z1) != 0)
		{
			return false;
		}
		if (direction == "+y" && get(x1, y1 + 1, z1) != 0)
		{
			return false;
		}
		if (direction == "-z" && get(x1, y1, z1 - 1) != 0)
		{
			return false;
		}
		if (direction == "+z" && get(x1, y1, z1 + 1) != 0)
		{
			return false;
		}
	
		return true;
	}
	public void sectorTranslate(float[] vector)
	{
		//Provides necessary information for translation to offset sectors
		GL20.glUniform3f(updateGraphics.cubeUniform[2],vector[0], vector[1], vector[2]);
	}
	public void generateLandscape(int biome, int xSector, int ySector, int zSector)
	{
				
		final int SEALEVEL = 50;
		byte tempBlockValue[][][] = new byte[secX][secY][secZ];
		int height[][] = new int[secX][secZ];
		float wavelength = 0, persistence = 0;
		int numFunctions = 0;
		float multiplier = 1;
		float levelHeight = 0;
		float precision = 256.0f;
		
		//ORDER OF OPERATIONS
		//1. Establish biome and set appropriate noise variables
		//2. Height Map is generated
		//3. Generate a solid sector of random block values (but in certain proportions)
		//// --zSector value is used to determine what types (ie.Rare blocks) of blocks are generated
		//4. Height Map is used to place dirt blocks appropriately
		//5. Extra blocks are removed from height map
		//6. Add Water blocks to appropriate areas
		
		//BIOME
		//0 - Flat land Grass
		//1 - Forest
		if (biome == 0)
		{
			precision = 56.0f;
			persistence = 0.5f;
			numFunctions = 15;//15;
			multiplier = 1;
		}
		if (biome == 1)
		{
			precision = 10.0f;
			persistence = 0.8f;
			numFunctions = 50;//15;
			multiplier = 2;
		}
		
		float xCoord = xSector /(precision * 15);
		float zCoord = zSector/(precision * 15);
		levelHeight = Math.abs((int)(updateGraphics.mapHeight.getNoise(xCoord, zCoord, persistence, numFunctions) * 20));
		//Gets the landscape height in each sector 
		//precision = 1024f;
		for (int x = 0; x < secX;x++)
		{
			for (int z = 0; z < secZ;z++)
			{
				xCoord = (xSector * secX + x) /precision; //Dividing coordinates creates less precision (not too random)(smoother)
				zCoord = (zSector * secZ + z) /precision;
				
				height[x][z] = Math.abs((int)(updateGraphics.mapHeight.getNoise(xCoord, zCoord, persistence, numFunctions) * multiplier + levelHeight));
							
				height[x][z] -= ySector * 16;

			}
		}
		
		//Generates appropriate block values here
		
		//Dirt Layer
		for (int x = 0; x < secX; x++)
		{
			for (int y = 0; y < secY; y++)
			{
				for (int z = 0; z < secZ; z++)
				{
					tempBlockValue[x][y][z] = 1;
				}
			}
		}
		
		
		
		//--- Sets everything to 0 that is greater than the specified height
		for (int x = 0; x < secX;x++)
		{
			for (int z = 0; z < secZ;z++)
			{
				for (int y = height[x][z]; y < secY; y++)
				{
					if (y >= 0)
						tempBlockValue[x][y][z] = 0;
				}
				if (height[x][z] > 0 && height[x][z] < 16)
				tempBlockValue[x][height[x][z] - 1][z] = 2;
			}
		}
		
		//Assigns the temporary value to the public value
		block = tempBlockValue;
		update = true;
		
	}



}
