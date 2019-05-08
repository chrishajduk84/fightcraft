/**
 * @author Chris Hajduk
 *
 * Started June 11th, 2012
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class readwriteData implements Runnable{

	float xpos = updateGraphics.xpos;
	float ypos = updateGraphics.ypos;
	float zpos = updateGraphics.zpos;

	static File data, tempFile;
	static volatile RandomAccessFile handle, tempHandle;

	//static ArrayList <int[]>request = new ArrayList();
	//static ArrayList <int[]>send = new ArrayList();
	static List<int[]> request = Collections.synchronizedList(new ArrayList<int[]>());
	static List<int[]> send = Collections.synchronizedList(new ArrayList<int[]>());

	static volatile boolean stopThread;

	static long fileLength;
	static long headerLength;
	static long seedLocation;
	static long charDataStart;
	static long charDataEnd;
	static long worldDataStart;
	static long q1,q2,q3,q4;
	static long worldDataEnd;

	static int x,y,z,quad;


	////////////////////////////////////////////////////////////
	// DATA STRUCTURE OF THE .MINE FILE
	////////////////////////////////////////////////////////////
	//Line	Data								Data Length
	//HEADER
	//1: 01111110(Header Start)						1 byte
	//2: 00000000(Number of bytes in text file)  	8 byte
	//3: 00000000(Header length [byte wise]) 		8 byte
	//4: 00000000(Location of Seed) 				8 byte
	//5: 00000000(Byte number of character data start) 8 byte
	//6: 00000000(Byte number of character data end)8 byte
	//7: 00000000(Byte number of world data start) 	8 byte
	//8: 00000000(Byte number of q1 data)			8 byte
	//9: 00000000(Byte number of q2 data)			8 byte
	//10: 00000000(Byte number of q3 data)			8 byte
	//11: 00000000(Byte number of q4 data)			8 byte
	//12: 00000000(Byte number of world data end)	8 byte
	//13: 10000001(Header End)						1 byte
	//DATA
	//14: 00000000...(Seed)								4 bytes(int)
	//15: 00000000(Character Data Start [# of characters)4 byte
	//n*L+1: 00000000(X position)						4 byte
	//n*L+2: 00000000(Y position)						4 byte
	//n*L+3: 00000000(Z position)						4 byte
	//n*L+4: 00000000(xAngle)							4 byte
	//n*L+5: 00000000(yAngle)							4 byte
	//n*L+6: 00000000(zAngle)							4 byte
	//n*L+7: 00000000(Health Value)						4 byte
	//n*L+8: 00000000(Block Value)						4 byte
	//n*L+9: 00000000(Character Data End [# of characters])4 byte
	//n*L+9 = c
	//c+1: 00000000(World data header)				1 byte
	//n*L+c+1: 11111111(Block data quadrant Start)			1 byte
	//n*L+c+2: 00000000...(X Location)					4 byte
	//n*L+c+3: 00000000...(Y Location)					4 byte
	//n*L+c+4: 00000000...(Z Location)					4 byte
	//n*L+c+5: 00000000...(Block ID)					1 byte
	//n*L+c+6: 00000000(World data end)				1 byte
	public readwriteData()
	{
		//		x = sectorX * 16;
		//		y = sectorY * 16;
		//		z = sectorZ  * 16;
		//		quad = quadrant;
	}


	public void run()
	{
		///The thread that reads Hard Drive data and places it into the RAM
		///Reads changes to the landscape (made by the user)
		while(!stopThread)
		{
			//Gets sector data
			if (request.size() > 0)
			{
				int[] temp = request.get(0);
				if (temp != null)
				{
					ArrayList <int[]> blocks = readwriteData.readData(temp[0], temp[1], temp[2], temp[3]);
					for (int i = 0; i < blocks.size(); i++)
					{
						int[] block = (int[]) blocks.get(i);
						if (block[4] != -1 && temp[0] > 0)
							updateGraphics.quadrants[temp[0] - 1].set(block[1], block[2], block[3], block[4], true);
					}
					blocks.clear();

				}
				if (!request.remove(temp)) System.out.println("Leak");
			}
			//Saves Sector data
			if (send.size() > 0)
			{
				int[] temp = send.get(0);
				if (temp != null)
					writeTempWorldData(temp[0],temp[1],temp[2],temp[3],temp[4]);
				if (!send.remove(temp)) System.out.println("Leak Send");
			}
		}

	}
	public void stopThread()
	{
		stopThread = true;
	}


	public static int extractSeed()
	{
		int seedValue = 0;
		try
		{
			//Pointer set to a given location
			handle.seek(seedLocation);
			//Bytes extracted
			byte[] rawSeed = new byte[4];
			handle.read(rawSeed);
			//Converted into a buffer
			ByteBuffer buffer = ByteBuffer.wrap(rawSeed);
			//Used to extract an numeric value
			seedValue = buffer.getInt();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		return seedValue;

	}
	public static void writeCharacterData(int xpos, int ypos, int zpos, float xangle, float yangle, float zangle, int health, int block)
	{
		try
		{
			handle.seek(charDataStart + 1);
			handle.writeInt(xpos);
			handle.writeInt(ypos);
			handle.writeInt(zpos);
			handle.writeFloat(xangle);
			handle.writeFloat(yangle);
			handle.writeFloat(zangle);
			handle.writeInt(health);
			handle.writeInt(block);

		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public static float[] readCharacterData()
	{
		float[] charData = new float[8];
		try
		{

			handle.seek(charDataStart + 1);
			charData[0] = handle.readInt(); //X position
			charData[1] = handle.readInt();	//Y position
			charData[2] = handle.readInt();	//Z position
			charData[3] = handle.readFloat();	//X angle
			charData[4] = handle.readFloat();	//Y angle
			charData[5] = handle.readFloat();	//Z angle
			charData[6] = handle.readInt();	//Health
			charData[7] = handle.readInt();	//Block #
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return charData;
	}
	public static void writeWorldData(int quadrant, int x, int y, int z, int block)
	{
		try
		{
			//If this block was changed, rewrite the block
			if (seekWorldData(quadrant,x,y,z,true))
			{
				handle.writeInt(x);
				handle.writeInt(y);
				handle.writeInt(z);
				handle.writeInt(block);
			}
			else //Shift everything down one and write the data in that position
			{
				long pos = handle.getFilePointer();

				byte[] tempData = new byte[16]; //16 bytes needed to move
				byte[] tempData2 = new byte[16];

				handle.read(tempData);

				//Problem somewhere here -REWRITE- SORTING IS GOOD
				while (handle.getFilePointer() <= fileLength)
				{

					long tempPos = handle.getFilePointer();
					//if (tempPos2 <= handle.length() + 16)
					handle.read(tempData2);

					//Reading Zeros for some reason???
					handle.seek(tempPos);
					handle.write(tempData);
					tempData = tempData2;
				}
					handle.write(tempData);
				//Seek back to the original position
				handle.seek(pos);
				handle.writeInt(x);
				handle.writeInt(y);
				handle.writeInt(z);
				handle.writeInt(block);

				//Then update the qudarant headers
				if (quadrant == 1)
				{
					q2 += 16;
				}
				if (quadrant <= 2)
				{
					q3 += 16;
				}
				if (quadrant <= 3)
				{
					q4 += 16;
				}
				if (quadrant <= 4)
				{
					fileLength += 16;
					worldDataEnd += 16;
				}
			}


		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}		
	public static synchronized ArrayList<int[]> readWorldData(int quadrant, int x, int y, int z)
	{
		ArrayList <int[]> data = new ArrayList<int[]>();
		try{
			//if (seekWorldData(quadrant,x,y,z))
			//{
			seekWorldData(quadrant, x * 16, y * 16, z * 16, false);
			//While there is valid data to read
			//long t = handle.getFilePointer();
			while (handle.getFilePointer() <= (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0)))) - 16)
			{
				int temp[] = new int[5];
				temp[0] = quadrant;
				for (int i = 1; i < 5; i++)
				{
					temp[i] = handle.readInt();		 // 4 + 4 + 4 + 4 = 16
				}
				data.add(temp);
			}

			return data;

		} catch (IOException e)
		{
			//e.printStackTrace();
			return data;
		}
	}
	public static void writeTempWorldData(int quadrant, int x, int y, int z, int block)
	{
		try {
			tempHandle.seek(tempHandle.length());
			tempHandle.writeInt(quadrant); //4 bytes
			tempHandle.writeInt(x);			// 4 bytes
			tempHandle.writeInt(y);			// 4 bytes
			tempHandle.writeInt(z);			// 4 bytes
			tempHandle.writeInt(block);		// 4 bytes = 20 bytes per entry
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static int[][] readTempWorldData()
	{
		int[][] data = null;
		try
		{
			data = new int[(int) (tempHandle.length() / 20)][5];

			tempHandle.seek(0);

			for (int i = 0; i < tempHandle.length() / 20; i++)
			{
				data[i][0] = tempHandle.readInt(); //Quadrant
				data[i][1] = tempHandle.readInt(); //X
				data[i][2] = tempHandle.readInt(); //Y
				data[i][3] = tempHandle.readInt(); //Z
				data[i][4] = tempHandle.readInt(); //Block
			}

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return data;
	}

	public static void saveNsort()
	{
		//Collects all the temporary data and writes it into the permanent file
		int[][] dat = readTempWorldData();
		for (int[] i:dat)
			writeWorldData(i[0],i[1],i[2],i[3],i[4]);
		try {
			writeHeader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static ArrayList<int[]> readData(int quadrant, int x, int y, int z)
	{
		//Read/search temp file, then read data file

		int[][] search = readTempWorldData();
		ArrayList <int[]>returnList = new ArrayList<int[]>();
		for (int i = 0; i < search.length; i++)
		{
			//Check if it is the correct quadrant,x,y,z
			if (search[i][0] == quadrant && search[i][1] >= x * 16 && search[i][1] < (x + 1) * 16 && search[i][2] >= y * 16 && search[i][2] < (y + 1) * 16 && search[i][3] >= z * 16 && search[i][3] < (z + 1) * 16)
			{
				returnList.add(search[i]);
			}
		}
		//And search the main file
		
		//search = new int[1][1];
		ArrayList <int[]> search2 = readWorldData(quadrant,x,y,z);
		//readWorldData(quadrant,x,y,z).toArray(search);
		
  		for (int i = 0; i < search2.size(); i++)
		{
  			int[] temp = search2.get(i);
			if (temp != null)
			//Check if it is the correct quadrant,x,y,z
			if (temp[0] == quadrant && temp[1] >= x * 16 && temp[1] < (x + 1) * 16 && temp[2] >= y * 16 && temp[2] < (y + 1) * 16 && temp[3] >= z * 16 && temp[3] < (z + 1) * 16)
			{
				returnList.add(temp);
			}
		}


		return returnList;
	}

	public synchronized static boolean seekWorldData(int quadrant, int x, int y, int z, boolean write)
	{
		//All values are int
		//x,y,z,blockid;etc;

		try
		{
//			seekQuadHeader(quadrant);
//			handle.skipBytes(1);
//			int[] t = new int[1000];
//			int i = 0;
//			while (handle.getFilePointer() <= (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0)))))
//			{
//				t[i++] = handle.readInt();
//			}

			
			//Seek to the appropriate quadrant location
			seekQuadHeader(quadrant);
			handle.skipBytes(1); //Header is one byte
			long limit = (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0))));
			int x1,y1,z1,x2,y2;
			if (handle.getFilePointer() <= (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0)))) - 16)
			{
				
				do
				{
					x1 = handle.readInt();
					x2 = x1;
					//y1 = handle.readInt();
					//z1 = handle.readInt();
//					x1 = handle.readInt();
//					//Skip   4 + 4   +   4      + 4
//					//Skip y + z + block + Next x
//					handle.skipBytes(12); // Plus the 4 bytes skipped from read
//				} while (x > x1 && handle.getFilePointer() < limit);
//				
//				handle.seek(handle.getFilePointer() - 12);   // +4 - 16 =   -12
//				
//				do
//				{
//					y1 = handle.readInt();
//					//Skip   4 + 4   +   4      + 4
//					//Skip y + z + block + Next x
//					handle.skipBytes(12); // Plus the 4 bytes skipped from read
//				} while (y > y1 && handle.getFilePointer() < limit);
//			
//				handle.seek(handle.getFilePointer() - 12);   // +4 - 16 =   -12
//				
//				do
//				{
//					z1 = handle.readInt();
//					//Skip   4 + 4   +   4      + 4
//					//Skip y + z + block + Next x
//					handle.skipBytes(12); // Plus the 4 bytes skipped from read
//				} while (z > z1 && handle.getFilePointer() < limit);
//
					//				handle.seek(handle.getFilePointer() - 16 - 8); //Move back 4 bytes (to compensate for readInt()) + 12 skipped bytes + 8 (to get back to x)
					handle.skipBytes(12);
					if (x1 >= x)
					{
						break;
					}
					
					
				} while (handle.getFilePointer() <= (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0)))));
								
				handle.seek(handle.getFilePointer() - 16);
				
				do
				{
				x1 = handle.readInt();
				y1 = handle.readInt();
				y2 = y1;
				handle.skipBytes(8);
					if (y1 >= y && x1 == x2)
					{
						break;
					}
				} while (handle.getFilePointer() <= (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0))))  && x1 == x2);
				
				
				handle.seek(handle.getFilePointer() - 16);
				
				do
				{
				x1 = handle.readInt();
				y1 = handle.readInt();
				z1 = handle.readInt();
				handle.skipBytes(4);
					if (z1 >= z && y1 == y2 && x1 == x2)
					{
						break;
					}
				} while (handle.getFilePointer() <= (quadrant == 4?worldDataEnd:(quadrant == 3?q4:(quadrant == 2?q3:(quadrant == 1?q2:0)))) && y1 == y2 && x1 == x2);
				
				handle.seek(handle.getFilePointer() - 16);
				
				x1 = handle.readInt();
				y1 = handle.readInt();
				z1 = handle.readInt();
				if (x == x1 && y == y1 && z == z1)
				{
					//Leaves file pointer at the current X value
					handle.seek(handle.getFilePointer() - 12);
					return true;
				}
				else if (write)
				{
					//Leaves file pointer at the next X value
					handle.seek(handle.getFilePointer() - 12);
					//handle.skipBytes(4);
					return false;
				}
				else
				{
					//Leaves file pointer at the current X value if reading
					handle.seek(handle.getFilePointer() - 12);
					return false;
				}
			}
			else
				return false;
		}catch (IOException e)
		{
			e.printStackTrace();
			try {
				handle.seek(q1 + 1);
			} catch (IOException e1) {
				//e1.printStackTrace();
			}
		}
		return false;
	}
	public static  void seekQuadHeader(int quadrant)
	{
		try
		{
			if (quadrant == 1)
				handle.seek(q1);
			else if (quadrant == 2)
				handle.seek(q2);
			else if (quadrant == 3)
				handle.seek(q3);
			else if (quadrant == 4)
				handle.seek(q4);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static long[] getHeader(RandomAccessFile handle) throws IOException
	{
		long[] headerValues = new long[13];
		//0 = Header Start Offset, 1 = File length, 2 = Header Length, 3 = Location of Seed,
		//4 = Offset of character data start, 5 = Offset of character data end,
		//6 = Offset of world data start, 7 = Offset of q1 data, 8 = Offset of q2 data,
		//9 = Offset of q3 data, 10 = Offset of q4 data, 11 = Offset of world data end,


		//Start at the beginning of file
		handle.seek(0);
		//Check if there is a file offset
		while(handle.readByte() != (byte)(01111110))
		{
			headerValues[0]++;
		}
		headerValues[1] = handle.readLong();
		headerValues[2] = handle.readLong();
		for (int i = 3; i < headerValues[2] /8; i++) // Don't read the header end value
		{
			headerValues[i] = handle.readLong();

		}

		return headerValues;

	}

	public static void writeHeader() throws IOException
	{

		handle.seek(0);
		handle.writeByte((byte)(01111110)); //Start Pattern
		handle.writeLong(fileLength);		//File Length
		handle.writeLong(headerLength);		//Header Length
		handle.writeLong(seedLocation);		//Seed Location
		handle.writeLong(charDataStart);	//Character Data
		handle.writeLong(charDataEnd);		//Character Data ends
		handle.writeLong(worldDataStart);	//World Data
		handle.writeLong(q1);				//Q1
		handle.writeLong(q2);				//Q2
		handle.writeLong(q3);				//Q3
		handle.writeLong(q4);				//Q4
		handle.writeLong(worldDataEnd);		//World Data ends
		handle.writeByte((byte)(1000001)); //End Pattern
	}

	public static String[] listFiles()
	{
		String directory = "./";
		File folder = new File(directory);
		return folder.list();
	}
	public static boolean loadFile(String url)
	{
		//Returns true if the file exists or false if a new one was created
		data = new File(url + ".mine");

		boolean exists;

		exists = data.isFile();

		try{
			handle = new RandomAccessFile(data,"rw");
			fileLength = handle.length();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException f)
		{
			f.printStackTrace();
		}

		if (exists == false)
		{
			try
			{
				fileLength = 90 + 34 + 6;
				headerLength = 90;
				seedLocation = 90;
				charDataStart = 94;
				charDataEnd = 128;
				worldDataStart = 129;
				q1 = 130;
				q2 = 131;
				q3 = 132;
				q4 = 133;
				worldDataEnd = 134;

				writeHeader();
				genFiller();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		else {
			try {
				long[] hVal = getHeader(handle);
				headerLength = hVal[2];
				seedLocation = hVal[3];
				charDataStart = hVal[4];
				charDataEnd = hVal[5];
				worldDataStart = hVal[6];
				q1 = hVal[7];
				q2 = hVal[8];
				q3 = hVal[9];
				q4 = hVal[10];
				worldDataEnd = hVal[11];
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return exists;
	}

	public static void createTempFile()
	{
		tempFile = new File("temp.unmine");
		try
		{
			tempHandle = new RandomAccessFile(tempFile,"rw");
		} catch (IOException e)
		{
			e.printStackTrace();
		}


	}

	public static void genFiller() throws IOException
	{
		handle.seek(90);
		handle.writeInt(0); //Writes Seed
		handle.writeByte((byte)(1));	//Character Data Header
		handle.writeInt(0);	//X,y,z
		handle.writeInt(0);
		handle.writeInt(0);
		handle.writeFloat(0); //x,y,z angles
		handle.writeFloat(0);
		handle.writeFloat(0);
		handle.writeInt(100); //Health
		handle.writeInt(1); //Block holding value (Dirt Block)
		handle.writeByte((byte)(1));	//Character Data Footer
		handle.writeByte((byte)(4));	//World Data Header
		handle.writeByte((byte)(1));	//Q1
		handle.writeByte((byte)(1));	//Q2
		handle.writeByte((byte)(1));	//Q3
		handle.writeByte((byte)(1));	//Q4
		handle.writeByte((byte)(4));	//World Data Footer
	}
	public static void writeSeed(int seed)
	{
		try
		{
			handle.seek(seedLocation);
			handle.writeInt(seed);
		}catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void cleanUp()
	{
		data = null;
		tempFile.deleteOnExit();
		tempFile = null;
		try {
			handle.close();
			tempHandle.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void test()
	{
		ArrayList <int[]> temp = readWorldData(1,0,0,0);
		for (int i = 0; i < temp.size(); i++)
			System.out.println(temp.get(i)[0] + "  " + temp.get(i)[1] + "  " + temp.get(i)[2] + "  " + temp.get(i)[3] + "  " + temp.get(i)[4]);
		
		System.out.println("----------------------");
			try {
			handle.seek(q1 + 1);
		for (int i = 0; i < (q2 - (q1 + 1)) / 4; i++)
			
				System.out.println("1  " + handle.readInt() + "  " + handle.readInt() + "  " + handle.readInt() + "  " + handle.readInt());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
