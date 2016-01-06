import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private Game game; // For calling backup up the tree

	protected static Sector lastNewSector = null;  // There can only be one

	protected Sector LeftTop, Top, RightTop, Left, Right, LeftBottom, Bottom, RightBottom;
	protected Sector Previous, Next;
	private int sectorWidth = 0, sectorHeight = 0;

	private int galacticX, galacticY, galacticZ;

	private static int galacticXMin = 0, galacticXMax = 0, galacticYMin = 0, galacticYMax = 0; // , galacticZMin = 0, galacticZMax = 0;

	public int LRS_EnemyCount = -1;
	public int LRS_StarbaseCount = -1;
	public int LRS_PlanetCount = -1;

	private int enemyCount = 0;
	private int starbaseCount = 0;
	private int planetCount = 0;

	private static PlayerShipEntity ship;

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<Entity> removeList = new ArrayList<Entity>();

	public Sector(Game game, int scale, int gx, int gy, Sector LT, Sector T, Sector RT, Sector L, Sector R, Sector LB, Sector B, Sector RB) {
		this.game = game;

		sectorWidth = game.getWidth();
		sectorHeight = game.getHeight();

		// scale: how far out we are going to build from this sector (sector build is recursive)
		galacticX = gx;	// Where are we
		galacticY = gy;	// where are we
		galacticZ = 0;
		LeftTop = LT; Top = T; RightTop = RT; Left = L; Right = R; LeftBottom = LB; Bottom = B; RightBottom = RB;
		Previous = lastNewSector; Next = null;
		if (lastNewSector != null) lastNewSector.Next = this;
		lastNewSector = this;

		if (gx < galacticXMin) galacticXMin = gx;
		if (gy < galacticYMin) galacticYMin = gy;
		if (gx > galacticXMax) galacticXMax = gx;
		if (gy > galacticYMax) galacticYMax = gy;

		// Counters
		newEnemyCount();
		starbaseCount = Math.random() < Constants.starbaseProbability ? 1 : 0;
		planetCount = (int) (Math.random() * (Constants.maxPlanets + 1));

		initEntities();

		if (scale > 0) {

			// Surround us with valid sectors
			if (Top == null) {
				Top = createSector(Constants.jumpDirection.Top, scale - 1);
			}

			if (Right == null) {
				Right = createSector(Constants.jumpDirection.Right, scale - 1);
			}

			if (Bottom == null) {
				Bottom = createSector(Constants.jumpDirection.Bottom, scale - 1);
			}

			if (Left == null) {
				Left = createSector(Constants.jumpDirection.Left, scale - 1);
			}

			if (LeftTop == null) {
				LeftTop = createSector(Constants.jumpDirection.LeftTop, scale - 1);
			}

			if (LeftBottom == null) {
				LeftBottom = createSector(Constants.jumpDirection.LeftBottom, scale - 1);
			}

			if (RightTop == null) {
				RightTop = createSector(Constants.jumpDirection.RightTop, scale - 1);
			}

			if (RightBottom == null) {
				RightBottom = createSector(Constants.jumpDirection.RightBottom, scale - 1);
			}
		}
	}

	public int newEnemyCount() {
		enemyCount += Math.random() < 0.7 ? (int) (Math.random() * (Constants.maxEnemy - enemyCount + 1)) : 0;
		return enemyCount;
	}

	private Sector createSector(Constants.jumpDirection dir, int scale) {
		Sector newSector = null;
		if 		(dir == Constants.jumpDirection.LeftTop)	{newSector = new Sector(game, scale-1, galacticX-1, 	galacticY-1, 	null, 	null, 		null, 	null, 		Top, 		null, 	Left,		this);}
		else if (dir == Constants.jumpDirection.Left)		{newSector = new Sector(game, scale-1, galacticX-1, 	galacticY, 		null,	LeftTop,	Top,	null,		this,		null,	LeftBottom,	Bottom);}
		else if (dir == Constants.jumpDirection.LeftBottom)	{newSector = new Sector(game, scale-1, galacticX-1,	galacticY+1, 	null, 	Left,	 	this, 	null, 		Bottom,		null, 	null,		null);}
		else if (dir == Constants.jumpDirection.Top)		{newSector = new Sector(game, scale-1, galacticX, 	galacticY-1, 	null,	null,		null,	LeftTop,	RightTop,	Left, 	this,		Right);}
		// Here
		else if (dir == Constants.jumpDirection.Bottom)		{newSector = new Sector(game, scale-1, galacticX, 	galacticY+1, 	Left,	this,		Right,	LeftBottom,	RightBottom,null,	null,		null);}
		else if (dir == Constants.jumpDirection.RightTop)	{newSector = new Sector(game, scale-1, galacticX+1,	galacticY-1,	null,	null,		null,	Top,		null,		this,	Right,	 	null);}
		else if (dir == Constants.jumpDirection.Right)		{newSector = new Sector(game, scale-1, galacticX+1, 	galacticY, 		Top,	RightTop,	null,	this,		null,		Bottom,	RightBottom,null);}
		else if (dir == Constants.jumpDirection.RightBottom){newSector = new Sector(game, scale-1, galacticX+1,	galacticY+1,	this,	Right,		null,	Bottom,		null,		null,	null, 		null);}

		return newSector;
	}

	public int getGalacticX() { return galacticX; }
	public int getGalacticY() { return galacticY; }
	public int getGalacticZ() { return galacticZ; }

	public int getGalacticXMin() { return galacticXMin; }


	public int getSectorWidth() { return sectorWidth; }
	public int getSectorHeight() { return sectorHeight; }

	public int getEnemyCount() {
		return enemyCount;
	}
	public int getStarbaseCount() {
		return starbaseCount;
	}
	public int getPlanetCount() {
		return planetCount;
	}

	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entity will be added to the overall list of entities in the game.
	 */
	public void initEntities() {
		Entity newEntity = null;
		int x = sectorWidth / 2;
		int y = sectorHeight / 2;

		// Whack the star into the middle of the sector
		newEntity = new StarEntity(game, Constants.FILE_IMG_STAR, x, y);
		entities.add(newEntity);

		// whack in a starbase if needed
		for (int i = 0; i < starbaseCount; i++) {
			do {
				newEntity = null;		// dispose of last attempt
				x = (int) (Math.random() * (sectorWidth - 50));
				y = (int) (Math.random() * (sectorHeight - 50));
				newEntity = new StarbaseEntity(game, Constants.FILE_IMG_STARBASE, x, y);
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}

		// Whack in the necessary number of enemy units
		for (int i = 0; i < enemyCount; i++) {
			do {
				newEntity = null;
				x = (int) (Math.random() * (sectorWidth - 50));
				y = (int) (Math.random() * (sectorHeight - 50));
				newEntity = new RomulanEntity(game, Constants.FILE_IMG_ROMULAN, x, y);
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}
	}

	public void initPlayerShip() {
		Entity newEntity = null;
		int x;
		int y;

		// Whack in the player ship - try desired location first
		do {
			x = (int) (Math.random() * (sectorWidth - 50));
			y = (int) (Math.random() * (sectorHeight - 50));
			if (newEntity != null) {
				newEntity = null;	// dispose of current entity
			}
			newEntity = new PlayerShipEntity(game, Constants.FILE_IMG_ENTERPRISE, x, y);
		} while (checkEntityForOverlap(newEntity));
		entities.add(newEntity);
		ship = (PlayerShipEntity) newEntity;
	}

	public void doLRS() {
		if (LeftTop == null) LeftTop = createSector(Constants.jumpDirection.LeftTop, 0);
		if (Top == null) Top = createSector(Constants.jumpDirection.Top, 0);
		if (Left == null) Left = createSector(Constants.jumpDirection.Left, 0);
		if (RightTop == null) RightTop = createSector(Constants.jumpDirection.RightTop, 0);
		if (LeftBottom == null) LeftBottom = createSector(Constants.jumpDirection.LeftBottom, 0);
		if (Bottom == null) Bottom = createSector(Constants.jumpDirection.Bottom, 0);
		if (RightBottom == null) RightBottom = createSector(Constants.jumpDirection.RightBottom, 0);
		if (Right == null) Right = createSector(Constants.jumpDirection.Right, 0);

		doLRSfor(LeftTop);
		doLRSfor(Top);
		doLRSfor(RightTop);
		doLRSfor(Left);
		doLRSfor(this);
		doLRSfor(Right);
		doLRSfor(LeftBottom);
		doLRSfor(Bottom);
		doLRSfor(RightBottom);
	}

	public void doSRS() {
		LRS_EnemyCount = enemyCount;
		LRS_StarbaseCount = starbaseCount;
		LRS_PlanetCount = planetCount;
	}

	private void doLRSfor(Sector sector) {
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

	public void queueRemoveEntity(Entity entity) {
		removeList.add(entity);		// Delay remove as list iterator gets upset about its list being modified
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
		game.soundManager.playSound(game.SOUND_LOOSE);
	}

	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
		game.soundManager.playSound(game.SOUND_WIN);
	}

	/**
	 * Notification that an alien has been killed
	 */
	public void notifyAlienKilled() {
		// if there are still some aliens left then they all need to get faster, so
		// speed up all the existing aliens

		game.soundManager.playEffect(game.SOUND_HIT);
	}

	public void setShipHeading(float direction, float inclination) {
		ship.setHeading(direction, inclination);
	}

	public void setShipThrust(float accel, float duration) {
		ship.setThrust(accel, duration);
	}

	public void setShipVelocity( float velocity) { ship.setVelocity( velocity ); }

	/**
	 * Attempt to fire a shot from the player in the direction provided.
	 * Its called "try"
	 * since we must first check that the player can fire at this
	 * point.
	 */
	public boolean tryToFire(float direction) {
		if (!ship.fireTorpedo(direction)) return false;	// no torpedoes left

		TorpedoEntity shot = new TorpedoEntity(game, this, ship, Constants.FILE_IMG_TORPEDO);
		shot.setImmediateHeading(direction, 0);
		shot.setVelocity(Constants.torpedoSpeed);
		entities.add(shot);
		game.soundManager.playEffect(game.SOUND_SHOT);
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

	private void leavingSector(Entity entity){
		// Entity location
		int x = entity.getX();
		int y = entity.getY();
		int z = entity.getZ();

		// Entity size
		int spriteWidth = entity.sprite.getWidth();
		int spriteHeight = entity.sprite.getHeight();

		// Sector size
		int maxWidth = getSectorWidth() - spriteWidth;
		int maxHeight = getSectorHeight() - spriteHeight;

		Sector newSector = null;

		// Are we at the border?
		if (x < spriteWidth)	{ newSector = jump(Constants.jumpDirection.Left, entity, Left); Left = newSector; x = maxWidth-1; }
		if (x > maxWidth )		{ newSector = jump(Constants.jumpDirection.Right, entity, Right); Right = newSector; x = spriteWidth+1; }
		if (y < spriteHeight)	{ newSector = jump(Constants.jumpDirection.Top, entity, Top); Top = newSector; y = maxHeight-1; }
		if (y > maxHeight)		{ newSector = jump(Constants.jumpDirection.Bottom, entity, Bottom); Bottom = newSector; y = spriteHeight+1; }

		if (newSector != null) { game.setPlayerSector(newSector); entity.setLocation(x, y, z); }
	}

	private Sector jump(Constants.jumpDirection dir, Entity entity, Sector newSector) {
		int x = entity.getX(), y = entity.getY(), z = entity.getZ();

		if (newSector == null) {
			newSector = createSector(dir, 1);
		}

		newSector.putEntity(entity);
		queueRemoveEntity(entity);

		return newSector;
	}

	public void draw() {
		// cycle round drawing all the entities we have in the game
		for (Entity entity : entities) {
			entity.draw();
		}
	}

	public void doLogic(double delta) {
		// cycle round every entity requesting that
		// their personal logic should be considered.
		for (Entity entity : entities) {
			entity.doLogic(delta);
			leavingSector(entity);
			processHits(entity);
		}

		if (!removeList.isEmpty()) {
			for (Entity entity : removeList)
				takeEntity(entity);
		}

		removeList.clear();
	}
}

