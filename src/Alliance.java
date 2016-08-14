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
	private AIManagement		AI;

	private Galaxy 				galaxy;
	protected int 				galaxySize = 10;

	public static boolean		gameRunning	= true;
	public String 				Command = null;

	protected Entity				playerShip;

	public int					totalEnemy = 0, totalStarbases = 0;

	public int					starDate = Constants.startDate;
	public double				rawStarDate = starDate;

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
		computer.setView(view);
		view.setComputer(computer);
		computer.setSound(sound);

		AI = new AIManagement();
		AI.setComputer(computer);
		AI.setAlliance(this);
		AI.setGalaxy(galaxy);

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
		initGalaxy();

		// Intialise player scan of current sector
		galaxy.getSector((int)playerShip.galacticLoc.x, (int)playerShip.galacticLoc.y).setLRS(playerShip, playerShip.getLRS((int)playerShip.galacticLoc.x, (int)playerShip.galacticLoc.y));	// Scan current sector
	}

	protected void placeEntityInSector(Entity entity, Sector sector) {
		float sectorWidth = view.getViewWidth(Constants.viewSector);
		float sectorHeight = view.getViewHeight(Constants.viewSector);
		int pX, pY;
		float entityW = entity.sprite.getWidth();
		float entityH = entity.sprite.getHeight();

		do {
			pX = (int) (Math.random() * (sectorWidth - 2*entityW) + entityW);
			pY = (int) (Math.random() * (sectorHeight - 2*entityH) + entityH);
			entity.sprite.setLocation(pX, pY, 0);
		} while (sector.inACollision(entity));

		entity.sprite.setRotationAngle(0, 0, (int) (Math.random() * (360)));
		// TODO Set random (slight) motion in current direction - need enemy AI to cope with this...   -- entity.sprite.setMotion(0,0,0);

		int ex = (int)Math.floor(sector.galacticLoc.x);
		int ey = (int)Math.floor(sector.galacticLoc.y);

		entity.setLocation(ex, ey, 0);

//		System.out.println("entity " + entity.eType + " landed at (" + pX + ", " + pY+ ") in sector (" + ex + "," + ey + ")");
	}

	private void initGalaxy() {
		float sectorWidth = view.getViewWidth(Constants.viewSector);
		float sectorHeight = view.getViewHeight(Constants.viewSector);
		float centreX = sectorWidth / 2;
		float centreY = sectorHeight / 2;
		Sector sector;
		Entity newEntity;
		int pX, pY, reps;
		int	gx, gy;

		// scan the galaxy and add stars, enemies, etc.
		for (gx = 0; gx < galaxySize; gx++) {
			for (gy = 0; gy < galaxySize; gy++) {
				sector = galaxy.getSector(gx, gy);

				// STAR first as it goes in the CENTRE of the sector every time
				newEntity = new Entity(Entity.SubType.STAR, Constants.FILE_IMG_STAR);
				newEntity.sprite.setLocation(centreX, centreY, 0);
				newEntity.sprite.setRotationInfluence(0.0f, 0.0f, 10.0f, -1.0f);
				newEntity.energyLevel = Constants.starEnergy.baseEnergy;
				newEntity.energyGrowth = Constants.starEnergy.stdGrowth;
				newEntity.maxEnergy = Constants.starEnergy.maxEnergy;
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
					newEntity = new Entity(Entity.SubType.STARBASE, Constants.FILE_IMG_STARBASE, galaxySize);
					newEntity.sprite.setRotationInfluence(0.0f, 0.0f, (float)(Math.random()*40.0f)-20.0f, -1.0f);
					newEntity.energyLevel = Constants.starbaseEnergy.baseEnergy;
					newEntity.energyGrowth = Constants.starbaseEnergy.stdGrowth;
					newEntity.maxEnergy = Constants.starbaseEnergy.maxEnergy;

					newEntity.maxShield = Constants.shieldEnergy.maxEnergy*100;

					newEntity.torpedoCount = (int)Math.ceil(Constants.starbaseTorpedoes.maxValue * Math.random());
					newEntity.maxTorpedo = (int)Constants.starbaseTorpedoes.maxValue;

					sector.AddEntity(newEntity);
					placeEntityInSector(newEntity, sector);
					totalStarbases++;
				}

				// Add Planet(s)
				for (reps = 0; reps < sector.planetCount; reps++) {
					newEntity = new Entity(Entity.SubType.PLANET, Constants.FILE_IMG_PLANET);
					newEntity.sprite.setRotationInfluence(0.0f, 0.0f, (float)(Math.random()*80.0f)-40.0f, -1.0f);
					newEntity.energyLevel = Constants.planetEnergy.baseEnergy;
					newEntity.maxEnergy = Constants.planetEnergy.maxEnergy;
					newEntity.energyGrowth = Constants.planetEnergy.stdGrowth;
					sector.AddEntity(newEntity);
					placeEntityInSector(newEntity, sector);
				}

				// Add Enemy(s)
				for (reps = 0; reps < sector.enemyCount; reps++) {
					newEntity = new Entity(Entity.SubType.ENEMYSHIP, Constants.FILE_IMG_ROMULAN, galaxySize);
					newEntity.sprite.setRotationInfluence(0.0f, 0.0f, 0.0f, 0.0f);

					// Enemy START with half the energy we have but they gain energy as fast as we do and can grow to twice the power - game gets harder as time goes by!
					newEntity.energyLevel = Constants.shipEnergy.baseEnergy / 2;
					newEntity.maxEnergy = Constants.shipEnergy.maxEnergy * 2;
					newEntity.energyGrowth = Constants.shipEnergy.stdGrowth / 5;	// Perhaps vary this value to set game difficulty levels

					newEntity.maxShield = Constants.shieldEnergy.maxEnergy;

					newEntity.torpedoCount = (int)Constants.shipTorpedoes.initialValue;
					newEntity.maxTorpedo = (int)Constants.shipTorpedoes.maxValue;

					sector.AddEntity(newEntity);
					placeEntityInSector(newEntity, sector);
					totalEnemy++;
				}
			}
		}

		// Add The PLAYER to a SECTOR in the GALAXY
		gx = (int) Math.floor(Math.random() * galaxySize);
		gy = (int) Math.floor(Math.random() * galaxySize);

		playerShip = new Entity(Entity.SubType.FEDERATIONSHIP, Constants.FILE_IMG_ENTERPRISE, galaxySize);

		playerShip.energyLevel = Constants.shipEnergy.baseEnergy;
		playerShip.energyGrowth = Constants.shipEnergy.stdGrowth;
		playerShip.maxEnergy = Constants.shipEnergy.maxEnergy;

		playerShip.maxShield = Constants.shieldEnergy.maxEnergy;

		playerShip.torpedoCount = (int)Constants.shipTorpedoes.initialValue;
		playerShip.maxTorpedo = (int)Constants.shipTorpedoes.maxValue;

		computer.setShip(playerShip);
		view.setShip(playerShip);
		sector = galaxy.getSector(gx, gy);
		sector.AddEntity(playerShip);
		placeEntityInSector(playerShip, sector);
		view.setSector(sector);
		computer.setSector(sector);

		System.out.println("Total Enemy = " + totalEnemy);
		System.out.println("Total Starbases = " + totalStarbases);
	}

	private void hitEnergy(Entity entity, double hitBy) {
		if (entity.shieldsUp) {
			entity.shieldEnergy -= hitBy;
			if (entity.shieldEnergy < 0) {
				hitBy = -entity.shieldEnergy;
				entity.shieldEnergy = 0;
				entity.shieldsUp = false;
			} else hitBy = 0;
		}

		doDamage(entity, hitBy);
		addEnergy( entity, -hitBy);

		int ex = (int)Math.floor(entity.galacticLoc.x);
		int ey = (int)Math.floor(entity.galacticLoc.y);
		view.writeScreen("Enemy at (" + ex + "," + ey + ") by " + computer.getDirection(playerShip, entity) + " has " + (int)entity.energyLevel + " units of energy left");
	}

	private void addEnergy(Entity entity, double deltaE) {
		entity.energyLevel += deltaE;
		if (entity.maxEnergy > 0 && entity.energyLevel > entity.maxEnergy) entity.energyLevel = entity.maxEnergy;
	}

	private void doDamage(Entity entity, double hitBy) {
		//TODO difference between entity.energyLevel and hitBy determines how many random things get damaged by this hit
	}

	private void dockedProcessing(Entity entity, Entity starbase, double secondsElapsed) {

		if (!entity.docked) {
			entity.sprite.setMotion(0, 0, 0);
			entity.sprite.setInfluence(0, 0, 0, 0);
			entity.docked = true;
			entity.dockedWith = starbase;
			entity.dockedTimer = 0;
		}

		entity.dockedTimer += secondsElapsed;	// for things that should only occur once per second

		float dockEnergy = (float)(Constants.shipEnergy.dockedGrowth*secondsElapsed);
		addEnergy(entity, dockEnergy);	// Docked bonus
		addEnergy(starbase, -dockEnergy);

		if (entity.torpedoCount < entity.maxTorpedo && starbase.torpedoCount > 0 && entity.dockedTimer >= 1) {
			entity.torpedoCount++;
			starbase.torpedoCount--;
		}

		if (entity.dockedTimer >= 1) entity.dockedTimer -= 1;
	}

	private void collisionDamage(Entity ca, Entity cb, double secondsElapsed) {

		// Torpedoes do all damage instantly, other collisions take time
		if (ca.eType == Entity.SubType.TORPEDO) {
			hitEnergy(cb, ca.energyLevel);
			ca.energyLevel = 0;
		}
		else if (cb.eType == Entity.SubType.TORPEDO) {
			hitEnergy(ca, cb.energyLevel);
			cb.energyLevel = 0;
		} else {
			double caEnergy = ca.energyLevel;    // pre- hit energy effect calculations
			hitEnergy(ca, cb.energyLevel * secondsElapsed);
			hitEnergy(cb, caEnergy * secondsElapsed);
		}
	}

	private void gameLogic(double secondsElapsed) {
		Sector sector;
		Entity deadEntity = null;
		Sector.CollisionList crashes;

		for (int gx = 0; gx < galaxySize; gx++) {
			for (int gy = 0; gy < galaxySize; gy++) {
				sector = galaxy.getSector(gx, gy);

				// Energy Calcs
				for (Entity entity : sector.getEntities()) {

					addEnergy(entity, (entity.energyGrowth*secondsElapsed));

					if (entity.shieldsUp) addEnergy(entity, -(float)(entity.shieldEnergy * secondsElapsed * Constants.shieldEnergy.runningEnergy) );

					if (entity.energyLevel > entity.sprite.energyConsumption) {
						addEnergy(entity, -entity.sprite.energyConsumption);
						entity.sprite.energyConsumption = 0;
					} else {
						entity.sprite.setInfluence(0,0,0,0);	// out of energy
					}
//					if (entity.eType == Entity.SubType.TORPEDO) System.out.println("Torpedo energy now at " + (int)entity.energyLevel + " after change of " + (int)entity.sprite.energyConsumption);
				}

				crashes = sector.getCollisions();
				for (Sector.CollisionList.Collision c : crashes.collisions) {
					// Docking
					if (c.compareTypes(Entity.SubType.FEDERATIONSHIP, Entity.SubType.STARBASE)) {
						dockedProcessing((c.A.eType == Entity.SubType.FEDERATIONSHIP ? c.A : c.B), (c.A.eType == Entity.SubType.FEDERATIONSHIP ? c.B : c.A), secondsElapsed);
					} else
					if (c.compareTypes(Entity.SubType.ENEMYSHIP, Entity.SubType.STARBASE)) {	// Enemy can dock with our starbases and commandeer supplies/energy
						dockedProcessing((c.A.eType == Entity.SubType.ENEMYSHIP ? c.A : c.B), (c.A.eType == Entity.SubType.ENEMYSHIP ? c.B : c.A), secondsElapsed);
					} else
						collisionDamage(c.A, c.B, secondsElapsed);
				}

				do {
					deadEntity = null;

					// Energy impact of collisions - note that we take a little each way - as long as the FPS rate is high it should be reasonably balanced.
					for (Entity entity : sector.getEntities()) {

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
						System.out.println("Entity " + deadEntity.eType + " ran out of energy - destroyed.");
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
		galaxy.doLogic(secondsElapsed);	// Updates all the entities in the galaxy (movements mostly)
		gameLogic(secondsElapsed);		// Energy and damage processing
		AILogic(secondsElapsed);					// enemy thoughts
	}

	private void AILogic(double secondsElapsed) {
		AI.runAI(secondsElapsed);
	}

	/*****************************************************************
	 * Run the main game loop. This method keeps rendering the scene
	 * and requesting that the callback update its screen.
	 *****************************************************************/
	private void gameLoop() {
//		sound.playSound("Start");

		while (gameRunning) {
			setTimeDelta();

			view.draw((double)msElapsed / 1000.0);

			gameRunning = user.Update();

			Command = user.getCommand();
			if (! Command.isEmpty()) {
				user.clearCommand();
				computer.doCommand(playerShip, Command);
			}

			// move everyone along msElapsed Seconds (fractional part thereof actually)
			updateLogic((double)msElapsed / 1000.0);

			if (totalEnemy <= 0) {
				sound.playSound("Win");
				gameRunning = false;
			}
		}

		view.writeScreen("Game exit in progress...");	// Does not appear :-( TODO Make it work
//		sleep(5000);

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

		rawStarDate += (msElapsed/100);
		starDate = (int)Math.floor(rawStarDate);
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
		// TODO Implement complete parameter handling
		// eg. -gs 10 	for setting the galaxy size dynamically
		System.out.println("Use -fullscreen for fullscreen mode");

		new Alliance((argv.length > 0 && "-fullscreen".equalsIgnoreCase(argv[0]))).execute();
//		new Alliance(true).execute();	// Force fullscreen for testing...

		System.exit(0);
	}

}
