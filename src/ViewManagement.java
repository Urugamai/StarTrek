
import java.awt.*;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javafx.scene.canvas.GraphicsContext;
import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.font.GlyphPage;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.gui.*;
import org.newdawn.slick.opengl.TextureImpl;
import static org.newdawn.slick.Color.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.pbuffer.GraphicsFactory;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Mark on 25/06/2016.
 */
public class ViewManagement {

	private int	fps = 0;
	private double fpsTimer = 0;

	// Starting hints - we use the biggest we can get in the end
	private int screenWidth = 1024;
	private int screenHeight = 768;

	private Alliance alliance = null;
	private Galaxy galaxy = null;
	private Sector sector = null;
	private Entity ship = null;
	private UserManagement user = null;
	private ComputerManagement computer = null;

	private int[] viewX, viewY, viewH, viewW;

	private String[] infoLines;
	private int maxInfoLines = 7;

	private static TrueTypeFont fontGalaxy = null;
	private static UnicodeFont fontComputer = null;

	public ViewManagement(boolean fullscreen) {
		initialiseView(fullscreen, screenWidth, screenHeight);
	}

	public ViewManagement(boolean fullscreen, int sw, int sh) {
		initialiseView(fullscreen, sw, sh);
	}

	public void initialiseView(boolean fullscreen, int sw, int sh) {
		int selectedDM = -1;
		DisplayMode[] dm = null;
		screenWidth = sw;
		screenHeight = sh;

		viewX = new int[4]; viewY = new int[4];
		viewW = new int[4]; viewH = new int[4];

		infoLines = new String[maxInfoLines];
		for (int i = 0; i < maxInfoLines; i++) infoLines[i] = "";

		// Setup DISPLAY - find biggest screen size we can get and use it
		try {
			Display.setTitle(Constants.WINDOW_TITLE);
			Display.setResizable(false);
			try {
				// get modes
				dm = Display.getAvailableDisplayModes();
				for (int i = 0; i < dm.length; i++)
				{
					if (dm[i].getWidth() >= screenWidth && dm[i].getHeight() >= screenHeight) {
						selectedDM = i;
						screenWidth = dm[i].getWidth();
						screenHeight = dm[i].getHeight();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Unable to enter fullscreen, continuing in windowed mode");
			}

			if (dm == null || selectedDM < 0) return;	// no suitable display modes available

			Display.setDisplayMode(dm[selectedDM]);
			Display.setFullscreen(fullscreen);
			Display.create();

			setViewOrigins();	// now we have the screen sized - determine where the views are going to be

			createGL();
			resizeGL();

			setupFonts();

		} catch (LWJGLException le) {
			System.out.println("Alliance exiting - exception in initialization:");
			le.printStackTrace();
			alliance.gameRunning = false;
			return;
		}
	}

	private void createGL() {
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_DEPTH_TEST);							// disable the OpenGL depth test since we're rendering 2D graphics
		glDisable(GL_LIGHTING);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               // Black Background
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, screenWidth, screenHeight, 0, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	private void resizeGL() {
		glViewport(0, 0, screenWidth, screenHeight);
	}

	private void setupFonts() {
		Font galaxyFont = new Font(Font.MONOSPACED, Font.PLAIN, 14); //name, style (PLAIN, BOLD, or ITALIC), size (TODO size based on galaxySize so galaxy view always fits in its box)
		fontGalaxy = new TrueTypeFont(galaxyFont, false);

		Font computerFont;
		computerFont = new Font("Courier New", Font.PLAIN, 16);
		fontComputer = new UnicodeFont(computerFont);
		fontComputer.getEffects().add(new ColorEffect());
		fontComputer.addAsciiGlyphs();
		try {
			fontComputer.loadGlyphs();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	public void Destroy() {
		Display.destroy();
	}

	private void setViewOrigins() {
		int window0W = (int)Math.floor(screenWidth*Constants.sectorWindowPercentW);
		int window0H = (int)Math.floor(screenHeight*Constants.sectorWindowPercentH);
		int window3W = screenWidth - window0W;
		int window3H = screenHeight - window0H;

		viewX[0] = 0; 			viewY[0] = window3H; 	viewW[0] = window0W; 	viewH[0] = window0H;
		viewX[1] = window0W;	viewY[1] = window3H;	viewW[1] = window3W; 	viewH[1] = window0H;
		viewX[2] = 0; 			viewY[2] = 0; 			viewW[2] = window0W; 	viewH[2] = window3H;
		viewX[3] = window0W;	viewY[3] = 0; 			viewW[3] = window3W; 	viewH[3] = window3H;
	}

	public int getViewWidth(int nView) {
		assert (nView >= 0);
		assert (nView < 4);
		return viewW[nView];
	}

	public int getViewHeight(int nView) {
		assert (nView >= 0);
		assert (nView < 4);
		return viewH[nView];
	}

	public void setAlliance(Alliance a) { alliance = a; }

	public void setGalaxy(Galaxy g) {
		galaxy = g;
	}

	public void setSector(Sector s) {
		sector = s;
	}

	public void setShip(Entity s) { ship = s; }

	public void setComputer(ComputerManagement c) { computer = c; }

	public void setUser(UserManagement u) { user = u; }

	public void writeScreen(String message) {
		// use first free line
		for (int i = 0; i < maxInfoLines; i++) {
			if (infoLines[i].isEmpty()) {
				infoLines[i] = message;
				return;
			}
		}

		// Scroll all lines up one, free up bottom line
		for (int i = 0; i < maxInfoLines - 1; i++) {
			infoLines[i] = infoLines[i + 1];
		}

		infoLines[maxInfoLines-1] = message;
	}

	public void draw(double secondsElapsed) {

		glClear(GL_COLOR_BUFFER_BIT);

		for (int nView = 0; nView < 4; nView++) {
			setupView(nView);
			drawView(nView, secondsElapsed);
		}

		fps++;	// we drew a frame
		fpsTimer += secondsElapsed;

		// update our FPS counter if a second has passed
		if (fpsTimer >= 1.0) {
			Display.setTitle(Constants.WINDOW_TITLE + " (FPS: " + fps + ")");
			fpsTimer -= 1.0;
			fps = 0;
		}

		glFlush();
		Display.update();		// update screen contents	(Switch non-visible with visible framebuffer)
//		Display.sync(60);
	}

	private void setupView(int nView) {
		glViewport(viewX[nView], viewY[nView], viewW[nView], viewH[nView] );
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, viewW[nView], viewH[nView], 0, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		glEnable(GL_TEXTURE_2D);
	}

	private void drawView(int nView, double secondsElapsed) {

		switch (nView) {
			case Constants.viewSector:
				drawSector();
				break;

			case Constants.viewStatus:
				drawStatus();
				break;

			case Constants.viewComputer:
				drawComputer();
				break;

			case Constants.viewGalaxy:
				drawGalaxy(secondsElapsed);
				break;

			default:
				break;
		}
	}

	private void drawGalaxy(double secondsElapsed) {
		if (galaxy == null) return;

		org.newdawn.slick.Color txtColor;
		String dontKnow = "*";

		int height = fontGalaxy.getLineHeight()+2;
		int width = (fontGalaxy.getWidth("000")+3);

		for(int gx = 0; gx < alliance.galaxySize; gx++) {
			for(int gy = 0; gy < alliance.galaxySize; gy++) {
				Entity.LRS lrs = ship.getLRS(gx, gy);
				String text = "" + (lrs.enemyCount < 0 ? dontKnow : lrs.enemyCount) + (lrs.starbaseCount < 0 ? dontKnow : lrs.starbaseCount) + (lrs.planetCount < 0 ? dontKnow : lrs.planetCount);
				if ((gx == (int)ship.galacticLoc.x) && (gy == (int)ship.galacticLoc.y)) txtColor = green;
				else txtColor = white;
				fontGalaxy.drawString((gx+0)*width+50,(gy+0)*height, text, txtColor);
			}
		}
	}

	private void drawSector() {
		if (sector == null) return;

		ArrayList<Entity> entities = sector.getEntities();

		for (Entity entity : entities) {
				drawEntity(entity);
		}
	}

	private void drawStatus() {
		if (ship == null) return;

		int ex = (int)Math.floor(ship.galacticLoc.x);
		int ey = (int)Math.floor(ship.galacticLoc.y);

		glPushMatrix();									// store the current model matrix

		drawSprite(ship.sprite, 100, 100, 20.0f, false);

		org.newdawn.slick.Color txtColor = green;
		int height = fontComputer.getLineHeight()+2;
		String text; // = "This is a test of the computer output capability 0123456789 Test Complete.";

		int textY = ship.sprite.getHeight()*20+120;

		text = "Stardate: " + alliance.starDate;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Location: (" + ex + ", " + ey + ")";
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Energy Level: " + (int)ship.energyLevel;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Shields: " + (ship.shieldsUp ? " Up" : " Down");
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Shield Energy: " + (int)ship.shieldEnergy;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Torpedo Count: " + ship.torpedoCount;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Enemy Count: " + alliance.totalEnemy;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "Starbase Count: " + alliance.totalStarbases;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		text = "DOCKED: " + ship.docked;
		fontComputer.drawString(0, textY, text, txtColor);
		textY += height;

		if (ship.docked && ship.dockedWith != null) {
			text = "  Base Energy: " + (int)ship.dockedWith.energyLevel;
			fontComputer.drawString(0, textY, text, txtColor);
			textY += height;
			text = "  Base Torpedo Stock: " + ship.dockedWith.torpedoCount;
			fontComputer.drawString(0, textY, text, txtColor);
			textY += height;
		}

		glPopMatrix();
	}

	private void drawComputer() {
		if (computer == null) return;

		glPushMatrix();									// store the current model matrix

		org.newdawn.slick.Color txtColor = green;
		int height = fontComputer.getLineHeight()+2;
		String text; // = "This is a test of the computer output capability 0123456789 Test Complete.";

		text = computer.getLastCommand();
		fontComputer.drawString(0, 0, "last Command: " + text, txtColor);

		text = user.getUserInput();
		fontComputer.drawString(0, 0 + height, "Computer: " + text, txtColor);

		for (int i = 0; i < maxInfoLines; i++) {
			if (infoLines[i].isEmpty()) break;	// done

			fontComputer.drawString(0, height *(i+2), infoLines[i], txtColor);
		}

		glPopMatrix();
	}

	private void drawEntity(Entity entity) {
		Sprite sprite = entity.sprite;
		Vector3f location = sprite.getLocation();

		drawSprite(sprite, (int)Math.floor(location.x), (int)Math.floor(location.y) );
	}

	private void drawSprite(Sprite sprite, int atX, int atY) {

		drawSprite(sprite, atX, atY, 1.0f, true);
	}

	private void drawSprite(Sprite sprite, int atX, int atY, float scale, boolean effects) {
		int height = sprite.getHeight();
		int width = sprite.getWidth();
		Texture texture = sprite.getTexture();
		Vector3f rotation = sprite.getRotationAngle();
//		Angle[] angle = (Angle[]) Array.newInstance(Angle.class, 3);
		Angle[] angle = new Angle[3];
		angle[0] = new Angle("Sprite",rotation.x);
		angle[1] = new Angle("Sprite",rotation.y);
		angle[2] = new Angle("Sprite",rotation.z);

		int centreY = height / 2, centreX = width / 2;

		glPushMatrix();									// store the current model matrix
		texture.bind();									// bind to the appropriate texture for this sprite

		glTranslatef(atX, atY, 0);						// move image to target location
		if (effects) {
			glRotatef((float)angle[0].getSpriteAngle(), 1.0f, 0.0f, 0.0f);        // rotate image around the X axis (the param with a 1.0 as its value)
			glRotatef((float)angle[1].getSpriteAngle(), 0.0f, 1.0f, 0.0f);        // rotate image around the Y axis (the param with a 1.0 as its value)
			glRotatef((float)angle[2].getSpriteAngle(), 0.0f, 0.0f, 1.0f);        // rotate image around the Z axis (the param with a 1.0 as its value)
		}
		glTranslatef(-centreX, -centreY, 0);			// move image to correct for rotation around 0,0 (a corner of the image)

		// draw a quad textured to match the sprite
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, 0);										glVertex2f(0, 0);
			glTexCoord2f(0, texture.getHeight());					glVertex2f(0, height*scale);
			glTexCoord2f(texture.getWidth(), texture.getHeight());	glVertex2f(width*scale, height*scale);
			glTexCoord2f(texture.getWidth(), 0);					glVertex2f(width*scale, 0);
		}
		glEnd();

		glBindTexture(0,0);								// unbind so life for other sections are easier
		glPopMatrix();
	}
}
