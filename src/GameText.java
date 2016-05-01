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

	public boolean 				bufferChanged = true;
	private TrueTypeFont 		font;
	private UnicodeFont 		font2;
	private static boolean		antiAlias 			= true;
	private Font 				awtFont;
	private String 				textRow[];

	private int 				windowRows;
	private int 				txtXMin;
	private int 				txtXMax;
	private int 				txtYMin;
	private int 				txtYMax;
	private int 				lineHeight, charWidth;
	private org.newdawn.slick.Color currentColour = org.newdawn.slick.Color.green;

	protected GameText(int txtXMin, int txtXMax, int txtYMin, int txtYMax) {
		// Setup FONT stuff
		awtFont = new Font(Constants.txtFont, Constants.txtStyle, Constants.txtSize);
		font = new TrueTypeFont(awtFont, antiAlias);
		font2 = new UnicodeFont(awtFont);
		font2.getEffects().add(new ColorEffect(java.awt.Color.white));
		font2.addAsciiGlyphs();
		try { font2.loadGlyphs(); }	catch (SlickException e) { e.printStackTrace(); }

		lineHeight = font2.getLineHeight()+2;	// 2 pixel line separation
		charWidth = font2.getSpaceWidth();
		windowRows = (int)Math.floor((txtYMax - txtYMin) / lineHeight);
		this.txtXMin = txtXMin;
		this.txtXMax = txtXMax;
		this.txtYMin = txtYMin;
		this.txtYMax = txtYMax;

		textRow = new String[Constants.textBufferSize];
	}

	public int getHeight() {
		return lineHeight * windowRows;
	}
	public int getLineHeight() { return lineHeight; }

	public void setTextColour(org.newdawn.slick.Color clr) {
		currentColour = clr;
	}

	public void writeAt(int row, String line) {
		assert(row < Constants.textBufferSize);
		assert(row >= 0);

		textRow[row] = line;
		bufferChanged = true;
	}

	public void writeLn(String line) {
		if (textRow[0] == null) textRow[0] = "";
		textRow[0] += line;
		scroll();
		bufferChanged = true;
	}

	public void write( String line ) {
		if (textRow[0] == null) textRow[0] = "";
		textRow[0] += line;
		bufferChanged = true;
	}

	public void scroll() {
		for (int row = Constants.textBufferSize - 1; row > 0; row--) {
			if (textRow[row-1] == null || textRow[row-1].trim().isEmpty()) {
				textRow[row] = "";
			} else {
				textRow[row] = textRow[row - 1].trim();
			}
		}
		textRow[0] = "";	// The row we freed up!
		bufferChanged = true;
	}

	public void draw() {
		glPushMatrix();
		glLoadIdentity();
		glEnable(GL_BLEND);
		glDisable(GL_DEPTH_TEST);

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		for (int row = 0; row < windowRows; row++) {
			if ( textRow[row] != null && !textRow[row].isEmpty()) font2.drawString(txtXMin, txtYMax - ( (row+1) * lineHeight), textRow[row], org.newdawn.slick.Color.green);
		}

		org.newdawn.slick.Color.white.bind();
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		glPopMatrix();

		bufferChanged = false;
	}
}
