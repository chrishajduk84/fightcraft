/**
 * @author Chris Hajduk
 *
 * Started May 4th, 2012
 */
import java.awt.BufferCapabilities;
import java.nio.Buffer;

import org.lwjgl.BufferChecks;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Cursor;
public class updateInput {

	static boolean[] keyPressed = new boolean[10]; //0 = W, 1 = S, 2 = A, 3 = D, 4 = F11, 5 = [space]
	static int[] mouseStatus = new int[10]; // 0 = MouseX, 1 = MouseY, 2 = Left Down (0/1), 3 = Right Down (0/1), 4 = Scroll Wheel, 5 = dx, 6 = dy, 7 = dwheel, 8 = Left Click, 9 = Right Click
	static boolean focused = false;
	static Cursor currentCursor;
	static Cursor originalCursor = Mouse.getNativeCursor();
	
	static int LeftDownLast = 0;
	static int RightDownLast = 0;

	public static int[] getMouse()
	{

		if (focused == true)
		{	
			//Update the cursor position
			//Mouse.updateCursor();
			Mouse.setCursorPosition(startGame.screenWidth/2,startGame.screenHeight/2);
			//Mouse.poll();
			mouseStatus[0] = Mouse.getX();
			mouseStatus[1] = Mouse.getY();
			mouseStatus[2] = Mouse.isButtonDown(0)?1:0; //Button down = 1 Button Up = 0
			mouseStatus[3] = Mouse.isButtonDown(1)?1:0;
			mouseStatus[4] = Mouse.isButtonDown(2)?1:0;
			mouseStatus[5] = Mouse.getDX();
			mouseStatus[6] = Mouse.getDY();
			mouseStatus[7] = Mouse.getDWheel();
			mouseStatus[8] = mouseStatus[2] == 1 && LeftDownLast == 0?1:0; //If it is down, but was not on the last clock cycle return true.
			mouseStatus[9] = mouseStatus[3] == 1 && RightDownLast == 0?1:0;
			
			LeftDownLast = mouseStatus[2];
			RightDownLast = mouseStatus[3];
			

			//Then retrieve the next set of values
			//So that the previous change in Mouse position does not counteract above variables
			Mouse.poll();
			
			//Sets the appropriate cursor (visible vs. not visible)
			try {
				if (currentCursor == null)
					currentCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
				Mouse.setNativeCursor(currentCursor);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}

		}
		else
		{
			try {
				Mouse.setNativeCursor(originalCursor);
			} catch (LWJGLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		
		return mouseStatus;
	}
	public static boolean[] getKeyboard()
	{


		while (Keyboard.next())
		{
			if (Keyboard.getEventKeyState()) ///If key was pressed
			{
				if (Keyboard.getEventKey() == Keyboard.KEY_W)
				{
					keyPressed[0] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S)
				{
					keyPressed[1] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_A)
				{
					keyPressed[2] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D)
				{
					keyPressed[3] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE)
				{
					keyPressed[5] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT)
				{
					keyPressed[6] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN)
				{
					keyPressed[7] = true;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
				{
					focused = !focused;
				}

			}
			else //If Key was released 
			{
				if (Keyboard.getEventKey() == Keyboard.KEY_W)
				{
					keyPressed[0] = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_S)
				{
					keyPressed[1] = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_A)
				{
					keyPressed[2] = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_D)
				{
					keyPressed[3] = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_SPACE)
				{
					keyPressed[5] = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT)
				{
					keyPressed[6] = false;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN)
				{
					keyPressed[7] = false;
				}
			}
		}
		///For the F11 Key Press (fullscreen)
		if (Keyboard.isKeyDown(Keyboard.KEY_F11) == true)
		{
			keyPressed[4] = true;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_F11) == false)
		{
			keyPressed[4] = false;
		}

		return keyPressed;
	}

}
