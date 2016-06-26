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


import org.lwjgl.Sys;

import java.util.ArrayList;

public class Alliance {

	// SCREEN Parameters
	private boolean				fullscreen;
	private ViewManagement		view;

	// TIMER Variables
	private long				msElapsed;
	private long				lastLoopTime		= getTime();
	private static long			timerTicksPerSecond	= Sys.getTimerResolution();

	// Game Variables
	private UserManagement		user;
	private SoundManagement		sound;
	private ComputerManagement  computer;

	private Galaxy 				galaxy;
	private int 				galaxySize = 10;

	private static boolean		gameRunning	= true;
	private String 				Command = null;

	private Entity				playerShip;
	private int					playerGalacticX, playerGalacticY;

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
	 * Intialise the common elements for the game
	 */
	public void initialize() {

		view = new ViewManagement(fullscreen);

		galaxy = new Galaxy(galaxySize, galaxySize);

		sound = new SoundManagement();
		sound.loadSound("Torpedo", Constants.FILE_SND_TORPEDO);
		sound.loadSound("Hit", Constants.FILE_SND_HIT);
		sound.loadSound("Start", Constants.FILE_SND_START);
		sound.loadSound("Win", Constants.FILE_SND_WIN);
		sound.loadSound("Lose", Constants.FILE_SND_LOSE);

		view.setGalaxy(galaxy);

		user = new UserManagement();
		user.setGalaxy(galaxy);

		computer = new ComputerManagement();
		computer.setGalaxy(galaxy);

		// setup the initial game state
		startAlliance();
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

	private void startAlliance() {
		Sector sector;

		// Add The PLAYER to a SECTOR in the GALAXY
		playerGalacticX = (int)Math.floor(Math.random()*galaxySize);
		playerGalacticY = (int)Math.floor(Math.random()*galaxySize);

		playerShip = new Entity(Entity.SubType.FEDERATIONSHIP, Constants.FILE_IMG_ENTERPRISE );
		computer.setShip(playerShip);

		galaxy.AddEntity(playerShip, playerGalacticX,	playerGalacticY);

		view.setSector(sector = galaxy.getSector(playerGalacticX, playerGalacticY));

		float centreX = view.getViewWidth(Constants.viewSector) / 2;
		float centreY = view.getViewHeight(Constants.viewSector) / 2;

		ArrayList<Entity> entities = sector.getEntities();
		for (Entity entity : entities) {
			if (entity.eType == Entity.SubType.STAR) {
				entity.sprite.setLocation(centreX, centreY, 0);
			}
		}
	}

	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 * NOT USED at this TOP LEVEL, we should be the one triggering game events ;-)
	 */
	public void updateLogic(double secondsElapsed) {
		galaxy.doLogic(secondsElapsed);	// Updates all the entities in the galaxy
	}

	/*****************************************************************
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 *****************************************************************/
	private void gameLoop() {
		sound.playSound("Start");

		while (gameRunning) {
			setTimeDelta();

			// TODO: move everyone along msElapsed milliSeconds
			updateLogic((double)msElapsed / 1000.0);

			view.draw((double)msElapsed / 1000.0);

			gameRunning = user.Update();
			Command = user.getCommand();
			if (! Command.isEmpty()) {
				user.clearCommand();
				computer.doCommand(Command);
			}
		}

		// clean up
		sound.Destroy();
		view.Destroy();
	}

	private void setTimeDelta() {
		// work out how long its been since the last update, this
		// will be used to calculate how far the entities should
		// move this loop
		long now = getTime();
		msElapsed = now - lastLoopTime;
		lastLoopTime = now;
	}

	/**
	 *		Start your engines...
	 */
	public void execute() {
		gameLoop();
	}

	/**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 *
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		System.out.println("Use -fullscreen for fullscreen mode");

		new Alliance((argv.length > 0 && "-fullscreen".equalsIgnoreCase(argv[0]))).execute();

		System.exit(0);
	}

}
