/**
 * @author Chris Hajduk
 *
 * Started May 1, 2012
 */

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.LWJGLException;

public class startGame {
	static int screenWidth;
	static int screenHeight;
	static boolean mainMenu = true;
	static boolean playing = false;

	public static void main(String args[]) throws LWJGLException
	{
		Screen game = new Screen();
		game.startWindow("FightCraft", 640, 480, 16, 60, false);
		updateGraphics.initializeGraphics();
		boolean fullscreen = false;

		while(!Display.isCloseRequested() && !game.closing)
		{
			///////////////////////////////////////////////////////////
			//// Input
			//////////////////////////////////////////////////////////

			if (updateInput.getKeyboard()[4] == true)
			{
				fullscreen = !fullscreen;
				game.setDisplayMode(640, 480, 32, 60, fullscreen);
				game.resizing = true;
			}
			if (game.resizing)
			{
				game.resizing = false;
				updateGraphics.resizeGraphics(Display.getWidth(), Display.getHeight());
				screenWidth = Display.getWidth();
				screenHeight = Display.getHeight();

			}

			if (mainMenu)
			{
				updateGraphics.renderMainMenu();
			}
			else if (playing)
			{
				////////////////////////////////////////////////////////////
				//// Logic
				///////////////////////////////////////////////////////////

				////////////////////////////////////////////////////////////
				//// Update World
				///////////////////////////////////////////////////////////
				updateGraphics.drawWorld();
				updateGraphics.calcFPS();


				game.parentWin.setTitle("Xangle: " + String.valueOf(updateGraphics.angleX) + " " + "Yangle: " + String.valueOf(updateGraphics.angleY) + " X:" + String.valueOf(updateGraphics.xpos) + " Y:" + String.valueOf(updateGraphics.ypos) + " Z:" + String.valueOf(updateGraphics.zpos) +  " FPS: " + updateGraphics.fpsout + " - FIGHTCRAFT");
			}
			Display.update();

		}
		//Destroy Threads
		readwriteData.stopThread = true;
		for (int i = 0; i < 4; i++)
		{
			updateGraphics.quadrants[i].stopInitializationThread();
			while (updateGraphics.q[i].isAlive());		//Waits till the thread is done
			updateGraphics.quadrants[i] = null;
			updateGraphics.q[i] = null;
			
		}
		while (updateGraphics.tFile.isAlive());   		//Waits till the thread is done
		updateGraphics.tFile = null;
		
		
		///Closes the game and saves position
		readwriteData.writeCharacterData((int)updateGraphics.xpos, (int)updateGraphics.ypos, (int)updateGraphics.zpos, updateGraphics.angleX, updateGraphics.angleY, updateGraphics.angleZ, 100, playerStatus.selectedBlock);
		readwriteData.saveNsort();
		readwriteData.test();
		readwriteData.cleanUp();
		Display.destroy();
		game.parentWin.dispose();
		game = null;
		System.exit(0);


	}
}
