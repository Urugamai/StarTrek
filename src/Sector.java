import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private Galaxy galaxy; // For calling backup up the tree

	protected static Sector lastNewSector = null;  // There can only be one
	private static int galacticXMin = 0, galacticXMax = 0, galacticYMin = 0, galacticYMax = 0; // , galacticZMin = 0, galacticZMax = 0;

	private int galacticX, galacticY, galacticZ;
	private int screenWidth = 0, screenHeight = 0;

	public int LRS_EnemyCount = -1;
	public int LRS_StarbaseCount = -1;
	public int LRS_PlanetCount = -1;

	private int enemyCount = 0;
	private int starbaseCount = 0;
	private int planetCount = 0;

	public static PlayerShipEntity ship;

	protected ArrayList<Entity> entities = new ArrayList<Entity>();

	private ArrayList<Entity> removeList = new ArrayList<Entity>();
	private ArrayList<Entity> addList = new ArrayList<Entity>();

	public Sector(Galaxy theGalaxy, int gx, int gy, int gz, int screenWidth, int screenHeight) {
		galaxy = theGalaxy;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;

		// scale: how far out we are going to build from this sector (sector build is recursive)
		galacticX = gx;	// Where are we
		galacticY = gy;	// where are we
		galacticZ = gz;

		if (gx < galacticXMin) galacticXMin = gx;
		if (gy < galacticYMin) galacticYMin = gy;
		if (gx > galacticXMax) galacticXMax = gx;
		if (gy > galacticYMax) galacticYMax = gy;

		// Counters
		newEnemyCount();
		starbaseCount = Math.random() < Constants.starbaseProbability ? 1 : 0;
		planetCount = (int) (Math.random() * (Constants.maxPlanets + 1));

		initEntities();
	}

	public int newEnemyCount() {
		enemyCount += Math.random() < 0.7 ? (int) (Math.random() * (Constants.maxEnemy - enemyCount + 1)) : 0;
		return enemyCount;
	}

	private Galaxy.locationSpec whatSectorIs(Constants.sectorDirection dir) {
		int gx, gy, gz = galacticZ;

		if 		(dir == Constants.sectorDirection.LeftTop)	{gx = galacticX-1;	gy = galacticY-1;}
		else if (dir == Constants.sectorDirection.Left)		{gx = galacticX-1;	gy = galacticY;}
		else if (dir == Constants.sectorDirection.LeftBottom)	{gx = galacticX-1;	gy = galacticY+1;}
		else if (dir == Constants.sectorDirection.Top)		{gx = galacticX;	gy = galacticY-1;}
		// Here
		else if (dir == Constants.sectorDirection.Bottom)		{gx = galacticX;	gy = galacticY+1;}
		else if (dir == Constants.sectorDirection.RightTop)	{gx = galacticX+1;	gy = galacticY-1;}
		else if (dir == Constants.sectorDirection.Right)		{gx = galacticX+1;	gy = galacticY;}
		else if (dir == Constants.sectorDirection.RightBottom){gx = galacticX+1;	gy = galacticY+1;}
		else return null;

		Galaxy.locationSpec jumpTo = new Galaxy.locationSpec(gx, gy, gz);
		jumpTo.setGx(gx);
		jumpTo.setGy(gy);
		jumpTo.setGz(gz);

		return jumpTo;
	}

	public int getGalacticX() { return galacticX; }
	public int getGalacticY() { return galacticY; }
	public int getGalacticZ() { return galacticZ; }
	public int getScreenWidth() { return screenWidth; }
	public int getScreenHeight() { return screenHeight; }

	public int getGalacticXMin() { return galacticXMin; }

	public int getEnemyCount() {
		return enemyCount;
	}
	public int getStarbaseCount() {
		return starbaseCount;
	}
	public int getPlanetCount() {
		return planetCount;
	}

	public Galaxy.locationSpec getPlayerLocation() {
		Galaxy.locationSpec loc = new Galaxy.locationSpec(ship.getX(), ship.getY(), ship.getZ());

		return loc;
	}
	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entity will be added to the overall list of entities in the game.
	 */
	public void initEntities() {
		Entity newEntity = null;
		int x = screenWidth / 2;
		int y = screenHeight / 2;

		// Whack the star into the middle of the sector
		newEntity = new StarEntity(this, Constants.FILE_IMG_STAR, x, y);
		entities.add(newEntity);

		// whack in a starbase if needed
		for (int i = 0; i < starbaseCount; i++) {
			do {
				newEntity = null;		// dispose of last attempt
				x = (int) (Math.random() * (screenWidth - 50));
				y = (int) (Math.random() * (screenHeight - 50));
				newEntity = new StarbaseEntity(this, Constants.FILE_IMG_STARBASE, x, y);
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}

		// Whack in the necessary number of enemy units
		for (int i = 0; i < enemyCount; i++) {
			do {
				newEntity = null;
				x = (int) (Math.random() * (screenWidth - 50));
				y = (int) (Math.random() * (screenHeight - 50));
				newEntity = new RomulanEntity(this, Constants.FILE_IMG_ROMULAN, x, y);
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}
	}

	public PlayerShipEntity initPlayerShip() {
		Entity newEntity = null;
		int x;
		int y;

		// Whack in the player ship - try desired location first
		do {
			x = (int) (Math.random() * (screenWidth - 50));
			y = (int) (Math.random() * (screenHeight - 50));
			if (newEntity != null) {
				newEntity = null;	// dispose of current entity
			}
			newEntity = new PlayerShipEntity(this, Constants.FILE_IMG_ENTERPRISE, x, y);
		} while (checkEntityForOverlap(newEntity));
		entities.add(newEntity);
		ship = (PlayerShipEntity) newEntity;

		return ship;
	}

	public void doSRS() {
		LRS_EnemyCount = enemyCount;
		LRS_StarbaseCount = starbaseCount;
		LRS_PlanetCount = planetCount;
	}

	private void doLRSfor(Sector sector) {
		if (sector == null) return;

		sector.LRS_PlanetCount = sector.planetCount;
		sector.LRS_StarbaseCount = sector.starbaseCount;
		sector.LRS_EnemyCount = sector.enemyCount;
	}

	private boolean checkEntityForOverlap(Entity me) {
		for (Entity entity : entities) {
			if (me == entity) continue;
			if (me.collidesWith(entity)) return true;
		}
		return false;	// No entities overlap me
	}

	public void queueEntity(Constants.listType listType, Entity entity) {
		if (listType == Constants.listType.add) {
			addList.add(entity);        // Delay remove as list iterator gets upset about its list being modified
		} else if (listType == Constants.listType.remove) {
			removeList.add(entity);
		}
	}

	public boolean takeEntity(Entity entity) {
		return entities.remove(entity);
	}

	public boolean putEntity(Entity entity) {
		return entities.add(entity);
	}

	/**
	 * Notification that the player has died.
	 */
	public void notifyDeath() {
//		game.soundManager.playSound(game.SOUND_LOOSE);
	}

	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
//		game.soundManager.playSound(game.SOUND_WIN);
	}

	/**
	 * Notification that an alien has been killed
	 */
	public void notifyAlienKilled() {
		// if there are still some aliens left then they all need to get faster, so
		// speed up all the existing aliens

//		game.soundManager.playEffect(game.SOUND_HIT);
	}

	public void setShipHeading(float direction, float inclination) {
		ship.setHeading(direction, inclination);
	}

	public float getShipHeading() {
		return ship.getHeading();
	}

	public void setShipThrust(float accel, float duration) {
		ship.setThrust(accel, duration);
	}

	public void setShipVelocity( float velocity) { ship.setVelocity( velocity ); }

	public float getShipVelocity( ) { return ship.getVelocity(  ); }

	public void setShipWarp(float warpSpeed, float duration) {
		ship.setWarp(warpSpeed, duration);
	}

	public boolean tryToFire(float direction) {
		if (!ship.fireTorpedo(direction)) return false;	// no torpedoes left

		TorpedoEntity shot = new TorpedoEntity(this, ship, Constants.FILE_IMG_TORPEDO);
		shot.setImmediateHeading(direction, 0);
		shot.setVelocity(Constants.torpedoSpeed);
		entities.add(shot);
//		game.soundManager.playEffect(game.SOUND_SHOT);
		return true;
	}

	// TODO processHits needs to be a LOT smarter
	// Identify source and target objects to determine type of hit involved.

	public void processHits(Entity me) {
		// brute force collisions, compare me against
		// every other entity. If any of them collide notify
		// both entities that the collision has occurred
		for (Entity him : entities) {
			if (him == me) continue;    // of course we hit ourselves, so dont check ;-)

			if (me.collidesWith(him)) {
				me.collidedWith(him);
				him.collidedWith(me);
			}
		}
	}

	private boolean leavingSector(Entity entity){
		// Entity location
		int x = entity.getX();
		int y = entity.getY();
		int z = entity.getZ();

		// Entity size
		int spriteWidth = entity.sprite.getWidth();
		int spriteHeight = entity.sprite.getHeight();

		// Sector size
		int maxWidth = screenWidth - spriteWidth;
		int maxHeight = screenHeight - spriteHeight;

		Sector newSector = null;

		// Are we at the border?
		if (x < spriteWidth)	{ newSector = jump(Constants.sectorDirection.Left, entity); entity.setX(maxWidth-1); }
		if (x > maxWidth )		{ newSector = jump(Constants.sectorDirection.Right, entity); entity.setX(spriteWidth+1); }
		if (y < spriteHeight)	{ newSector = jump(Constants.sectorDirection.Top, entity); entity.setY(maxHeight-1); }
		if (y > maxHeight)		{ newSector = jump(Constants.sectorDirection.Bottom, entity); entity.setY(spriteHeight+1); }

		if (newSector != null) {
			if (entity instanceof PlayerShipEntity) {
				galaxy.playerSector = newSector;
			}
			entity.currentSector = newSector;
		}

		return (newSector != null);
	}

	private Sector jump(Constants.sectorDirection dir, Entity entity) {
		Galaxy.locationSpec newLoc = whatSectorIs(dir);

		Sector newSector = galaxy.getSector(newLoc);	// creates if missing

		newSector.queueEntity(Constants.listType.add, entity);
		queueEntity(Constants.listType.remove, entity);

		return newSector;
	}

	private boolean warpJump(Entity entity) {
		if (! entity.doWarpJump()) return false;

		Galaxy.locationSpec newLoc = entity.calculateWarpJump(new Galaxy.locationSpec(galacticX, galacticY, galacticZ) );

		Sector newSector = galaxy.getSector(newLoc.getGx(), newLoc.getGy(), newLoc.getGz());
		newSector.queueEntity(Constants.listType.add, entity);
		queueEntity(Constants.listType.remove, entity);
		if (newSector != null && entity instanceof PlayerShipEntity) {
			galaxy.playerSector = newSector;
		}

		entity.warpJumpDone();
		return true;
	}

	public void draw() {
		// cycle round drawing all the entities we have in the game
		for (Entity entity : entities) {
			entity.draw();
		}
	}

	public void doLogic(double delta) {
		// cycle round every entity doing personal logic and other interactions
		for (Entity entity : entities) {
			entity.doLogic(delta);
		}
	}

	public boolean doJumps(double delta){
		for (Entity entity : entities) {
			if (warpJump(entity)) return true;
		}
		return false;
	}

	public boolean checkLeaving() {
		for (Entity entity : entities) {
			if (leavingSector(entity)) return true;	// concurrent update errors means we must exit and restart sector list processing
		}
		return false;
	}

	public void checkHits() {
		for (Entity entity : entities) {
			processHits(entity);
		}
	}

	public void doAdd() {
		// Add any entities that are meant to be in this sector now
		if (!addList.isEmpty()) {
			for (Entity entity : addList)
				putEntity(entity);

			addList.clear();
		}
	}

	public void doRemove() {
		// if we have entities that have left, delete them.
		if (!removeList.isEmpty()) {
			for (Entity entity : removeList)
				takeEntity(entity);

			removeList.clear();
		}
	}
}

