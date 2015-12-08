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
	private org.newdawn.slick.Color currentColour = org.newdawn.slick.Color.green;

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

		lineHeight = font2.getLineHeight()+2;	// 2 pixel line separation
	}

	public int getHeight() {
		return lineHeight * windowRows;
	}

	public void setTextColour(org.newdawn.slick.Color clr) {
		currentColour = clr;
	}

	public void writeLine(int row, String line) {
		if (row > windowRows - 1) return;
		if (row < 0) return;

		textRow[row] = line;
	}

	public void write( String line ) {
		if (textRow[0] == null) textRow[0] = "";
		textRow[0] += line.trim();
	}

	public void scroll() {
		for (int row = windowRows-1; row > 0; row--) {
			if (textRow[row-1] == null || textRow[row-1].trim().isEmpty()) continue;

			textRow[row] = textRow[row - 1].trim();
		}
		textRow[0] = "";
	}

	public void draw() {
		glPushMatrix();
		glLoadIdentity();
		glEnable(GL_BLEND);
		glDisable(GL_DEPTH_TEST);

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (int row = 0; row < windowRows; row++) {
			if ( textRow[row] != null && !textRow[row].isEmpty()) font2.drawString(bottomLeftX, bottomLeftY - ( (row+1) * lineHeight), textRow[row], org.newdawn.slick.Color.green);
		}

		org.newdawn.slick.Color.white.bind();
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		glPopMatrix();
	}
}
