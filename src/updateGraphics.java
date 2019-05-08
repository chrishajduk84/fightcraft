/**
 * @author Chris Hajduk
 *
 * Started May 3rd, 2012
 */

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.Color;

public class updateGraphics {
	//Trig constant
	static float pi180 = (float)Math.PI/180.0f;

	//Current Position
	static float xpos = 0;
	static float ypos = 16 * 9;
	static float zpos = 0;
	static float angleX = 0;
	static float angleY = 0;
	static float angleZ = 0;

	//Last position
	static float xposLast = 0;
	static float yposLast = 0;
	static float zposLast = 0;
	static float angleXLast = 0;
	static float angleYLast = 0;

	//Direction facing: 0 = +Z, 1 = +X, 2 = -Z, 3 = -X
	static int direction = 0;

	static float mouseSensitivity = 10f;
	static float movementFactor = 0.01f;

	static superSector[] quadrants = new superSector[4];
	static Thread q[];
	static Thread tFile;

	static perlinNoise mapHeight = new perlinNoise();
	static int seed;

	static shaderProgram cube;
	static int[] cubeAttrib = new int[1];
	static int[] cubeUniform = new int[4];

	static shaderProgram bars;
	static int[] barsAttrib = new int[1];
	static int[] barsUniform = new int[4];

	static shaderProgram mainMenu;
	static int[] mainmenuAttrib = new int[1];
	static int[] mainmenuUniform = new int[4];

	static float pov = 45f;//45

	public static void initializeGraphics()
	{		
		//readwriteData.listFiles();
		//Get OpenGL version
		String GLVersion = GL11.glGetString(GL11.GL_VERSION);
		String GLSLVersion = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("OpenGL Version: " + GLVersion);
		System.out.println("GLSL Version: " + GLSLVersion);

		//Load Save File from File System
		if (!readwriteData.loadFile("world")) //If the File Exists return true
		{

			//Initialize Map Seeding and generation
			mapHeight.generateRandomSeed();
			seed = mapHeight.seed;
			readwriteData.writeSeed(seed);
		}
		else
		{
			seed = readwriteData.extractSeed();
			float[] pos = readwriteData.readCharacterData();
						xpos = pos[0];
						ypos = pos[1];
						zpos = pos[2];
						angleX = pos[3];
						angleY = pos[4];
						angleZ = pos[5];
						playerStatus.selectedBlock = (int)pos[7];
			mapHeight.seed = seed;
		}
		readwriteData.createTempFile();

		//////////////////////////
		////Enable Shader Programs
		//////////////////////////
		cube = new shaderProgram();
		cube.attachShader("cube");
		GL20.glUseProgram(cube.glslProg);
		//Assign variables
		cubeAttrib[0] = cube.getAttribute("coord4d");
		cubeUniform[0] = cube.getUniform("pos");
		cubeUniform[1] = cube.getUniform("rot");
		cubeUniform[2] = cube.getUniform("secTrans");
		cubeUniform[3] = cube.getUniform("texture");

		bars = new shaderProgram();
		bars.attachShader("bars");
		GL20.glUseProgram(bars.glslProg);
		barsAttrib[0] = bars.getAttribute("coord2d");
		barsUniform[0] = bars.getUniform("texture");
		barsUniform[1] = bars.getUniform("block");


		mainMenu = new shaderProgram();
		mainMenu.attachShader("mainmenu");
		GL20.glUseProgram(mainMenu.glslProg);
		mainmenuUniform[0] = mainMenu.getUniform("picture");
		mainmenuAttrib[0] = mainMenu.getAttribute("coord2d");



		//Initialize Textures
		GL20.glUseProgram(cube.glslProg);
		textures.loadTextures("textures",cubeUniform[3]);
		GL20.glUseProgram(bars.glslProg);
		textures.loadTextures("ScreenTemplate",barsUniform[0]);
		textures.loadTextures("textures",barsUniform[1]);
		GL20.glUseProgram(mainMenu.glslProg);
		textures.loadTextures("MainMenu",mainmenuUniform[0]);


		///////////////////////////////////
		//Enable Misc. Features/Switches
		//////////////////////////////////

		//Clear Depth,Color,etc.
		GL11.glShadeModel(GL11.GL_SMOOTH);                            //Enables Smooth Color Shading
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               //This Will Clear The Background Color To Black
		GL11.glClearDepth(1.0);                                  //Enables Clearing Of The Depth Buffer
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);  // Really Nice Perspective Calculations

		GL11.glEnable(GL11.GL_TEXTURE_2D);				//Enable Textures
		GL11.glEnable(GL11.GL_BLEND);					//Blending Mode
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);		//Allow Transparency in Textures

		//Setup the 4 quadrants
		q = new Thread[4];
		for (int i = 0; i < 4; i++)
		{
			quadrants[i] = new superSector();
			quadrants[i].quadrant = i + 1;
			//quadrants[i].initialize((int)xpos,(int)ypos,(int)zpos);
			q[i] = new Thread(quadrants[i],"Terrain Generation " + i);
			q[i].setPriority(Thread.MIN_PRIORITY);
			q[i].start();
		}

		tFile = new Thread(new readwriteData(), "File IO");
		tFile.start();

	}
	public static void resizeGraphics(int width, int height)
	{
		///Resets the Viewport
		GL11.glViewport(0,0,width,height);
		//Loads the matrix into projection mode
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(pov, (float)width/(float)height, 0.01f, 1000.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	public static void translateMouse(int[] mouseEvent,int deltaTime)
	{
		//Last angle is recorded for graphics purposes
		angleXLast = angleX;
		angleYLast = angleY;

		//Translate half the screen width and height
		int movementVert = mouseEvent[6];
		int movementHoriz = mouseEvent[5];

		angleX += movementHoriz / mouseSensitivity;
		angleY += movementVert / mouseSensitivity;


		//Keep Horizontal angles between 0 and 360
		if (angleX > 360)
			angleX -= 360;
		if (angleX < 0)
			angleX += 360;
		//Keep Horizontal angles between 90 and -90
		if (angleY > 90)
			angleY = 90;
		if (angleY < -90)
			angleY = -90;
		
		//Determine direction facing
		if (angleX >= 315 || angleX < 45)
			direction = 0;
		else if (angleX >= 45 && angleX < 135)
			direction = 1;
		else if (angleX >= 135 && angleX < 225)
			direction = 2;
		else if (angleX >= 225 && angleX < 315)
			direction = 2;
		

		//Selection of blocks for each tick of the scroll wheel
		if (mouseEvent[7] > 0)
			playerStatus.selectedBlock++;
		if (mouseEvent[7] < 0)
			playerStatus.selectedBlock--;
		if (playerStatus.selectedBlock < 0)
			playerStatus.selectedBlock *= -1;
		if (playerStatus.selectedBlock > 16)
			playerStatus.selectedBlock -= 16;
		if (playerStatus.selectedBlock == 0)
			playerStatus.selectedBlock = 1;


	}
	public static void translateKeyboard(boolean[] keyEvent, int deltaTime)
	{


		zposLast = zpos;
		xposLast = xpos;
		yposLast = ypos;
		//Syncs any lag with movement


		if (keyEvent[0] == true) ///W key is pressed
		{
			zpos -= movementFactor  * deltaTime * (float)Math.cos(pi180 * (-angleX)); //Calculates Trig based on angle of rotation
			xpos -= movementFactor * deltaTime * (float)Math.sin(pi180 * (-angleX));
		}
		if (keyEvent[1] == true) //S key is pressed
		{
			zpos += movementFactor * deltaTime * (float)Math.cos(pi180 * (-angleX));
			xpos += movementFactor * deltaTime * (float)Math.sin(pi180 * (-angleX));
		}
		if (keyEvent[2] == true) //A key is pressed
		{
			xpos -= movementFactor * deltaTime * (float)Math.cos(pi180 * (angleX));
			zpos -= movementFactor * deltaTime * (float)Math.sin(pi180 * (angleX));
		}
		if (keyEvent[3] == true) //D key is pressed
		{
			xpos += movementFactor * deltaTime * (float)Math.cos(pi180 * (angleX));
			zpos += movementFactor * deltaTime * (float)Math.sin(pi180 * (angleX));
		}
		if (keyEvent[5] == true) //Space key is pressed
		{
			ypos += movementFactor * deltaTime;
		}
		if (keyEvent[6] == true) //Space key is pressed
		{
			ypos -= movementFactor * deltaTime;
		}

	}

	public static void drawWorld()
	{
		//Use the 3D Cube Shader
		GL20.glUseProgram(cube.glslProg);

		//Controls
		int deltaTime = getDeltaTime();
		int mouseAction[] = updateInput.getMouse();
		translateKeyboard(updateInput.getKeyboard(), deltaTime);
		translateMouse(mouseAction, deltaTime);

		//Translation

		//Vector in the X Multiplied by the projection of angleY on the X axis
		Vector3f cameraDirection = new Vector3f((float)Math.sin(angleX * pi180) * (float)Math.cos(pi180 * (angleY + 15)), (float)Math.sin(pi180 * (angleY + 15)), -(float)Math.cos(angleX * pi180) * (float)Math.cos(pi180 * (angleY + 15)));
		Vector3f cameraPosition = new Vector3f(xpos,ypos,zpos);
		Vector3f cameraMovement = new Vector3f(xpos - xposLast, ypos - yposLast, zpos - zposLast);
		//Checks for a collision and if so resets the last position values
		if (!unProject.collisionCheck(cameraMovement, cameraPosition))
		{
			//angleX = angleXLast;
			//angleY = angleYLast;
			xpos = xposLast;
			ypos = yposLast;
			zpos = zposLast;
		}

		float xangleTrans = angleX * pi180;
		float yangleTrans = angleY * pi180 * (float)Math.cos(pi180 * angleX);
		float zangleTrans = -angleY * pi180 * (float)Math.sin(pi180 * angleX);

		float angle[] = {xangleTrans,yangleTrans,zangleTrans};
		float trans[] = {-xpos,-ypos,-zpos};


		GL20.glUniform3(cubeUniform[0], buffer.makeFloatBuffer(trans));
		GL20.glUniform3(cubeUniform[1], buffer.makeFloatBuffer(angle));

		GL11.glLoadIdentity(); //Resets all matrix calculations (So it isn't multiplied exponentially)
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.53f, 0.80f, 0.98f, 0.0f);




		//Assign variables
		for (int i = 0; i < 4; i++)
			quadrants[i].render();

		//GL11.glPopMatrix();

		//Use the 2D status Bar shader
		GL20.glUseProgram(bars.glslProg);
		renderBars();
		if (mouseAction[8] == 1)
		{
			unProject.rayCasting(cameraDirection,cameraPosition,1);

		}
		else if (mouseAction[9] == 1)
		{
			unProject.rayCasting(cameraDirection,cameraPosition,2);

		}
	}

	public static void renderBars()
	{

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		////////////////////////////////////////////
		//Setup 2D Drawing Mode
		///////////////////////////////////////////
		float aspect = 0;
		try
		{
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			if (startGame.screenHeight != 0)
				aspect = startGame.screenWidth / startGame.screenHeight;
			GL11.glOrtho(-aspect, aspect, -1, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
		}
		catch (ArithmeticException e)
		{
			e.printStackTrace();
		}

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		/////////////
		//Screen Template
		/////////////
		//0 indicates the screen template
		float vertex[] = {
				-1f,-1f, 0,
				1f,1f, 0,
				-1f,1f, 0,
				-1f,-1f, 0,
				1f,-1f, 0,
				1f,1f,0		
		};
		FloatBuffer vbo = buffer.makeFloatBuffer(vertex);
		int id = buffer.getBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		vbo = null;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,id);
		GL20.glVertexAttribPointer(
				barsAttrib[0],
				3,
				GL11.GL_FLOAT,
				false,
				0,
				0				
				);
		GL20.glEnableVertexAttribArray(barsAttrib[0]);

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6); //6 points to make a square
		GL20.glDisableVertexAttribArray(barsAttrib[0]);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		/////////////
		//Selected Block
		/////////////
		//1 indicates the Selected Block
		float b = playerStatus.selectedBlock;
		float vertex2[] = {
				0.675f,-0.575f, b,
				aspect,-aspect, b,
				0.675f,-aspect, b,
				0.675f,-0.575f, b,
				aspect,-0.575f, b,
				aspect,-aspect,b		
		};
		FloatBuffer vbo2 = buffer.makeFloatBuffer(vertex2);
		int id2 = buffer.getBuffer(GL15.GL_ARRAY_BUFFER, vbo2);
		vbo2 = null;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,id2);
		GL20.glVertexAttribPointer(
				barsAttrib[0],
				3,
				GL11.GL_FLOAT,
				false,
				0,
				0				
				);
		GL20.glEnableVertexAttribArray(barsAttrib[0]);

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6); //6 points to make a square
		GL20.glDisableVertexAttribArray(barsAttrib[0]);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();

	}

	public static void renderMainMenu()
	{
		////////////////////////////////////////////
		//Setup 2D Drawing Mode
		///////////////////////////////////////////
		if (updateInput.getKeyboard()[7] == true)
		{
			startGame.mainMenu = false;
			startGame.playing = true;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

		try
		{
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			float aspect = startGame.screenWidth / startGame.screenHeight;
			GL11.glOrtho(-aspect, aspect, -1, 1, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

		}
		catch (ArithmeticException e)
		{
			e.printStackTrace();
		}
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_COLOR_BUFFER_BIT);
		GL11.glClearColor(0.53f, 0.80f, 0.98f, 0.0f);
		GL20.glUseProgram(mainMenu.glslProg);

		/////////////
		//Display Area Quad
		/////////////
		float vertex[] = {
				-1f,-1f,
				1f,1f,
				-1f,1f,
				-1f,-1f,
				1f,-1f,
				1f,1f		
		};
		FloatBuffer vbo = buffer.makeFloatBuffer(vertex);
		int id = buffer.getBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		vbo = null;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,id);
		GL20.glVertexAttribPointer(
				mainmenuAttrib[0],
				2,
				GL11.GL_FLOAT,
				false,
				0,
				0				
				);
		GL20.glEnableVertexAttribArray(barsAttrib[0]);

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6); //6 points to make a square
		GL20.glDisableVertexAttribArray(mainmenuAttrib[0]);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);


		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();

	}


	//Game Time Functions
	public static long getTime()
	{
		long time;
		time = Sys.getTime() * 1000 / Sys.getTimerResolution(); //Gets Time in Milliseconds
		return time;
	}


	static long lasttime = 0;
	public static int getDeltaTime()
	{
		//Gets the change in time
		long time = getTime();
		int delta;

		delta = (int)(time - lasttime);

		//Sets time to a storage variable
		lasttime = time;
		return delta;
	}

	static long lastFrame = getTime();
	static int fps = 0;
	static int fpsout = 0;
	static int deltaFps = 0;
	static int lastFps = 0;
	public static void calcFPS()
	{
		//Check if time is greater than 1 second and increment the counter for each time it is not
		if (getTime() - lastFrame >= 1000)
		{
			fpsout = fps;
			fps = 0;
			lastFrame += 1000;
			
			//Calculate delta fps
			deltaFps = fpsout - lastFps;
			lastFps = fpsout;
		}
		fps++;

	}

}
