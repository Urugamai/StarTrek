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

import java.security.Key;
import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

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

	/** Game Parameters */
	private static String		WINDOW_TITLE		= "Star Trek - TNG";

	// Graphics
	private static String		FILE_IMG_ENTERPRISE	= "res/ST_Enterprise.gif";
	private static String		FILE_IMG_TORPEDO	= "res/ST_Torpedo.gif";
	public static  String 		FILE_IMG_ROMULAN 	= "res/ST_Romulan.gif";
	public static  String		FILE_IMG_STARBASE	= "res/ST_StarBase.gif";
	public static  String		FILE_IMG_STAR		= "res/ST_Star.gif";

	// Audio
	private static String		FILE_SND_TORPEDO	= "res/ST_Torpedo.wav";

	// Originals
	private static String		FILE_IMG_START		= "res/pressanykey.gif";
	private static String		FILE_IMG_LOSE		= "res/gotyou.gif";
	private static String		FILE_IMG_WIN		= "res/youwin.gif";

	private static String		FILE_SND_HIT		= "res/hit.wav";
	private static String		FILE_SND_LOSE		= "res/loose.wav";
	private static String		FILE_SND_START		= "res/start.wav";
	private static String		FILE_SND_WIN		= "res/win.wav";

	private int					width				= 1000;
	private int					height				= 1000;
	private int					heightTextArea;

	private float				moveSpeed			= 300;
	private long				firingInterval		= 500;
	private long				msElapsed;

	private TextureLoader		textureLoader;

	private GameText 			textWindow;
	private String				userInput = "";
	private boolean				returnDown;

	private ArrayList<Entity>	entities			= new ArrayList<Entity>();
	private ArrayList<Entity>	removeList			= new ArrayList<Entity>();
	private PlayerShipEntity	ship;
	private TorpedoEntity[]		shots;

	private Sprite				message;
	private Sprite				pressAnyKey;
	private Sprite				youWin;
	private Sprite				gotYou;

	private int					shotIndex;
	private long				lastFire;
	private int					alienCount;
	private boolean				waitingForKeyPress	= true;
	private boolean				logicRequiredThisLoop;
	private long				lastLoopTime		= getTime();
	private boolean				fireHasBeenReleased;

	private long				lastFpsTime;
	private int					fps;
	private static long			timerTicksPerSecond	= Sys.getTimerResolution();

	public static boolean		gameRunning			= true;
	private SoundManager		soundManager;
	private boolean				fullscreen;

	private int					SOUND_SHOT;
	private int					SOUND_HIT;
	private int					SOUND_START;
	private int					SOUND_WIN;
	private int					SOUND_LOOSE;

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
		return (Sys.getTime() * 1000) / timerTicksPerSecond;
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
		try {
			setDisplayMode();
			Display.setTitle(WINDOW_TITLE);
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
			SOUND_SHOT   = soundManager.addSound(FILE_SND_TORPEDO);
			SOUND_HIT    = soundManager.addSound(FILE_SND_HIT);
			SOUND_START  = soundManager.addSound(FILE_SND_START);
			SOUND_WIN    = soundManager.addSound(FILE_SND_WIN);
			SOUND_LOOSE  = soundManager.addSound(FILE_SND_LOSE);
		} catch (LWJGLException le) {
			System.out.println("Game exiting - exception in initialization:");
			le.printStackTrace();
			Game.gameRunning = false;
			return;
		}

		// get our sprites
		gotYou = getSprite(FILE_IMG_LOSE);
		pressAnyKey = getSprite(FILE_IMG_START);
		youWin = getSprite(FILE_IMG_WIN);

		message = pressAnyKey;

		// setup 5 shots
		shots = new TorpedoEntity[15];
		for (int i = 0; i < shots.length; i++) {
			shots[i] = new TorpedoEntity(this, FILE_IMG_TORPEDO, 0, 0);
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
			DisplayMode[] dm = org.lwjgl.util.Display.getAvailableDisplayModes(width, height, -1, -1, -1, -1, 60, 60);

			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
					"width=" + width,
					"height=" + height,
					"freq=" + 60,
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
		entities.clear();
		initEntities();
	}

	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entity will be added to the overall list of entities in the game.
	 */
	private void initEntities() {
		// create the player ship and place it somewhere TODO Make the initial location of the Enterprise Random but not on any existing objects
		ship = new PlayerShipEntity(this, FILE_IMG_ENTERPRISE, 50, 50);
		entities.add(ship);

		// TODO Make the enemy ship locations initially random and not in every sector
		// todo make a SET of enemy vessels and place throughout federation space
		Entity Romulan = new EnemyShipEntity(this, width - 50, height - textWindow.getHeight() -50);
		entities.add(Romulan);

		Entity Star = new StarEntity(this, FILE_IMG_STAR, width/2, (height - textWindow.getHeight())/2);
		entities.add(Star);

		// todo make the starbase location somewhat random and only in a few sectors
		Entity StarBase = new FriendlyEntity(this, FILE_IMG_STARBASE, width-50, 100);
		entities.add(StarBase);
	}

	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 */
	public void updateLogic() {
		logicRequiredThisLoop = true;
	}

	/**
	 * Remove an entity from the game. The entity removed will
	 * no longer move or be drawn.
	 *
	 * @param entity The entity that should be removed
	 */
	public void removeEntity(Entity entity) {
		removeList.add(entity);
	}

	/**
	 * Notification that the player has died.
	 */
	public void notifyDeath() {
		if (!waitingForKeyPress) {
			soundManager.playSound(SOUND_LOOSE);
		}
		message = gotYou;
		waitingForKeyPress = true;
	}

	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
		message = youWin;
		waitingForKeyPress = true;
		soundManager.playSound(SOUND_WIN);
	}

	/**
	 * Notification that an alien has been killed
	 */
	public void notifyAlienKilled() {
		// reduce the alient count, if there are none left, the player has won!
		alienCount--;

		if (alienCount == 0) {
			notifyWin();
		}

		// if there are still some aliens left then they all need to get faster, so
		// speed up all the existing aliens
		for ( Entity entity : entities ) {
			if ( entity instanceof EnemyShipEntity ) {
				// speed up by 2%
				entity.setHorizontalMovement(entity.getHorizontalMovement() * 1.04f);
			}
		}

		soundManager.playEffect(SOUND_HIT);
	}

	/**
	 * Attempt to fire a shot from the player in the direction provided.
	 * Its called "try"
	 * since we must first check that the player can fire at this
	 * point.
	 */
	public void tryToFire(float direction) {

		// TODO check if a torpedo tube has been [re]loaded and so is available to shoot

		TorpedoEntity shot = shots[shotIndex++ % shots.length];
		shot.reinitialize(ship.getX(), ship.getY(), direction);
		entities.add(shot);

		soundManager.playEffect(SOUND_SHOT);
	}

	/**
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 */
	private void gameLoop() {
		while (Game.gameRunning) {
			// clear screen
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

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
		//SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
		Display.sync(60);

		// work out how long its been since the last update, this
		// will be used to calculate how far the entities should
		// move this loop
		long now = getTime();
		msElapsed = now - lastLoopTime;
		lastLoopTime = now;
		lastFpsTime += msElapsed;
		fps++;

		// update our FPS counter if a second has passed
		if (lastFpsTime >= 1000) {
			Display.setTitle(WINDOW_TITLE + " (FPS: " + fps + ")");
			lastFpsTime %= 1000;
			fps = 0;
		}
	}

	private void processHits() {
		// brute force collisions, compare every entity against
		// every other entity. If any of them collide notify
		// both entities that the collision has occurred
		for (int p = 0; p < entities.size(); p++) {
			for (int s = p + 1; s < entities.size(); s++) {
				Entity me = entities.get(p);
				Entity him = entities.get(s);

				if (me.collidesWith(him)) {
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}

		// remove any entity that has been marked for clear up
		entities.removeAll(removeList);
		removeList.clear();
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

	private void userInteracations() {

		char key = getCurrentKey();

		if (key != '\0') {
			// do something with this key
			if (key == 32) return;	// space ignored
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

/* TO BE REMOVED
		// resolve the movement of the ship. First assume the ship
		// isn't moving. If either cursor key is pressed then
		// update the movement appropriately
		ship.setHorizontalMovement(0);

		// get mouse movement on x axis. We need to get it now, since
		// we can only call getDX ONCE! - secondary calls will yield 0, since
		// there haven't been any movement since last call.
		mouseX = Mouse.getDX();

		// we delegate input checking to sub-method since we want to check
		// for keyboard, mouse & controller
		boolean leftPressed   = hasInput(Keyboard.KEY_LEFT);
		boolean rightPressed  = hasInput(Keyboard.KEY_RIGHT);
		boolean firePressed   = hasInput(Keyboard.KEY_SPACE);

		if (!waitingForKeyPress && !soundManager.isPlayingSound()) {
			if ((leftPressed) && (!rightPressed)) {
				ship.setHorizontalMovement(-moveSpeed);
			} else if ((rightPressed) && (!leftPressed)) {
				ship.setHorizontalMovement(moveSpeed);
			}

			// if we're pressing fire, attempt to fire
			if (firePressed) {
				tryToFire(0);
			}
		} else {
			if (!firePressed) {
				fireHasBeenReleased = true;
			}
			if ((firePressed) && (fireHasBeenReleased) && !soundManager.isPlayingSound()) {
				waitingForKeyPress = false;
				fireHasBeenReleased = false;
				startGame();
				soundManager.playSound(SOUND_START);
			}
		}
*/

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

		setTimeDelta();

		// cycle round asking each entity to move itself
		for ( Entity entity : entities ) {
			entity.move(msElapsed);
		}

		// cycle round drawing all the entities we have in the game
		for ( Entity entity : entities ) {
			entity.draw();
		}

		processHits();

		// TODO: This probably needs to change

		// cycle round every entity requesting that
		// their personal logic should be considered.
			for ( Entity entity : entities ) {
				entity.doLogic();
			}

		userInteracations();

		textWindow.draw();
	}

	private void processCommand(String cmd) {
		String CMD = cmd.trim().toUpperCase();

		if (CMD.startsWith("TOR") ) {
			String direction = CMD.substring(4).trim();
			if (direction.isEmpty()) return;
			float angle = Float.valueOf(direction);
			tryToFire(angle);
		}
	}

	/**
	 * @param direction
	 * @return
	 */
	private boolean hasInput(int direction) {
		switch(direction) {
			case Keyboard.KEY_LEFT:
				return
						Keyboard.isKeyDown(Keyboard.KEY_LEFT) ||
								mouseX < 0;

			case Keyboard.KEY_RIGHT:
				return
						Keyboard.isKeyDown(Keyboard.KEY_RIGHT) ||
								mouseX > 0;

			case Keyboard.KEY_SPACE:
				return
						Keyboard.isKeyDown(Keyboard.KEY_SPACE) ||
								Mouse.isButtonDown(0);
		}
		return false;
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
}
