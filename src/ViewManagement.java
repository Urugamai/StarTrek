
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;

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
		screenWidth = sw;
		screenHeight = sh;

		window0PercentW = wWF;
		window0PercentH = wHF;
		viewX = new int[4]; viewY = new int[4];
		viewW = new int[4]; viewH = new int[4];
		setViewOrigins();

		try {
			Display.setTitle(Constants.WINDOW_TITLE);
			Display.setFullscreen(fullscreen);
			Display.create();

			setDisplayMode();

			glMatrixMode(GL_PROJECTION);            			// Select The Projection Matrix
			glLoadIdentity();                       			// Reset The Projection Matrix
			// Calculate The Aspect Ratio Of The Window
			//gluPerspective(45.0f,(GLfloat)width/(GLfloat)height,0.1f,100.0f);

			glMatrixMode(GL_MODELVIEW);                			// Select The Modelview Matrix
			glLoadIdentity();                           		// Reset The Modelview Matrix

			// enable textures since we're going to use these for our sprites
			//glShadeModel(GL_SMOOTH);                        // Enables Smooth Shading
			glEnable(GL_COLOR_MATERIAL);                        // Enable Color Material (Allows Us To Tint Textures)
			glEnable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               // Black Background

			glClearDepth(1.0f);                         // Depth Buffer Setup
			glDisable(GL_DEPTH_TEST);							// disable the OpenGL depth test since we're rendering 2D graphics
//			glEnable(GL_DEPTH_TEST);                        // Enables Depth Testing
//			glDepthFunc(GL_LEQUAL);                         // The Type Of Depth Test To Do
//			glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);          // Really Nice Perspective Calculations

//			glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
			glViewport(0, 0, screenWidth, screenHeight);

		} catch (LWJGLException le) {
			System.out.println("Alliance exiting - exception in initialization:");
			le.printStackTrace();
//			Alliance.gameRunning = false;
			return;
		}
	}

	public void Destroy() {
		Display.destroy();
	}

	/**
	 * Sets the display mode for fullscreen mode
	 */
	private boolean setDisplayMode() {
		try {
			// get modes
			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes(screenWidth, screenHeight, -1, -1, -1, -1, Constants.FramesPerSecond, Constants.FramesPerSecond );
			for (int i = 0; i < dm.length; i++)
			{
				if (dm[i].getWidth() > screenWidth && dm[i].getHeight() > screenHeight) {
//					updateDimensions( dm[i].getWidth(), dm[i].getHeight() );
				}
			}

			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
					"width=" + screenWidth,
					"height=" + screenHeight,
					"freq=" + Constants.FramesPerSecond,
					"bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
			});
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to enter fullscreen, continuing in windowed mode");
			return false;
		}

		return true;
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
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		for (int nView = 0; nView < 4; nView++) {
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

		glFlush();
		Display.update();		// update screen contents	(Switch non-visible with visible framebuffer)
	}

	private void drawView(int nView) {
		glColor3ub((byte)0, (byte)0, (byte)0);
		glViewport(viewX[nView], viewY[nView], viewW[nView], viewH[nView] );

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

		glPushMatrix();

		// Pick one ?
//		glMatrixMode (GL_PROJECTION);
//		glMatrixMode(GL_TEXTURE);
// 		glMatrixMode(GL_MODELVIEW);

		glLoadIdentity();

		// draw galactic map in current viewport by asking galaxy.xxx() for needed details
		// ...

		glPopMatrix();
	}

	private void drawSector() {
		if (sector == null) return;

		glPushMatrix();

		// Pick one ?
//		glMatrixMode (GL_PROJECTION);
//		glMatrixMode(GL_TEXTURE);
// 		glMatrixMode(GL_MODELVIEW);

		glLoadIdentity();

		// draw sector map in current viewport by asking sector.xxx() for needed details
		// ...
		ArrayList<Entity> entities = sector.getEntities();

		for (Entity entity : entities) {
			drawEntity(entity);
		}

		glPopMatrix();
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

		float centreY = height/2, centreX = width / 2;

		// bind to the appropriate texture for this sprite
		texture.bind();
		glTranslatef(-centreX, -centreY, 0);			// move centre of image to 0,0,0
		glRotatef(rotation.x, 0.0f, 0.0f, 0.0f);		// rotate image around the 0,0,0 point
		glTranslatef(atX, atY, 0);				// move image to target location

		// draw a quad textured to match the sprite
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, 0);
			glVertex2f(0, 0);

			glTexCoord2f(0, height);
			glVertex2f(0, height);

			glTexCoord2f(width, height);
			glVertex2f(width, height);

			glTexCoord2f(width, 0);
			glVertex2f(width, 0);
		}
		glEnd();
	}
}
