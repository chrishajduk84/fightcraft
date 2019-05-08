/**
 * @author Chris Hajduk
 *
 * Created May 8, 2012
 */
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

import java.io.IOException;
import java.io.InputStream;
import java.lang.RuntimeException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.awt.Color;
import java.awt.Font;
public class textures {

	//public static Texture[][] blocks = new Texture[9][6]; ///Each block has 6 sides for 100+ textures
	//One texture for all the blocks
	public static Texture blocks;
	public static Texture[] bitmap2D = new Texture[1];
	public static String[] blockName = new String[9];
	
	///Font Variables
	public static UnicodeFont menuText;

	public static int texBuffer = GL13.GL_TEXTURE0;
	public static IntBuffer id;
	
	public static void loadTextures(String file, int uniform)
	{
		try
		{
		InputStream in = ResourceLoader.getResourceAsStream("textures/" + file + ".png");
		PNGDecoder decoder = new PNGDecoder(in);
		System.out.println("Texture loaded: "+decoder.toString()); 
		System.out.println(">> Image width: "+decoder.getWidth()); 
		System.out.println(">> Image height: "+decoder.getHeight()); 
		if (file == "textures")
			System.out.println(">> " + decoder.getWidth() / 16 + " block types.");
		
		//4 Bytes per pixel * area of picture = Total # of bytes
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
		//(Buffer,Width of each line of pixels,Format)
		decoder.decode(buf,decoder.getWidth() * 4, Format.RGBA);
		
		//Reset Marker and position
		buf.rewind();
		///////////////////////////////////////////
		// Attach pixel data to the OpenGL buffers
		///////////////////////////////////////////
		//Get texture memory buffer pointer
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL13.glActiveTexture(texBuffer);

		id = BufferUtils.createIntBuffer(1);
		GL11.glGenTextures(id);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id.get(0));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT,4);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		//Unpacks the data in groups of 4 bytes
		
		//Associate the texture with the shader variable
		//id.rewind();
		GL13.glActiveTexture(texBuffer);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id.get(0));
		GL20.glUniform1i(uniform,texBuffer - GL13.GL_TEXTURE0);
		texBuffer++;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void loadTextures()
	{
		blockName[0] = "air";
		blockName[1] = "dirtBottom";
		blockName[2] = "grassSide";
		blockName[3] = "grassTop";

		//Naming convention:
		//blockname-[side].png
		//Where [side]:
		//0 = Front
		//1 = Left
		//2 = Back
		//3 = Right
		//4 = Top
		//5 = Bottom
//		int count = 0;
//		for (String name : blockName)
//		{
//			for (int i = 0; i < 6; i++)
//			{
//				try {
//					blocks[count][i] = TextureLoader.getTexture("PNG",ResourceLoader.getResourceAsStream("textures/" + name + "-" + i + ".png"));
//					System.out.println("Texture loaded: "+blocks[count][i]); 
//					System.out.println(">> Image width: "+blocks[count][i].getImageWidth()); 
//				} catch (IOException e) {
//					System.out.println("IO Error: Texture NOT loaded: "+blocks[count][i]); 
//					e.printStackTrace();
//				}
//				catch (RuntimeException f)
//				{
//					System.out.println("Texture NOT loaded: "+blocks[count][i]); 
//					f.printStackTrace();
//				}
//
//			}
//			count++;
//		}
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		///////////////////////////////////////
		//Block Textures
		///////////////////////////////////////
		try {
			blocks = TextureLoader.getTexture("PNG",ResourceLoader.getResourceAsStream("textures/" + "textures.png"));
			System.out.println("Texture loaded: "+blocks); 
			System.out.println(">> Image width: "+blocks.getImageWidth()); 
			System.out.println(">> " + blocks.getImageWidth() / 16 + " block types.");
		} catch (IOException e) {
			System.out.println("IO Error: Texture NOT loaded: "+blocks); 
			e.printStackTrace();
		}
		catch (RuntimeException f)
		{
			System.out.println("Texture NOT loaded: "+blocks); 
			f.printStackTrace();
		}
		
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, blocks.getTextureID());
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL20.glUniform1i(updateGraphics.cubeUniform[3],3);

		////////////////////////////////////
		//2D textures
		////////////////////////////////////
		try {
			bitmap2D[0] = TextureLoader.getTexture("PNG",ResourceLoader.getResourceAsStream("textures/" + "crosshair.png"));
			System.out.println("Texture loaded: "+bitmap2D[0]); 
			System.out.println(">> Image width: "+bitmap2D[0].getImageWidth()); 
		} catch (IOException e) {
			System.out.println("IO Error: Texture NOT loaded: "+bitmap2D[0]); 
			e.printStackTrace();
		}
		catch (RuntimeException f)
		{
			System.out.println("Texture NOT loaded: "+bitmap2D[0]); 
			f.printStackTrace();
		}
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, bitmap2D[0].getTextureID());
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL20.glUniform1i(updateGraphics.barsUniform[0],2);
	}
	
	public static void loadFonts()
	{
		GL13.glActiveTexture(texBuffer);
		Font awtfont = new Font("Times New Roman",Font.BOLD,100);
		menuText = new UnicodeFont(awtfont,100,false,false);
		menuText.addAsciiGlyphs();
		menuText.addGlyphs(400, 600);
		menuText.getEffects().add(new ColorEffect(Color.white));

		
		try
		{
			menuText.loadGlyphs();
			
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
		
	}
}
