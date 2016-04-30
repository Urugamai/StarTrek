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

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Alliance {

	private Galaxy 				galaxy;
	private PlayerShipEntity	playerShip;
	private Sector				currentSector;
	private float				starDate 			= 12345;
	private ArrayList<Transaction> transactions;

	private int screenWidth = 1024;
	private int screenHeight = 768;

	private int sectorWindowTop, sectorWindowLeft, sectorWindowBottom, sectorWindowRight;
	private int statusWindowTop, statusWindowLeft, statusWindowBottom, statusWindowRight;
	private int messageWindowTop, messageWindowLeft, messageWindowBottom, messageWindowRight;

	private long				msElapsed;

	private TextureLoader		textureLoader;

	private GameText 			textWindow;
	private GameText			helpWindow;
	private StatusDisplay		statusDisplay;
	private String				userInput 			= "";
	private ArrayList<String>	userInputHistory	= new ArrayList<>();
	private int					historyLines = 0, historyPosition = 0;
	private boolean				returnDown;
	private boolean				inKeyUp = false, inKeyDown = false;

	private Constants.DisplayMode displayMode 		= Constants.DisplayMode.DISPLAY_SECTOR;

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
	public Alliance(boolean fullscreen) {
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

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	/**
	 * Sleep for a fixed number of milliseconds.
	 *
	 * @param duration The amount of time in milliseconds to sleep for
	 */
	public static void sleep(long duration) {
		try {
			Thread.sleep((duration * timerTicksPerSecond) / 1000);
		} catch (InterruptedException ex) {
		}
	}

	/**
	 * Intialise the common elements for the game
	 */
	public void initialize() {

		/*
		 * initialise the Full Game window
		 */
		displayMode = Constants.DisplayMode.DISPLAY_SECTOR;
		try {
			setDisplayMode();
			Display.setTitle(Constants.WINDOW_TITLE);
			Display.setFullscreen(fullscreen);
			Display.create();

			// grab the mouse, don't want a cursor when we're playing!
			if (isApplication) {
				Mouse.setGrabbed(true);
			}

			// enable textures since we're going to use these for our sprites
			glEnable(GL_TEXTURE_2D);
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

			// disable the OpenGL depth test since we're rendering 2D graphics
			glDisable(GL_DEPTH_TEST);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();

			glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glViewport(0, 0, screenWidth, screenHeight);

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
			System.out.println("Alliance exiting - exception in initialization:");
			le.printStackTrace();
			Alliance.gameRunning = false;
			return;
		}

		textureLoader = new TextureLoader();		// for loading sprites

		// Text block at bottom of the screen
		textWindow = new GameText( 0, screenHeight, 5);
		textWindow.setTextColour( org.newdawn.slick.Color.green);
		textWindow.write( "Establishing the Galaxy...");
		textWindow.scroll();	// Free up the bottom line for user entry

		/*
		 * Other views
		 */
		// Full screen help window (linked to F1 button)
		helpWindow = new GameText(0, screenHeight, 55);

		transactions = new ArrayList<Transaction>();

		// setup the initial game state
		startAlliance();
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
					updateDimensions( dm[i].getWidth(), dm[i].getHeight() );
				}
			}

			org.lwjgl.util.Display.setDisplayMode(dm, new String[] {
					"screenWidth=" + screenWidth,
					"screenHeight=" + screenHeight,
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

	private void updateDimensions(int newScreenWidth, int newScreenHeight) {
		screenWidth = newScreenWidth;
		screenHeight = newScreenHeight;

		sectorWindowTop = 0;
		sectorWindowLeft = 0;
		sectorWindowBottom = (int)(screenHeight * Constants.fractionSectorWindowHeight);
		sectorWindowRight = (int)(screenWidth * Constants.fractionSectorWindowWidth);

		messageWindowTop = sectorWindowBottom;
		messageWindowLeft = 0;
		messageWindowBottom = screenHeight;
		messageWindowRight = screenWidth;

		statusWindowTop = 0;
		statusWindowLeft = sectorWindowRight;
		statusWindowBottom = sectorWindowBottom;
		statusWindowRight = screenWidth;

	}

	private void startAlliance() {
		galaxy = new Galaxy(10, 10);	// TODO make these a choice the game player can select
		playerShip = new PlayerShipEntity(Constants.FILE_IMG_ENTERPRISE);
	}

	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 * NOT USED at this TOP LEVEL, we should be the one triggering game events ;-)
	 */
	public void updateLogic() {
	}

	/*****************************************************************
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 *****************************************************************/
	private void gameLoop() {
		soundManager.playEffect(SOUND_START);

		//SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
//		Display.sync(60);	// Causes this loop to stop until the next 60th of a second is ready

//		textWindow.write( "There are " + galaxy.getEnemyCount() + " enemy ships currently in Alliance space");
//		textWindow.scroll();
//		textWindow.write( "You currently have " + galaxy.getStarbaseCount() + " starbases available");
//		textWindow.scroll();

		while (Alliance.gameRunning) {
			setTimeDelta();

			userInteractions();

			galaxy.doLogic(msElapsed / 1000.0, transactions);

			galaxy.processTransactions(transactions);

			frameRendering();
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

		if (Keyboard.isKeyDown(Keyboard.KEY_F1)) displayMode = Constants.DisplayMode.HELP_SCREEN;
		else if (Keyboard.isKeyDown(Keyboard.KEY_F2)) displayMode = Constants.DisplayMode.GALACTIC_MAP;
		else if (Keyboard.isKeyDown(Keyboard.KEY_F3)) displayMode = Constants.DisplayMode.DISPLAY_SECTOR;
		else if (Keyboard.isKeyDown(Keyboard.KEY_F4)) displayMode = Constants.DisplayMode.SHIP_STATUS;
		else if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			if (!inKeyUp) {
				userInput = userInputHistory.get(historyPosition--);
				if (historyPosition < 0) historyPosition = 0;
				textWindow.writeLine(0, userInput);
				inKeyUp = true;
			}
		} else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			if (!inKeyDown) {
				historyPosition++;
				if (historyPosition >= userInputHistory.size()) historyPosition--;
				userInput = userInputHistory.get(historyPosition);
				textWindow.writeLine(0, userInput);
				inKeyDown = true;
			}
		} else {
			inKeyUp = false;
			inKeyDown = false;
		}

		char key = getCurrentKey();

		if (key != '\0') {
			// do something with this key
			if (key == 13) {
				userInputHistory.add(userInput);
				historyPosition = userInputHistory.size()-1;
				processCommand(userInput);
				userInput = "";
				textWindow.scroll();
				textWindow.writeLine(0, "Currently in sector (" + currentSector.getGalacticX() + "," + currentSector.getGalacticY() + ")");  // TODO Remove this sector display once the status display does it
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
			Alliance.gameRunning = false;
		}
	}

	/**
	 * Notification that a frame is being rendered. Responsible for
	 * running visual parts of game logic and rendering the scene.
	 */
	public void frameRendering() {
		// clear non-visible screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();

		if (displayMode == Constants.DisplayMode.HELP_SCREEN)
			drawHelp();
		else if (displayMode == Constants.DisplayMode.DISPLAY_SECTOR)
			galaxy.drawSector();
		else if (displayMode == Constants.DisplayMode.GALACTIC_MAP)
			galaxy.drawGalaxy();
		else if (displayMode == Constants.DisplayMode.SHIP_STATUS)
			galaxy.drawShipStatus();	// TODO: Change this to a picture and text showing the current ship status

		textWindow.draw();

		// update window contents	(Switch non-visible and visible frames)
		Display.update();
	}

	private boolean command_TOR(String[] pieces) {
		float angle;
		String direction = "";

		if (pieces.length > 1) {
			direction = pieces[1];
			try {
				angle = Float.valueOf(direction);
			} catch (Exception e) {
				textWindow.writeLine(1, "Syntax Error: Command format should be: TOR,direction");
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			textWindow.writeLine(1, "Syntax Error: Command format should be: TOR,direction");
			return false;  // no firing for you when you get the parameter wrong
		}

//TODO		galaxy.playerFiredTorpedo(angle);
		return true;
	}

	private boolean command_PHA(String[] pieces) {
		float angle = -1;
		float power = 0;

		if (pieces.length > 2) {
			try {
				angle = Float.valueOf(pieces[1]);
				power = Float.valueOf(pieces[2]);
			} catch (Exception e) {
				textWindow.writeLine(1, "Syntax Error: Command format should be: PHA,direction,power");
				return false;  // no firing for you when you get the parameter wrong
			}
		} else {
			textWindow.writeLine(1, "Syntax Error: Command format should be: PHA,direction,power");
			return false;  // no firing for you when you get the parameter wrong
		}

//TODO		galaxy.playerFiredPhaser(angle, power);
		return true;
	}

	private boolean command_IMP(String[] pieces) {
		float angle;
		float force;
		float seconds;
		String direction = "", power = "", duration = "";

		if ( pieces.length > 3 ) {
			direction = pieces[1];
			power = pieces[2];
			duration = pieces[3];

			try {
				angle = Float.valueOf(direction) % 360;
				if (angle < 0) angle += 360;
				force = Float.valueOf(power); if (force > 20) force = 20;	// technically could make the energy requirements exponential and so preclude the need for a limit
				seconds = Float.valueOf(duration);							// Dont need time limit as energy reserves will expire and stop progress anyway
			} catch (Exception e) {
				textWindow.writeLine(1, "Syntax Error: direction and force must be numeric: IMP,direction,accel,duration");
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			textWindow.writeLine(1, "Syntax Error: Command format should be: IMP,direction,accel,duration");
			return false; // no moving for you when you get the parameters wrong
		}


//TODO		galaxy.setPlayerHeading(angle, 0);
//TODO		galaxy.setPlayerThrust( force, seconds);

		textWindow.writeLine(0, "Command Complete: " + pieces[0] + " " + angle + " " + force + " " + seconds);

		return true;
	}

	private boolean command_WARP(String[] pieces) {
		float angle;
		float force;
		float seconds;
		String direction = "", power = "", duration = "";

		if ( pieces.length > 3 ) {
			direction = pieces[1];
			power = pieces[2];
			duration = pieces[3];

			try {
				angle = Float.valueOf(direction) % 360;
				if (angle < 0) angle += 360;
				force = Float.valueOf(power); if (force > 10) force = 10; // again, energy requirements will preclude this limit in the future
				seconds = Float.valueOf(duration);
			} catch (Exception e) {
				textWindow.writeLine(1, "Syntax Error: direction and force must be numeric: WARP,direction,Speed,duration");
				return false; // no moving for you when you get the parameters wrong
			}
		} else {
			textWindow.writeLine(1, "Syntax Error: Command format should be: WARP,direction,Speed,duration");
			return false; // no moving for you when you get the parameters wrong
		}

//TODO		galaxy.setPlayerHeading(angle, 0);
//TODO		galaxy.setPlayerThrust( 0, 6);	// Turn in the desired direction, it takes 6 seconds to do 180 degrees

//TODO		galaxy.setPlayerWarp(force, seconds);

		textWindow.writeLine(0, "Command Complete: " + pieces[0] + " " + angle + " " + force + " " + seconds );

		return true;
	}

	private boolean command_STOP(String[] pieces) {
//TODO		float currentVelocity = galaxy.getPlayerVelocity();
//TODO		if (currentVelocity > 0) {
//TODO			galaxy.setPlayerThrust(-50, currentVelocity / 50.0f + 1);
//TODO		}
		return true;
	}

	protected int compute_angle_between(Galaxy.locationSpec loc1, Galaxy.locationSpec loc2 ) {
		double dx = (loc2.getGx() - loc1.getGx());
		double dy = (loc1.getGy() - loc2.getGy());

		if (-1 < dx && dx < 1) {
			if (dy < 0) return 270;
			else if (dy > 0) return 90;
			else return 0;
		}

		double range = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
		if (-1 < range && range < 1) return 0; // we are colliding anyway

		int newAngleTan = (int)Math.toDegrees( Math.atan(dy/dx) );
		int newAngleSin = (int)Math.toDegrees( Math.asin(dy/range) );
		int newAngleCos = (int)Math.toDegrees( Math.acos(dx/range) );

		if (dx < 0) newAngleTan = 180 + newAngleTan;
		if (newAngleTan < 0) newAngleTan += 360;
//		newAngle += 180;
//		newAngle %= 360;

		return newAngleTan;
	}

	protected int compute_distance_between(Galaxy.locationSpec loc1, Galaxy.locationSpec loc2 ) {
		double dx = loc1.getGx() - loc2.getGx();
		double dy = loc1.getGy() - loc2.getGy();

		return (int)Math.sqrt( Math.pow(dy, 2) + Math.pow(dx, 2) );
	}

	private boolean command_COMP_TARGET(String[] pieces) {
		int currentLine = 1;
//TODO		Galaxy.locationSpec myLoc = galaxy.playerSector.getPlayerLocation();
		Galaxy.locationSpec otherLoc = null;

		if (pieces.length < 3) {
			textWindow.writeLine(1, "Syntax Error: Command format should be: COMP,TGT,[BES]");
			return false; // no computer command
		}

		if (pieces[2].compareToIgnoreCase("E") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (ent instanceof PlayerShipEntity) continue;
//TODO				if (ent instanceof StarbaseEntity) continue;
//TODO				if (ent instanceof StarEntity) continue;

				// Must be an enemy
				if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeLine(currentLine++, ent.eType + " angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
				}
//TODO			}
		}
		else if (pieces[2].compareToIgnoreCase("B") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (! (ent instanceof StarbaseEntity) ) continue;

				// Must be an Starbase
				if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeLine(currentLine++, "Starbase angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
				}
//TODO			}
		}
		else if (pieces[2].compareToIgnoreCase("S") == 0 ) {
//TODO			for (Entity ent : galaxy.playerSector.entities) {
//TODO				if (! (ent instanceof StarEntity) ) continue;

				// Must be a Star
				if (currentLine < 6) {
//TODO					otherLoc = new Galaxy.locationSpec( ent.getX(), ent.getY(), ent.getZ());
//TODO					textWindow.writeLine(currentLine++, "Star angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
				}
//TODO			}
		}
		else { textWindow.writeLine(1, "Error: No such computer command: " + pieces[1]); }

		return true;
	}

	private boolean command_COMP_NAVIGATION(String[] pieces) {
//TODO		Galaxy.locationSpec myLoc = new Galaxy.locationSpec( galaxy.playerSector.getGalacticX(), galaxy.playerSector.getGalacticY(), galaxy.playerSector.getGalacticZ() );
		Galaxy.locationSpec otherLoc = null;
		int enemyCount = 0;

		if (pieces.length < 3) {
			textWindow.writeLine(1, "Syntax Error: Command format should be: COMP,NAV,[BE],{n}");
			return false; // no computer command
		}

		if (pieces[2].compareToIgnoreCase("B") == 0 ) {
//TODO			otherLoc = galaxy.getNearestStarbase(myLoc);
			if (otherLoc == null) {
				textWindow.writeLine(1, "No starbases in known space." );
				return true;
			}
//TODO			textWindow.writeLine(1, "Starbase angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
		}
		else if (pieces[2].compareToIgnoreCase("E") == 0 ) {
			if (pieces.length > 3) {
				try {
				enemyCount = Integer.valueOf(pieces[3]);
				} catch (Exception e) {
					textWindow.writeLine(1, "Syntax Error: direction and force must be numeric: WARP,direction,Speed,duration");
					return false; // no moving for you when you get the parameters wrong
				}
//TODO				otherLoc = galaxy.getNearestEnemyByCount(myLoc, enemyCount);
			} else
//TODO				otherLoc = galaxy.getNearestEnemy(myLoc);

			if (otherLoc == null) {
				textWindow.writeLine(1, "No enemy in known space." );
				return true;
			}
//TODO			textWindow.writeLine(1, "Nearest Enemy angle: "  + compute_angle_between(myLoc, otherLoc) + " range " +  compute_distance_between(myLoc, otherLoc) + "." );
		}

		return true;
	}

	private boolean command_COMP(String[] pieces) {
		if (pieces.length < 2) {
			textWindow.writeLine(1, "Syntax Error: Command format should be: COMP,command,[parameters]");
			return false; // no computer command
		}

		if (pieces[1].compareToIgnoreCase("TGT") == 0 ) { command_COMP_TARGET(pieces); }
		else if (pieces[1].compareToIgnoreCase("NAV") == 0 ) { command_COMP_NAVIGATION(pieces); }
		else { textWindow.writeLine(1, "Error: No such computer command: " + pieces[1]); }

		return true;
	}

	private void command_HELP(String[] pieces) {
		displayMode = Constants.DisplayMode.HELP_SCREEN;

	}

	private void drawHelp() {
		int currentLine = 50;

		helpWindow.writeLine(currentLine--, "F1                          This help screen");
		helpWindow.writeLine(currentLine--, "F2                          Galaxy Display");
		helpWindow.writeLine(currentLine--, "F3                          Sector Display");
		helpWindow.writeLine(currentLine--, "F4                          Ship Status Display");
		currentLine--;
		helpWindow.writeLine(currentLine--, "TOR angle                   Send a torpedo out at the angle (in degrees) provided");
		helpWindow.writeLine(currentLine--, "PHA angle power             Fire phasers along indicated angle starting with indicated power. Power drops by 1 for each 1 unit of range.");
		helpWindow.writeLine(currentLine--, "IMP angle force duration    Turn ship towards angle while applying force for indicated number of seconds");
		helpWindow.writeLine(currentLine--, "WARP angle factor duration  Turn ship towards angle then travel at Warp Factor provided for indicated number of seconds");
		helpWindow.writeLine(currentLine--, "                            Warp factor 1 for 1 second will take you one sector.  Higher factors take more energy, travel further and get there faster.");
		helpWindow.writeLine(currentLine--, "STOP                        apply maximum deceleration force until we have stopped moving");
		helpWindow.writeLine(currentLine--, "SRS                         Short range scan, refreshes data about the current sector.");
		helpWindow.writeLine(currentLine--, "LRS                         Long range scan, Gather information from adjoining sectors. This updates Galactic Map.");
		helpWindow.writeLine(currentLine--, "SHUP                        Shields UP");
		helpWindow.writeLine(currentLine--, "SHDOWN                      Shields DOWN");
		helpWindow.writeLine(currentLine--, "EXIT");
		currentLine--;
		helpWindow.writeLine(currentLine--, "COMP command [parameters]   Ask computer to calculate something");
		helpWindow.writeLine(currentLine--, "        NAV [BE]            Computer navigation angle (and range) to any Base or Enemy in current sector");
		helpWindow.writeLine(currentLine--, "        TGT [BES]           Computer targeting information (angle and range) to Base, Enemy or Star in sector.");
		helpWindow.writeLine(currentLine--, " ");

		helpWindow.draw();
	}

	private void processCommand(String cmd) {
		float angle;
		float force;
		float seconds;
		String[] pieces;
		String direction = "", power = "", duration = "";

		pieces = cmd.trim().toUpperCase().split("[ ,\t\\n\\x0B\\f\\r]");

		if (pieces[0].compareToIgnoreCase("HELP") == 0 ) { command_HELP(pieces); }
		else if (pieces[0].compareToIgnoreCase("TOR") == 0 ) { command_TOR(pieces); }
		else if (pieces[0].compareToIgnoreCase("PHA") == 0 ) { command_PHA(pieces); }
		else if (pieces[0].compareToIgnoreCase("IMP") == 0 ) { command_IMP(pieces); }
		else if (pieces[0].compareToIgnoreCase("WARP") == 0 ) { command_WARP(pieces); }
		else if (pieces[0].compareToIgnoreCase("STOP") == 0 ) { command_STOP(pieces); }
		else if (pieces[0].compareToIgnoreCase("COMP") == 0 ) { command_COMP(pieces); }
		else if (pieces[0].compareToIgnoreCase("LRS") == 0 ) { galaxy.doLRS(); }
		else if (pieces[0].compareToIgnoreCase("SRS") == 0 ) { galaxy.doSRS(); }
		else if (pieces[0].compareToIgnoreCase("SHUP") == 0 ) { playerShip.shieldsUp = true; }
		else if (pieces[0].compareToIgnoreCase("SHDOWN") == 0 ) { playerShip.shieldsUp = false; }
		else if (pieces[0].compareToIgnoreCase("EXIT") == 0 ) { Alliance.gameRunning = false; }
		else { textWindow.writeLine(1, "Error: No such command: " + cmd); }
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
		new Alliance((argv.length > 0 && "-fullscreen".equalsIgnoreCase(argv[0]))).execute();
//		new Alliance(true).execute();	// force to full screen

		System.exit(0);
	}

	/**
	 *		Start your engines...
	 */
	public void execute() {
		gameLoop();
	}

}
