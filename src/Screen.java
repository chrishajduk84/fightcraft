/**
 * @author Chris Hajduk 
 * Started May 1, 2012
 * 
 */
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.LWJGLException;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

public class Screen {

	//Initialize the frame/Window
	Frame parentWin = new Frame("Test");
	//Create the Canvas area
	Canvas winArea = new Canvas();
	//Closing Flag - Tells all processes to end
	boolean closing, resizing;
	int winWidth, winHeight;

	public void startWindow(String title, int width, int height, int bits, int frequency, boolean fullscreen)
	{

		//Set Window Event Listeners and window properties
		winArea.setSize(width, height); //Prevent that Invalid Drawable error

		parentWin.setSize(width,height);
		parentWin.setTitle(title);
		parentWin.setVisible(true);
		parentWin.add(winArea);
		parentWin.addWindowListener(winClose);
		parentWin.addComponentListener(winResize);

		winWidth = width;
		winHeight = height;
		
		//CREATE THE WINDOW
		try
		{
			Display.setParent(winArea);
			setDisplayMode(width,height,bits,frequency,fullscreen); //Get Valid Display Modes
			Display.create();
		}
		catch (LWJGLException e)
		{ 
			e.printStackTrace();
		}
	}

	public void setDisplayMode(int width, int height, int bits, int frequency, boolean fullscreen) throws LWJGLException
	{

		//Check if the display mode needs to be changed
		if (Display.getDisplayMode().getHeight() == height && Display.getDisplayMode().getWidth() == width && Display.isFullscreen() == fullscreen)
		{
			return;
		}



		DisplayMode selected = null;

		if (fullscreen)
		{
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			int freq = 0;
			int biggestHeight = 0;
			int biggestWidth = 0;
			
			for (int i = 0; i < modes.length; i++)
			{
				DisplayMode current = modes[i];
				///Check for the closest match to the appropriate resolution etc.


				if (current.getWidth() >= width && current.getHeight() >= height && current.getWidth() >= biggestWidth && current.getHeight() >= biggestHeight)
				{
					if (selected == null || current.getFrequency() >= freq)
					{
						if (selected == null || current.getBitsPerPixel() >= selected.getBitsPerPixel())
						{
							freq = current.getFrequency();
							biggestWidth = current.getWidth();
							biggestHeight = current.getHeight();
							selected = current;
						}
					}
					///If it matches the desktop display mode, use that. It will be more likely to work.
					if (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency() && current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
					{
						selected = current;
						biggestWidth = current.getWidth();
						biggestHeight = current.getHeight();
						break;
					}
				}
			}
		}
		else
		{
			selected = new DisplayMode(width,height);
			Display.setParent(winArea);
			
		}
		if (selected == null)
		{
			System.out.println("Error. Could not find display mode. Width:" + width + " Height: " + height + " Freq: " + frequency + " BPP: " + bits + " Fullscreen: " + fullscreen);
			return;
		}

		try
		{
		Display.setDisplayMode(selected);
		Display.setFullscreen(fullscreen);
		}
		catch (LWJGLException e)
		{
			System.out.println("Error. Could not create the display.");
			e.printStackTrace();
		}
	}



	//////////////////////////////////////////////////////////////////////
	///ALL THE WINDOW EVENT LISTENERS
	//////////////////////////////////////////////////////////////////////


	WindowAdapter winClose = new WindowAdapter()
	{
		public void windowClosing(WindowEvent e)
		{
			closing = true;
		}


	};
	ComponentAdapter winResize = new ComponentAdapter()
	{
		public void componentResized(ComponentEvent e)
		{
			winArea.setSize(parentWin.getWidth(),parentWin.getHeight());
			resizing = true;
			winWidth = parentWin.getWidth();
			winHeight = parentWin.getHeight();
			
			//updateGraphics.resizeGraphics(e.getComponent().getWidth(), e.getComponent().getHeight());
			
			///GLU.gluPerspective(180,2,1,100);///Change Perspective (Field of view,aspect ratio,znear, zback)
//					try
//						{	
//							Display.setDisplayConfiguration(2, 20, 12);
//							Display.releaseContext();
//							
//						}
//						catch (LWJGLException i)
//						{
//							i.printStackTrace();
//						}
		}

	};


}
