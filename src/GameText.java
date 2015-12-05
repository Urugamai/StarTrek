import org.newdawn.slick.*;
import org.newdawn.slick.font.effects.ColorEffect;

import java.awt.Font;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Created by Mark on 5/12/2015.
 */
public class GameText {

	private TrueTypeFont 		font;
	private UnicodeFont 		font2;
	private static boolean		antiAlias 			= true;
	private Font 				awtFont;
	private String 				textRow[];
	private int 				windowRows;
	private int 				bottomLeftX, bottomLeftY;
	private int 				lineHeight;

	protected GameText(int blockX, int blockY, int lines) {
		// Setup FONT stuff
		awtFont = new Font("Courier New", Font.PLAIN, 14);
		font = new TrueTypeFont(awtFont, antiAlias);
		font2 = new UnicodeFont(awtFont);
		font2.getEffects().add(new ColorEffect(java.awt.Color.white));
		font2.addAsciiGlyphs();
		try { font2.loadGlyphs(); }
		catch (SlickException e) {
			e.printStackTrace();
		}

		textRow = new String[lines];
		windowRows = lines;
		bottomLeftX = blockX;
		bottomLeftY = blockY;

		lineHeight = font2.getLineHeight();
	}

	/*
	 * writeLn - add line to screen, scroll all others up
	 */
	public void writeLn( String line, org.newdawn.slick.Color clr) {
		for (int i = windowRows-1; i > 0; i--) {
			textRow[i] = textRow[i - 1];
		}
		textRow[0] = line;
	}

	public void write( String line ) {
		if (textRow[0] == null) textRow[0] = "";
		textRow[0] += line;
	}

	public void draw() {
		glPushMatrix();
		glLoadIdentity();
		glEnable(GL_BLEND);
		glDisable(GL_DEPTH_TEST);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		//char key = Keyboard.getEventCharacter();
//		font2.drawString(10, height - heightTextArea + 10, "THE LIGHTWEIGHT JAVA GAMES LIBRARY", org.newdawn.slick.Color.green);
		for (int row = 0; row < windowRows; row++) {
			if ( textRow[row] != null) font2.drawString(bottomLeftX, bottomLeftY - ( (row+1) * lineHeight), textRow[row], org.newdawn.slick.Color.green);
		}

		org.newdawn.slick.Color.white.bind();
		glPopMatrix();
		glEnable(GL_DEPTH_TEST);
	}
}
