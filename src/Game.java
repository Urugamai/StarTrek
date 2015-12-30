/*
 * Copyright (c) 2002-2010 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.util.Date;

import static org.lwjgl.opengl.GL11.*;

/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic.
 *
 * Display management will consist of a loop that cycles round all
 * entities in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main ship.
 *
 * As a mediator it will be informed when entities within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 *
 */
public class Game {

	private Galaxy galaxy;
	private Galaxy.galacticLocation currentSector;
	private Sector sector;

	private int					width				= 1000;
	private int					height				= 1000;

	private long				msElapsed;

	private TextureLoader		textureLoader;

	private GameText 			textWindow;
	private String				userInput = "";
	private boolean				returnDown;

	private Constants.DisplayMode displayMode;

	private long				lastLoopTime		= getTime();
	private long				lastFpsTime;
	private int					fps;
	private static long			timerTicksPerSecond	= Sys.getTimerResolution();

	public static boolean		gameRunning			= true;
	public SoundManager			soundManager;
	private boolean				fullscreen;

	public int					SOUND_SHOT;
	public int					SOUND_HIT;
	public int					SOUND_START;
	public int					SOUND_WIN;
	public int					SOUND_LOOSE;

	private int					mouseX;

	private static boolean 		isApplication;

	/**
	 * Construct our game and set it running.
	 * @param fullscreen
	 *
	 */
	public Game(boolean fullscreen) {
		this.fullscreen = fullscreen;
		initialize();
	}

	/**
	 * Get the high resolution time in milliseconds
	 *
	 * @return The high resolution time in milliseconds
	 */
	public static long getTime() {
		// we get the "timer ticks" from the high resolution timer
		// multiply by 1000 so our end result is in milliseconds
		// then divide by the number of ticks in a second giving
		// us a nice clear time in milliseconds
		// Code is effectively return (Sys.getTime() * 1000) / Sys.getTimerResolution(); without all the calling overhead
		return (Sys.getTime() * 1000) / timerTicksPerSecond;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Create or get a sprite which displays the image that is pointed
	 * to in the classpath by "ref"
	 *
	 * @param ref A reference to the image to load
	 * @return A sprite that can be drawn onto the current graphics context.
	 */
	public Sprite getSprite(String ref) {
		return new Sprite(textureLoader, ref);
	}

	/**
	 * Sleep for a fixed number of milliseconds.
	 *
	 * @param duration The amount of time in milliseconds to sleep for
	 */
	public static void sleep(long duration) {
		try {
			Thread.sleep((duration * timerTicksPerSecond) / 1000);
		} catch (InterruptedException inte) {
		}
	}

	/**
	 * Intialise the common elements for the game
	 */
	public void initialize() {
		// initialize the window beforehand
		displayMode = Constants.DisplayMode.Sector;
		try {
			setDisplayMode();
			Display.setTitle(Constants.WINDOW_TITLE);
			Display.setFullscreen(fullscreen);
			Display.create();

			// grab the mouse, dont want that hideous cursor when we're playing!
			if (isApplication) {
				Mouse.setGrabbed(true);
			}

			// enable textures since we're going to use these for our sprites
			glEnable(GL_TEXTURE_2D);

			// disable the OpenGL depth test since we're rendering 2D graphics
			glDisable(GL_DEPTH_TEST);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();

			glOrtho(0, width, height, 0, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glViewport(0, 0, width, height);

			textureLoader = new TextureLoader();

			// create our sound manager, and initialize it with 7 channels
			// 1 channel for sounds, 6 for effects - this should be enough
			// since we have a most 4 shots on screen at any one time, which leaves
			// us with 2 channels for explosions.
			soundManager = new SoundManager();
			soundManager.initialize(8);

			// load our sound data
			SOUND_SHOT   = soundManager.addSound(Constants.FILE_SND_TORPEDO);
			SOUND_HIT    = soundManager.addSound(Constants.FILE_SND_HIT);
			SOUND_START  = soundManager.addSound(Constants.FILE_SND_START);
			SOUND_WIN    = soundManager.addSound(Constants.FILE_SND_WIN);
			SOUND_LOOSE  = soundManager.addSound(Constants.FILE_SND_LOSE);
		} catch (LWJGLException le) {
			System.out.println("Game exiting - exception in initialization:");
			le.printStackTrace();
			Game.gameRunning = false;
			return;
		}

		textWindow = new GameText(0, height, 5);
		textWindow.setTextColour( org.newdawn.slick.Color.green);
		textWindow.write( "Star Trekking across the universe...");
		textWindow.scroll();

		// setup the initial game state
		startGame();
	}

	/**
	 * Sets the display mode for fullscreen mode
	 */
	private boolean setDisplayMode() {
		try {
			// get modes
			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes(width, height, -1, -1, -1, -1, Constants.FramesPerSecond, Constants.FramesPerSecond);

			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
					"width=" + width,
					"height=" + height,
					"freq=" + Constants.FramesPerSecond,
					"bpp=" + org.lwjgl.opengl.Display.getDisplayMode().getBitsPerPixel()
			});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to enter fullscreen, continuing in windowed mode");
		}

		return false;
	}

	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame() {
		// clear out any existing entities and intialise a new set
		//entities.clear();
		galaxy = new Galaxy(this);
		galaxy.initSectors( width, (height - textWindow.getHeight()) );
//		currentSector = galaxy.getSafeSector();	// TODO: To become a 'player skill-level' based selection of initial sector
		currentSector = galaxy.getLeastSafeSector();
		sector = galaxy.getSector(currentSector);
	}

	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 */
	public void updateLogic() {
	}

	/**
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 */
	private void gameLoop() {
		soundManager.playEffect(SOUND_START);

		//SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
//		Display.sync(60);	// Causes this loop to stop until the next 60th of a second is ready

		while (Game.gameRunning) {
			setTimeDelta();

			userInteractions();

			sector.processHits();

			sector.doLogic();

			// let subsystem paint
			frameRendering();

			// update window contents
			Display.update();
		}

		// clean up
		soundManager.destroy();
		Display.destroy();
	}

	private void setTimeDelta() {
		// work out how long its been since the last update, this
		// will be used to calculate how far the entities should
		// move this loop
		//sleep(1);
		long now = getTime();
		msElapsed = now - lastLoopTime;
		lastLoopTime = now;
		lastFpsTime += msElapsed;
		fps++;

		// update our FPS counter if a second has passed
		if (lastFpsTime >= 1000) {
			Display.setTitle(Constants.WINDOW_TITLE + " (FPS: " + fps + ")");
			lastFpsTime %= 1000;
			fps = 0;
		}
	}

	private char getCurrentKey() {
		boolean returnPressed = Keyboard.isKeyDown(Keyboard.KEY_RETURN);
		if (returnPressed) {
			if (returnDown) return '\0';  // Already processed this pressing of return
			returnDown = true;
			return Keyboard.getEventCharacter();
		}

		if (Keyboard.next()) {
			return Keyboard.getEventCharacter();
		}

		return '\0';
	}

	private void userInteractions() {

		textWindow.writeLine(4, "Currently in sector (" + currentSector.getGx() + "," + currentSector.getGy() + ")");

		if (Keyboard.isKeyDown(Keyboard.KEY_F2)) displayMode = Constants.DisplayMode.GalacticMap;
		else if (Keyboard.isKeyDown(Keyboard.KEY_F1)) displayMode = Constants.DisplayMode.DamageReport;
		else displayMode = Constants.DisplayMode.Sector;

		char key = getCurrentKey();

		if (key != '\0') {
			// do something with this key
			//if (key == 32) return;	// space
			if (key == 13) {
				processCommand(userInput);
				userInput = "";
				textWindow.scroll();
			} else {
				if (key == 8 && userInput.length() > 0 ) { // Backspace
					userInput = userInput.substring(0, userInput.length() - 1);
					textWindow.writeLine(0, userInput);
				} else {
					userInput += key;
					textWindow.write("" + key);
				}
			}
		}

		// if escape has been pressed, stop the game
		if ((Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) && isApplication) {
			Game.gameRunning = false;
		}
	}

	/** THE MAIN GAME PROCESSOR, called every loop
	 * ********************************************
	 *
	 * Notification that a frame is being rendered. Responsible for
	 * running game logic and rendering the scene.
	 */
	public void frameRendering() {	//

		// clear non-visible screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		sector.move(msElapsed);

		if (displayMode == Constants.DisplayMode.Sector)
			sector.draw();
		else if (displayMode == Constants.DisplayMode.GalacticMap)
			galaxy.draw();
		else if (displayMode == Constants.DisplayMode.DamageReport)
			galaxy.draw();	// TODO: Change this to a picture showing the current ship damage status

		textWindow.draw();
	}

	private void processCommand(String cmd) {
		float angle;
		float force;
		float seconds;
		String[] pieces;
		String direction = "", power = "", duration = "";

		pieces = cmd.trim().toUpperCase().split("[ ,\\t\\n\\x0B\\f\\r]");

		if (pieces[0].compareToIgnoreCase("TOR") == 0 ) {
			if (pieces.length > 1) {
				direction = pieces[1];
				try {
					angle = Float.valueOf(direction);
				} catch (Exception e) {
					textWindow.writeLine(1, "Syntax Error: Command format should be: TOR,direction");
					return;  // no firing for you when you get the parameter wrong
				}
				sector.tryToFire(angle);
			} else {
				textWindow.writeLine(1, "Syntax Error: Command format should be: TOR,direction");
				return;  // no firing for you when you get the parameter wrong
			}

			return;
		}

		if (pieces[0].compareToIgnoreCase("IMP") == 0 ) {
			if ( pieces.length > 3 ) {
				direction = pieces[1];
				power = pieces[2];
				duration = pieces[3];

				try {
					angle = Float.valueOf(direction);
					force = Float.valueOf(power);
					seconds = Float.valueOf(duration);
				} catch (Exception e) {
					textWindow.writeLine(1, "Syntax Error: direction and force must be numeric: IMP,direction,accel,duration");
					return; // no moving for you when you get the parameters wrong
				}
			} else {
				textWindow.writeLine(1, "Syntax Error: Command format should be: IMP,direction,accel,duration");
				return; // no moving for you when you get the parameters wrong
			}

			sector.setShipHeading(angle, 0);
			sector.setShipThrust( force, seconds);
			textWindow.writeLine(0, "Command Complete: " + pieces[0] + " " + pieces[1] + " " + pieces[2] + " " + pieces[3]);

			return;
		}

		if (pieces[0].compareToIgnoreCase("STOP") == 0 ) {
			sector.setShipVelocity(0.0f);
			return;
		}

		if (pieces[0].compareToIgnoreCase("LRS") == 0 ) {
			galaxy.doLRS(currentSector);
			return;
		}

		if (pieces[0].compareToIgnoreCase("SRS") == 0 ) {
			galaxy.doSRS(currentSector);
			return;
		}

		if (pieces[0].compareToIgnoreCase("EXIT") == 0 ) {
			Game.gameRunning = false;
			return;
		}

		textWindow.writeLine(1, "Error: No such command: " + cmd);
		return;
	}

	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 *
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		isApplication = true;
		System.out.println("Use -fullscreen for fullscreen mode");
		new Game((argv.length > 0 && "-fullscreen".equalsIgnoreCase(argv[0]))).execute();
//		new Game(true).execute();	// force to full screen

		System.exit(0);
	}

	/**
	 *
	 */
	public void execute() {
		gameLoop();
	}

}
