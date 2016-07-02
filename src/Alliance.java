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
	protected int 				galaxySize = 10;

	public static boolean		gameRunning	= true;
	public String 				Command = null;

	private Entity				playerShip;
	protected int				playerGalacticX, playerGalacticY;

	public int					totalEnemy = 0, totalStarbases = 0;

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
		view.setAlliance(this);

		galaxy = new Galaxy(galaxySize, galaxySize);

		sound = new SoundManagement();
		sound.loadSound("Torpedo", Constants.FILE_SND_TORPEDO);
		sound.loadSound("Hit", Constants.FILE_SND_HIT);
		sound.loadSound("Start", Constants.FILE_SND_START);
		sound.loadSound("Win", Constants.FILE_SND_WIN);
		sound.loadSound("Lose", Constants.FILE_SND_LOSE);

		view.setGalaxy(galaxy);

		user = new UserManagement();
		user.setAlliance(this);
		user.setGalaxy(galaxy);
		view.setUser(user);

		computer = new ComputerManagement();
		computer.setAlliance(this);
		computer.setGalaxy(galaxy);
		view.setComputer(computer);
		computer.setSound(sound);

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
		ArrayList<Entity> entities;

		// Add The PLAYER to a SECTOR in the GALAXY
		playerGalacticX = (int) Math.floor(Math.random() * galaxySize);
		playerGalacticY = (int) Math.floor(Math.random() * galaxySize);

		playerShip = new Entity(Entity.SubType.FEDERATIONSHIP, Constants.FILE_IMG_ENTERPRISE, galaxySize);
		computer.setShip(playerShip);
		view.setShip(playerShip);
		playerShip.torpedoCount = Constants.maxTorpedoes;
		playerShip.energyLevel = 10000;

		sector = galaxy.getSector(playerGalacticX, playerGalacticY);
		sector.AddEntity(playerShip);
		view.setSector(sector);
		computer.setSector(sector);

		float sectorWidth = view.getViewWidth(Constants.viewSector);
		float sectorHeight = view.getViewHeight(Constants.viewSector);

		int pX, pY;
		float entityW = playerShip.sprite.getWidth();
		float entityH = playerShip.sprite.getHeight();

		do {
			pX = (int) (Math.random() * (sectorWidth - 2*entityW) + entityW);
			pY = (int) (Math.random() * (sectorHeight - 2*entityH) + entityH);
			playerShip.sprite.setLocation(pX, pY, 0);
		} while (sector.findCollision(playerShip) != null);
		System.out.println("player landed at " + pX + ", " + pY);

		initGalaxy();

		// Intialise player scan of current sector
		galaxy.getSector(playerGalacticX, playerGalacticY).setLRS(playerShip.getLRS(playerGalacticX, playerGalacticY));	// Scan current sector
	}

	private void initGalaxy() {
		float sectorWidth = view.getViewWidth(Constants.viewSector);
		float sectorHeight = view.getViewHeight(Constants.viewSector);
		float centreX = sectorWidth / 2;
		float centreY = sectorHeight / 2;
		float entityW;
		float entityH;
		Sector sector;
		Entity newEntity;
		int pX, pY, reps;

		// scan the galaxy and add stars, enemies, etc.
		for (int gx = 0; gx < galaxySize; gx++) {
			for (int gy = 0; gy < galaxySize; gy++) {
				sector = galaxy.getSector(gx, gy);

				newEntity = new Entity(Entity.SubType.STAR, Constants.FILE_IMG_STAR);
				newEntity.sprite.setLocation(centreX,centreY,0);
				newEntity.sprite.setRotationInfluence(0.0f, 0.0f, 10.0f, -1.0f);
				newEntity.energyLevel = 1000000000;
				sector.AddEntity(newEntity);

				sector.starbaseCount = Math.random() < Constants.starbaseProbability ? 1 : 0;
				sector.planetCount = (int) (Math.random() * (Constants.maxPlanets + 1));
				// If there is a starbase then chances of enemy AND enemy count should be low
				// The increase in planets should reduce the enemy count
				sector.enemyCount =
						sector.starbaseCount > 0 ?	Math.random() < 0.1 ? (int) (Math.random() * (Constants.maxEnemy / 3.0 + 1)) : 0
						: sector.planetCount > 0 ?	Math.random() > (sector.planetCount/(Constants.maxPlanets + 1)) ? (int) (Math.random() * (Constants.maxEnemy / sector.planetCount + 1)) : 0
						: 							Math.random() < 0.8 ? (int) (Math.random() * (Constants.maxEnemy + 1)) : 0
						;

				// Add Starbase (Maximum 1)
				if (sector.starbaseCount > 0) {
					newEntity = new Entity(Entity.SubType.STARBASE, Constants.FILE_IMG_STARBASE);
					entityW = newEntity.sprite.getWidth();
					entityH = newEntity.sprite.getHeight();
					do {
						pX = (int) (Math.random() * (sectorWidth - 2*entityW) + entityW);
						pY = (int) (Math.random() * (sectorHeight - 2*entityH) + entityH);
						newEntity.sprite.setLocation(pX, pY, 0);
					} while (sector.findCollision(newEntity) != null);
					newEntity.sprite.setLocation(pX,pY,0);
					newEntity.sprite.setRotationInfluence(0.0f, 0.0f, (float)(Math.random()*40.0f)-20.0f, -1.0f);
					newEntity.energyLevel = 1000000;
					sector.AddEntity(newEntity);
					totalStarbases++;
				}

				// Add Planet(s)
				for (reps = 0; reps < sector.planetCount; reps++) {
					newEntity = new Entity(Entity.SubType.PLANET, Constants.FILE_IMG_PLANET);
					entityW = newEntity.sprite.getWidth();
					entityH = newEntity.sprite.getHeight();
					do {
						pX = (int) (Math.random() * (sectorWidth - 2*entityW) + entityW);
						pY = (int) (Math.random() * (sectorHeight - 2*entityH) + entityH);
						newEntity.sprite.setLocation(pX, pY, 0);
					} while (sector.findCollision(newEntity) != null);
					newEntity.sprite.setLocation(pX,pY,0);
					newEntity.sprite.setRotationInfluence(0.0f, 0.0f, (float)(Math.random()*80.0f)-40.0f, -1.0f);
					newEntity.energyLevel = 100000000;
					sector.AddEntity(newEntity);
				}

				// Add Enemy(s)
				for (reps = 0; reps < sector.enemyCount; reps++) {
					newEntity = new Entity(Entity.SubType.ENEMYSHIP, Constants.FILE_IMG_ROMULAN);
					entityW = newEntity.sprite.getWidth();
					entityH = newEntity.sprite.getHeight();
					do {
						pX = (int) (Math.random() * (sectorWidth - 2*entityW) + entityW);
						pY = (int) (Math.random() * (sectorHeight - 2*entityH) + entityH);
						newEntity.sprite.setLocation(pX, pY, 0);
					} while (sector.findCollision(newEntity) != null);
					newEntity.sprite.setLocation(pX,pY,0);
					newEntity.sprite.setRotationInfluence(0.0f, 0.0f, 0.0f, 0.0f);
					newEntity.energyLevel = 10000;
					sector.AddEntity(newEntity);
					totalEnemy++;
				}
			}
		}

		System.out.println("Total Enemy = " + totalEnemy);
		System.out.println("Total Starbases = " + totalStarbases);
	}

	private void gameLogic(double secondsElapsed) {
		Sector sector;
		Entity deadEntity = null;

		for (int gx = 0; gx < galaxySize; gx++) {
			for (int gy = 0; gy < galaxySize; gy++) {
				sector = galaxy.getSector(gx, gy);

				do {
					deadEntity = null;

					// Energy impact
					for (Entity entity : sector.getEntities()) {
						if (entity.collidedWith != null) {
							if (entity.eType == Entity.SubType.TORPEDO) {
								entity.collidedWith.energyLevel -= entity.energyLevel;
								entity.energyLevel = 0; // torpedo dies instantly
							} else if (entity.collidedWith.eType == Entity.SubType.STARBASE) {
								if (!playerShip.docked) {
									playerShip.sprite.setMotion(0, 0, 0);
									playerShip.sprite.setInfluence(0, 0, 0, 0);
									playerShip.docked = true;
								}
							} else {
								entity.energyLevel -= (entity.collidedWith.energyLevel * secondsElapsed);
							}
						}

						// Are we dead yet?
						if (entity.energyLevel <= 0) {
							deadEntity = entity;
							break;
						}
						if (deadEntity != null) break;
					}
					if (deadEntity != null) {
						if (deadEntity.eType == Entity.SubType.ENEMYSHIP) sound.playSound("Hit");
						if (deadEntity == playerShip) {
							gameRunning = false;
							sound.playSound("Lose");
						}
						sector.removeEntity(deadEntity);
						if (deadEntity.eType == Entity.SubType.ENEMYSHIP) { totalEnemy--; sector.enemyCount--; }
						if (deadEntity.eType == Entity.SubType.STARBASE) { totalStarbases--; sector.starbaseCount--; }
						if (deadEntity.eType == Entity.SubType.PLANET) { sector.planetCount--; }
					}
				} while (deadEntity != null);
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
		gameLogic(secondsElapsed);
	}

	/*****************************************************************
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 *****************************************************************/
	private void gameLoop() {
//		sound.playSound("Start");

		while (gameRunning) {
			setTimeDelta();

			// TODO: move everyone along msElapsed milliSeconds
			updateLogic((double)msElapsed / 1000.0);

			view.draw((double)msElapsed / 1000.0);

			if (gameRunning) gameRunning = user.Update();

			Command = user.getCommand();
			if (! Command.isEmpty()) {
				user.clearCommand();
				computer.doCommand(Command);
			}

			if (totalEnemy <= 0) {
				sound.playSound("Win");
				gameRunning = false;
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
//		new Alliance(true).execute();	// Force fullscreen for testing...

		System.exit(0);
	}

}
