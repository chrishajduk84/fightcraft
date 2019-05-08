import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * @author Chris Hajduk
 *
 * Created May 31, 2012
 */
public class superSector implements Runnable {
	//Simply a instance that calls many instances of a Sector
	final int sSecX = 99; 	//10
	final int sSecY = 16;	//3
	final int sSecZ = 99;	//10
	Sector[][][] sec = new Sector[sSecX][sSecY][sSecZ];
	
	ArrayList <int[]>iniList = new ArrayList();
	ArrayList <int[]>renderList = new ArrayList();
	ArrayList <int[]>deiniList = new ArrayList();
	boolean deInitialize[][][] = new boolean[sSecX][sSecY][sSecZ];

	int xpos,ypos,zpos;
	float angleX;
	
	static float pi180 = (float)Math.PI/180.0f;

	int renderDistance = 8;
	int vertRenderDistance = 2;
	int nvrenderDistance = renderDistance / 2;
	
	int iniDistance = 1;
	int deIniDistance = iniDistance + 1;
	int quadrant;

	volatile boolean stopThread = false;

	public void stopInitializationThread()
	{
		stopThread = true;
	}


	public void run()
	{
		//New Thread for initialization
		while (!stopThread)
		{

			initialize();
			Thread.yield();
			cleanup();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		}

	}
	
	public void initialize()
	{
		//Generate a list based on the last change in fps (lag)
		//for (int i = updateGraphics.deltaFps; i > 0; i--)
		//{
		//Only add one to the list per render loop to minimize lag
		//And if the frame rate is greater than 60 frames
			overloop:
			for (int d = 0; d < renderDistance + iniDistance; d++)
			{
				for (int angle = (int) (angleX - 30f); angle <= (int)(angleX + 30f); angle++)
				{
					int x = (int)Math.floor(Math.sin(angle * pi180) * d) + xpos/16;
					int z = (int)Math.floor(Math.cos((180 - angle) * pi180) * d) + zpos/16;
					for (int y = (ypos/16 - 1 - vertRenderDistance >= 0)?(ypos/16 - 1) - vertRenderDistance:0; y < (ypos/16 + 1) + vertRenderDistance; y++)
					{
						
						int val[] = getArrayQuadrantValues(x,y,z,quadrant);
						int x1 = val[0];
						int y1 = val[1];
						int z1 = val[2];
						//If it isn't a valid coordinate (in the superSector) break the loop
						if (val[3] == 1)
							break;
						
						if (x1 >= 0 && y1 >= 0 && z1 >= 0 && x1 < sSecX && y1 < sSecY && z1 < sSecZ)
						{
							if (sec[x1][y1][z1] == null)
							{
								sec[x1][y1][z1] = new Sector();
								sec[x1][y1][z1].generateLandscape(0, x, y, z);
								
								//Fix Request System later
								readwriteData.request.add(new int[]{quadrant,x1,y1,z1});

								sec[x1][y1][z1].SectX = x1;
								sec[x1][y1][z1].SectY = y1;
								sec[x1][y1][z1].SectZ = z1;
								sec[x1][y1][z1].quadrant = quadrant;
								iniList.add(new int[]{x1,y1,z1});
								break overloop;
							}
						}
					}
				}
			}
		

				

	}
	public void set(int x, int y, int z, int type, boolean diskread)
	{
		int secX = Math.abs(x/16);
		int secY = Math.abs(y/16);
		int secZ = Math.abs(z/16);
		int remX = Math.abs(x%16);
		int remY = Math.abs(y%16);
		int remZ = Math.abs(z%16);

		//All quadrants meet at the center
		//Quadrant 1 contains x=0,z=0
		if (secX < sSecX & secY < sSecY & secZ < sSecZ)
		{
			if (quadrant == 2)
			{
				//secZ++;
				if (remZ == 0)
				{
					secZ--;
					remZ = 16;
				}
				remZ = 16 - remZ;


			}
			if (quadrant == 3)
			{
				//secZ++;
				//secX++;

				if (remX == 0)
				{
					secX--;
					remX = 16;
				}
				if (remZ == 0)
				{
					secZ--;
					remZ = 16;
				}

				remZ = 16 - remZ;
				remX = 16 - remX;

			}
			if (quadrant == 4)
			{
				//If the block number is 16 stay in the same sector but make remX = 16 - remX = 0 (array goes from 0 to 15)
				//secX++;	
				if (remX == 0)
				{
					secX--;
					remX = 16;
				}
				remX = 16 - remX;


			}
			
			if (secX >= 0 && secY >= 0 && secZ >= 0)
			{
				sec[secX][secY][secZ].set(remX,remY,remZ,type);
				if (!diskread)
					readwriteData.send.add(new int[]{quadrant, Math.abs(x), Math.abs(y), Math.abs(z), type});
			}
		}

	}
	public int get(int x, int y, int z)
	{
		int sectorX = Math.abs(x/16);
		int sectorY = Math.abs(y/16);
		int sectorZ = Math.abs(z/16);
		int remX = Math.abs(x%16);
		int remY = Math.abs(y%16);
		int remZ = Math.abs(z%16);
		if (sectorX < sSecX && sectorY < sSecY && sectorZ < sSecZ && sec[sectorX][sectorY][sectorZ] != null)
		{
			if (quadrant == 2)
			{
				//sectorZ++;
				if (remZ == 0)
				{
					sectorZ--;
					remZ = 16;
				}
				remZ = 16 - remZ;


			}
			if (quadrant == 3)
			{
				//sectorZ++;
				//sectorX++;

				if (remX == 0)
				{
					sectorX--;
					remX = 16;
				}
				if (remZ == 0)
				{
					sectorZ--;
					remZ = 16;
				}

				remZ = 16 - remZ;
				remX = 16 - remX;

			}
			if (quadrant == 4)
			{
				//If the block number is 16 stay in the same sector but make remX = 16 - remX = 0 (array goes from 0 to 15)
				//sectorX++;	
				if (remX == 0)
				{
					sectorX--;
					remX = 16;
				}
				remX = 16 - remX;


			}
			
			if (sectorX >= 0 && sectorZ >= 0 && sectorY >= 0 && sec[sectorX][sectorY][sectorZ] != null)
					return sec[sectorX][sectorY][sectorZ].get(remX, remY, remZ);
			else
				return 0;
		}
		else
			return 0;
	}
	
	public void render()
	{
		xpos = (int)updateGraphics.xpos;
		ypos = (int)updateGraphics.ypos;
		zpos = (int)updateGraphics.zpos;
		angleX = updateGraphics.angleX;
		//Read Sectors
		//initialize();
		//initialize(xpos,ypos,zpos);
		
		
		//Draw Sectors
		for (int i = 0; i < iniList.size(); i++)
		{
			int[] temp = iniList.get(i);			
			try
			{
				
			
			//Check for negative coordinates in x and z direction
			if  (quadrant == 1 && temp[0] < sSecX && temp[1] < sSecY && temp[2] < sSecZ) // Quadrant 1 +X +Z
			{
				sec[temp[0]][temp[1]][temp[2]].sectorTranslate(new float[]{temp[0] * sec[temp[0]][temp[1]][temp[2]].secX, temp[1] * sec[temp[0]][temp[1]][temp[2]].secY, temp[2] * sec[temp[0]][temp[1]][temp[2]].secZ});
				sec[temp[0]][temp[1]][temp[2]].render();

			}
			else if  (quadrant == 2 && temp[0] < sSecX && temp[1] < sSecY && temp[2] < sSecZ) //Quadrant 2 +X -Z
			{
				sec[temp[0]][temp[1]][temp[2]].sectorTranslate(new float[]{temp[0] * sec[temp[0]][temp[1]][temp[2]].secX, temp[1] * sec[temp[0]][temp[1]][temp[2]].secY, (-temp[2] - 1) * sec[temp[0]][temp[1]][temp[2]].secZ});
				sec[temp[0]][temp[1]][temp[2]].render();
			}
			else if  (quadrant == 3 && temp[0] < sSecX && temp[1] < sSecY && temp[2] < sSecZ) //Quadrant 3 -X -Z
			{
				sec[temp[0]][temp[1]][temp[2]].sectorTranslate(new float[]{(-temp[0] - 1) * sec[temp[0]][temp[1]][temp[2]].secX, temp[1] * sec[temp[0]][temp[1]][temp[2]].secY, (-temp[2] - 1) * sec[temp[0]][temp[1]][temp[2]].secZ});
				sec[temp[0]][temp[1]][temp[2]].render();
			}				
			else if  (quadrant == 4 && temp[0] < sSecX && temp[1] < sSecY && temp[2] < sSecZ) //Quadrant 4 -X +Z
			{
				sec[temp[0]][temp[1]][temp[2]].sectorTranslate(new float[]{(-temp[0] - 1) * sec[temp[0]][temp[1]][temp[2]].secX, temp[1] * sec[temp[0]][temp[1]][temp[2]].secY, temp[2] * sec[temp[0]][temp[1]][temp[2]].secZ});
				sec[temp[0]][temp[1]][temp[2]].render();
			}
			
//			int val[] = getArrayQuadrantValues(xpos/16,ypos/16,zpos/16,quadrant);
//			int x1 = val[0];
//			int y1 = val[1];
//			int z1 = val[2];
			
//			if (temp[0] > x1 + renderDistance + iniDistance || temp[1] > y1 + vertRenderDistance + iniDistance ||  temp[2] > z1 + renderDistance + iniDistance || temp[0] < x1 - renderDistance - iniDistance || temp[1] < y1 - vertRenderDistance - iniDistance ||  temp[2] < z1 - renderDistance - iniDistance)
//			{
//				deiniList.add(temp);
//				//deInitialize[x1][y1][z1] = true;
//			}

			}catch (NullPointerException e)
			{
				
			}
			
			
		}
		//cleanup();
	}
	public void cleanup()
	{
		//Remove Unused Sectors
		//Save chunks to hard disk
		
//		for (int i = 0; i < deiniList.size(); i++)
//		{
//			int[] temp = deiniList.get(i);
//			if (deInitialize[temp[0]][temp[1]][temp[2]] == true)
//			{
//				sec[temp[0]][temp[1]][temp[2]] = null;
//				deiniList.remove(i);
//			}
//			deInitialize[temp[0]][temp[1]][temp[2]] = true;
//		}
//		overloop:
//		for (int d = renderDistance + deIniDistance; d > 0 ; d--)
//		{
//			for (int angle = (int) (angleX + 45f); angle <= (int)(angleX + 315f); angle++)
//			{
//				int x = (int)Math.floor(Math.sin(angle * pi180) * d) + xpos/16;
//				int z = (int)Math.floor(Math.cos((180 - angle) * pi180) * d) + zpos/16;
//				for (int y = ypos/16 - vertRenderDistance + deIniDistance; y < ypos/16 + vertRenderDistance + deIniDistance; y++)
//				{
//					
//					int val[] = getArrayQuadrantValues(x,y,z,quadrant);
//					int x1 = val[0];
//					int y1 = val[1];
//					int z1 = val[2];
//					//If it isn't a valid coordinate (in the superSector) break the loop
//					if (val[3] == 1)
//						break;
//					
//					if (x1 >= 0 && y1 >= 0 && z1 >= 0)
//					{
//						if (sec[x1][y1][z1] != null)
//						{
//							if (deInitialize[x1][y1][z1] == true)
//							{
//								renderList.remove(new int[]{x1,y1,z1});
//								sec[x1][y1][z1] = null;
//								break overloop;
//							}
//							deInitialize[x1][y1][z1] = true;
//						}
//					}
//				}
//			}
//		}
		//Remove Oldest chunks
//		for (int i = 100; i < iniList.size(); i++)
//		{
//			int temp[] = iniList.get(i);
//			iniList.remove(temp);
//			renderList.remove(temp);
//			sec[temp[0]][temp[1]][temp[2]] = null;
//		}
		//Remove everything out of the render distance
		for (int i = 0; i < iniList.size(); i++)
		{

				int temp[] = iniList.get(i);
				double distance = Math.sqrt(Math.pow(Math.abs(xpos/16) - temp[0],2) + Math.pow(Math.abs(ypos/16) - temp[1],2) + Math.pow(Math.abs(zpos/16) - temp[2],2));
				if (distance >= renderDistance + deIniDistance && sec[temp[0]][temp[1]][temp[2]] != null)
				{
					iniList.remove(temp);
					sec[temp[0]][temp[1]][temp[2]] = null;
					break;
				}

			}

	}
	public int[] getArrayQuadrantValues(int x, int y, int z, int quadrant)
	{

		//Alters the positive/negative values based on the quadrant

		int[] returnCoord = new int[4];
		if (quadrant == 1) // +X,+Z
		{
			if (x >= 0)
				returnCoord[0] = x;
			else
				returnCoord[3] = 1;
			if (y >= 0)
				returnCoord[1] = y;
			else
				returnCoord[3] = 1;
			if (z >= 0)
				returnCoord[2] = z;
			else
				returnCoord[3] = 1;
		}
		else if (quadrant == 2) //+X,-Z
		{
			if (x >= 0)
				returnCoord[0] = x;
			else
				returnCoord[3] = 1;
			if (y >= 0)
				returnCoord[1] = y;
			else
				returnCoord[3] = 1;
			if (z <= 0)
				returnCoord[2] = -z;
			else
				returnCoord[3] = 1;
		}
		else if (quadrant == 3) //-X,-Z
		{
			if (x <= 0)
				returnCoord[0] = -x;
			else
				returnCoord[3] = 1;
			if (y >= 0)
				returnCoord[1] = y;
			else
				returnCoord[3] = 1;
			if (z <= 0)
				returnCoord[2] = -z;
			else
				returnCoord[3] = 1;
		}
		else if (quadrant == 4) //-X,+Z
		{
			if (x <= 0)
				returnCoord[0] = -x;
			else
				returnCoord[3] = 1;
			if (y >= 0)
				returnCoord[1] = y;
			else
				returnCoord[3] = 1;
			if (z >= 0)
				returnCoord[2] = z;
			else
				returnCoord[3] = 1;
		}
		return returnCoord;
	}

}
