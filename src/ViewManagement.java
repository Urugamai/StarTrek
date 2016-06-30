
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;

import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.util.glu.GLU.gluOrtho2D;

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

	private Galaxy galaxy = null;
	private Sector sector = null;
	private GameText computer = null;
	private Entity ship = null;

	private int[] viewX, viewY, viewH, viewW;

	public ViewManagement(boolean fullscreen) {
		initialiseView(fullscreen, screenWidth, screenHeight, window0PercentW, window0PercentH);
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
//			Alliance.gameRunning = false;
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

	public void setGalaxy(Galaxy g) {
		galaxy = g;
	}

	public void setSector(Sector s) {
		sector = s;
	}

	public void draw(double secondsElapsed) {

		// glMatrixMode, glBegin, glColor4f, glVertex2f, etc have been DEPRECATED!!!
		glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

//		glColor3ub((byte)150, (byte)150, (byte)150);

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

	private void setupView(int nView) {	}

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

	}

	private void drawSector() {
		if (sector == null) return;

		// draw sector map in current viewport by asking sector.xxx() for needed details
		// ...
		ArrayList<Entity> entities = sector.getEntities();

		for (Entity entity : entities) {
				drawEntity(entity);
		}

	}

	private void drawStatus() {
		if (ship == null) return;

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

		int height = sprite.getHeight();
		int width = sprite.getWidth();
		Texture texture = sprite.getTexture();
		Vector3f rotation = sprite.getRotationAngle();

		int centreY = height / 2, centreX = width / 2;

		// store the current model matrix
		glPushMatrix();

		// bind to the appropriate texture for this sprite
		texture.bind();

//		System.out.println("Rotated (" + rotation.x + ", " + rotation.y + ", " + rotation.z + ")");
//		glTranslatef(, , 0);			// move centre of image to 0,0,0
		glTranslatef(atX, atY, 0);				// move image to target location
		glRotatef(rotation.x, 1.0f, 0.0f, 0.0f);		// rotate image around the X axis (the param with a 1.0 as its value)
		glRotatef(rotation.y, 0.0f, 1.0f, 0.0f);		// rotate image around the Y axis (the param with a 1.0 as its value)
		glRotatef(rotation.z, 0.0f, 0.0f, 1.0f);		// rotate image around the Z axis (the param with a 1.0 as its value)
		glTranslatef(-centreX, -centreY, 0);				// move image to target location

		// draw a quad textured to match the sprite
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, 0);										glVertex2f(0, 0);
			glTexCoord2f(0, texture.getHeight());					glVertex2f(0, height);
			glTexCoord2f(texture.getWidth(), texture.getHeight());	glVertex2f(width, height);
			glTexCoord2f(texture.getWidth(), 0);					glVertex2f(width, 0);
		}
		glEnd();

		glPopMatrix();
	}
}
