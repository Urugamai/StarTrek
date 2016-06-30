
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.*;

import java.awt.*;
import java.awt.Font;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Mark on 25/06/2016.
 */
public class ViewManagement {

	private int	fps = 0;
	private double fpsTimer = 0;

	private int screenWidth = 1024;
	private int screenHeight = 768;
	private double window0PercentW = 0.8;
	private double window0PercentH = 0.8;

	private Alliance alliance = null;
	private Galaxy galaxy = null;
	private Sector sector = null;
	private GameText computer = null;
	private Entity ship = null;

	private int[] viewX, viewY, viewH, viewW;

	private TrueTypeFont fontGalaxy;
	private Font awtFont = new Font("Courier", Font.PLAIN, 16); //name, style (PLAIN, BOLD, or ITALIC), size

	public ViewManagement(boolean fullscreen) {
		initialiseView(fullscreen, screenWidth, screenHeight, window0PercentW, window0PercentH);
		fontGalaxy = new TrueTypeFont(awtFont, false); //base Font, anti-aliasing true/false
	}

	public ViewManagement(boolean fullscreen, int sw, int sh) {
		initialiseView(fullscreen, sw, sh, window0PercentW, window0PercentH);
	}

	public ViewManagement(boolean fullscreen, int sw, int sh, double wWF, double wHF) {
		initialiseView(fullscreen, sw, sh, wWF, wHF);
	}

	public void initialiseView(boolean fullscreen, int sw, int sh, double wWF, double wHF) {
		int selectedDM = -1;
		DisplayMode[] dm = null;
		screenWidth = sw;
		screenHeight = sh;

		window0PercentW = wWF;
		window0PercentH = wHF;
		viewX = new int[4]; viewY = new int[4];
		viewW = new int[4]; viewH = new int[4];

		// Setup DISPLAY
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

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               // Black Background

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, screenWidth, screenHeight, 0, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	private void resizeGL() {
		glViewport(0, 0, screenWidth, screenHeight);
	}

	public void Destroy() {
		Display.destroy();
	}

	private void setViewOrigins() {
		int window0W = (int)Math.floor(screenWidth*window0PercentW);
		int window0H = (int)Math.floor(screenHeight*window0PercentH);
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

	public void draw(double secondsElapsed) {

		glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		for (int nView = 0; nView < 4; nView++) {
			setupView(nView);
			drawView(nView);
		}

		fps++;	// we drew a frame
		fpsTimer += secondsElapsed;

		// update our FPS counter if a second has passed
		if (fpsTimer >= 1.0) {
			Display.setTitle(Constants.WINDOW_TITLE + " (FPS: " + fps + ")");
			fpsTimer -= 1.0;
			fps = 0;
		}

//		glFlush();
		Display.update();		// update screen contents	(Switch non-visible with visible framebuffer)
	}

	private void setupView(int nView) {
		glViewport(viewX[nView], viewY[nView], viewW[nView], viewH[nView] );
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, viewW[nView], viewH[nView], 0, 1, -1);
	}

	private void drawView(int nView) {

		switch (nView) {
			case 0:
				drawSector();
				break;

			case 1:
				drawStatus();
				break;

			case 2:
				drawComputer();
				break;

			case 3:
				drawGalaxy();
				break;

			default:
				break;
		}
	}

	private void drawGalaxy() {
		if (galaxy == null) return;
		int height = fontGalaxy.getHeight()+1;
		int width = (fontGalaxy.getWidth("1")+0)*3;

		for(int gx = 0; gx < alliance.galaxySize; gx++) {
			for(int gy = 0; gy < alliance.galaxySize; gy++) {
				Sector sector = galaxy.getSector(gx, gy);
				String text = "x1z";// + sector.enemyCount + sector.starbaseCount + sector.planetCount;
				fontGalaxy.drawString((gx+0)*width,(gy+0)*height, text, org.newdawn.slick.Color.white);
				//drawSprite(ship.sprite, (gx+1)*15, (gy+1)*15, 0.5f);
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

		drawSprite(ship.sprite, 100, 100, 20.0f);
	}

	private void drawComputer() {
		if (computer == null) return;

	}

	private void drawEntity(Entity entity) {
		Sprite sprite = entity.sprite;
		Vector3f location = sprite.getLocation();

		drawSprite(sprite, (int)Math.floor(location.x), (int)Math.floor(location.y) );
	}

	private void drawSprite(Sprite sprite, int atX, int atY) {

		drawSprite(sprite, atX, atY, 1.0f);
	}

	private void drawSprite(Sprite sprite, int atX, int atY, float scale) {
		int height = sprite.getHeight();
		int width = sprite.getWidth();
		Texture texture = sprite.getTexture();
		Vector3f rotation = sprite.getRotationAngle();

		int centreY = height / 2, centreX = width / 2;

		// store the current model matrix
		glPushMatrix();

		// bind to the appropriate texture for this sprite
		texture.bind();

		glTranslatef(atX, atY, 0);				// move image to target location
		glRotatef(rotation.x, 1.0f, 0.0f, 0.0f);		// rotate image around the X axis (the param with a 1.0 as its value)
		glRotatef(rotation.y, 0.0f, 1.0f, 0.0f);		// rotate image around the Y axis (the param with a 1.0 as its value)
		glRotatef(rotation.z, 0.0f, 0.0f, 1.0f);		// rotate image around the Z axis (the param with a 1.0 as its value)
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

		glPopMatrix();
	}
}
