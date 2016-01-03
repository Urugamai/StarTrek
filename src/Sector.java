import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private Game game; // For calling backup up the tree

	protected Sector LeftTop, Top, RightTop, Left, Right, LeftBottom, Bottom, RightBottom;
	protected Sector Previous, Next;
	private int screenWidth = 0, screenHeight = 0;

	private int galacticX, galacticY, galacticZ;

	private static int galacticXMin = 0, galacticXMax = 0, galacticYMin = 0, galacticYMax = 0; // , galacticZMin = 0, galacticZMax = 0;

	public int LRS_EnemyCount = -1;
	public int LRS_StarbaseCount = -1;
	public int LRS_PlanetCount = -1;

	private int sectorWidth, sectorHeight;

	private int enemyCount = 0;
	private int starbaseCount = 0;
	private int planetCount = 0;

	private PlayerShipEntity ship;

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<Entity> removeList = new ArrayList<Entity>();

	public Sector(Game game, int scale, int gx, int gy, Sector LT, Sector T, Sector RT, Sector L, Sector R, Sector LB, Sector B, Sector RB) {
		this.game = game;

		screenWidth = game.getWidth();
		screenHeight = game.getHeight();

		// scale: how far out we are going to build from this sector (sector build is recursive)
		galacticX = gx;	// Where are we
		galacticY = gy;	// where are we
		galacticZ = 0;
		LeftTop = LT; Top = T; RightTop = RT; Left = L; Right = R; LeftBottom = LB; Bottom = B; RightBottom = RB;

		if (gx < galacticXMin) galacticXMin = gx;
		if (gy < galacticYMin) galacticYMin = gy;
		if (gx > galacticXMax) galacticXMax = gx;
		if (gy > galacticYMax) galacticYMax = gy;

		if ( scale > 0 && Top == null) {
			Top = createSector(Constants.jumpDirection.Top, scale-1);
			this.Next = Top;
			Top.Previous = this;
			Top.LeftBottom = Left;
			Top.Bottom = this;
			Top.RightBottom = Right;
			Top.Left = LeftTop;
			Top.Right = RightTop;
		}

		if (scale > 0 && Right == null) {
			Right = createSector(Constants.jumpDirection.Right, scale-1);
			this.Next = Right;
			Right.Previous = this;
			Right.LeftTop = Top;
			Right.Left = this;
			Right.LeftBottom = Bottom;
			Right.Top = RightTop;
			Right.Bottom = RightBottom;
		}

		if (scale > 0 && Bottom == null) {
			Bottom = createSector(Constants.jumpDirection.Bottom, scale-1);
			this.Next = Bottom;
			Bottom.Previous = this;
			Bottom.LeftTop = Left;
			Bottom.Top = this;
			Bottom.RightTop = Right;
			Bottom.Left = LeftBottom;
			Bottom.Right = RightBottom;
		}

		if (scale > 0 && Left == null) {
			Left = createSector(Constants.jumpDirection.Left, scale-1);
			this.Next = Left;
			Left.Previous = this;
			Left.RightTop = Top;
			Left.Right = this;
			Left.RightBottom = Bottom;
			Left.Top = LeftTop;
			Left.Bottom = LeftBottom;
		}

		if (scale > 0 && LeftTop == null) {
			LeftTop = createSector(Constants.jumpDirection.LeftTop, scale-1);
			this.Next = LeftTop;
			LeftTop.Previous = this;
			LeftTop.Right = Top;
			LeftTop.RightBottom = this;
			LeftTop.Bottom = Left;
		}

		if (scale > 0 && LeftBottom == null) {
			LeftBottom = createSector(Constants.jumpDirection.LeftBottom, scale-1);
			this.Next = LeftBottom;
			LeftBottom.Previous = this;
			LeftBottom.Top = Left;
			LeftBottom.RightTop = this;
			LeftBottom.Right = Bottom;
		}

		if (scale > 0 && RightTop == null) {
			RightTop = createSector(Constants.jumpDirection.RightTop, scale-1);
			this.Next = RightTop;
			RightTop.Previous = this;
			RightTop.Left = Top;
			RightTop.LeftBottom = this;
			RightTop.Bottom = Right;
		}

		if (scale > 0 && RightBottom == null) {
			RightBottom = createSector(Constants.jumpDirection.RightBottom, scale-1);
			this.Next = RightBottom;
			RightBottom.Previous = this;
			RightBottom.Top = Right;
			RightBottom.LeftTop = this;
			RightBottom.Left = Bottom;
		}

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

	private Sector createSector(Constants.jumpDirection dir, int distance) {
		Sector newSector = null;
		if 		(dir == Constants.jumpDirection.LeftTop)	newSector = new Sector(game, distance-1, galacticX-1, 	galacticY-1, 	null, 	LeftTop, 	Top, 	null, 		this, 		null, 	LeftBottom,	Bottom );
		else if (dir == Constants.jumpDirection.Left)		newSector = new Sector(game, distance-1, galacticX-1, 	galacticY, 		null,	LeftTop,	Top,	null,		this,		null,	LeftBottom,	Bottom);
		else if (dir == Constants.jumpDirection.LeftBottom)	newSector = new Sector(game, distance-1, galacticX-1,	galacticY+1, 	null, 	LeftTop, 	Top, 	null, 		this, 		null, 	LeftBottom,	Bottom );
		else if (dir == Constants.jumpDirection.Top)		newSector = new Sector(game, distance-1, galacticX, 	galacticY-1, 	null,	null,		null,	LeftTop,	RightTop,	Left, 	this,		Right);
		// Here
		else if (dir == Constants.jumpDirection.Bottom)		newSector = new Sector(game, distance-1, galacticX, 	galacticY+1, 	Left,	this,		Right,	LeftBottom,	RightBottom,null,	null,		null);
		else if (dir == Constants.jumpDirection.RightTop)	newSector = new Sector(game, distance-1, galacticX+1,	galacticY-1,	null,	LeftTop,	Top,	null,		this,		null,	LeftBottom, Bottom );
		else if (dir == Constants.jumpDirection.Right)		newSector = new Sector(game, distance-1, galacticX+1, 	galacticY, 		Top,	RightTop,	null,	this,		null,		Bottom,	RightBottom,null);
		else if (dir == Constants.jumpDirection.RightBottom)newSector = new Sector(game, distance-1, galacticX+1,	galacticY+1,	null,	LeftTop,	Top,	null,		this,		null,	LeftBottom, Bottom );

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

		// Whack the star into the middle of the sector
		newEntity = new StarEntity(game, Constants.FILE_IMG_STAR, screenWidth / 2, screenHeight / 2);
		entities.add(newEntity);

		// whack in a starbase if needed
		newEntity = null;
		for (int i = 0; i < starbaseCount; i++) {
			do {
				if (newEntity != null) {
					newEntity = null;		// dispose of last attempt
				}
				newEntity = new StarbaseEntity(game, Constants.FILE_IMG_STARBASE, (int) (Math.random() * (screenWidth - 50)), (int) (Math.random() * (screenHeight - 50)));
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}

		// Whack in the necessary number of enemy units
		newEntity = null;
		for (int i = 0; i < enemyCount; i++) {
			do {
				if (newEntity != null) {
					newEntity = null;
				} // dispose of dud selection
				newEntity = new RomulanEntity(game, Constants.FILE_IMG_ROMULAN, (int) (Math.random() * (screenWidth - 50)), (int) (Math.random() * (screenHeight - 50)));
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
		return false;
	}

	public boolean takeEntity(Entity entity) {
		return entities.remove(entity);
	}

	public boolean putEntity(Entity entity) {
		if (entity instanceof PlayerShipEntity) ship = (PlayerShipEntity)entity;
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
		if (x < spriteWidth)	{ newSector = jump(Constants.jumpDirection.Left, entity, Left); Left = newSector; x = maxWidth; }
		if (x > maxWidth )		{ newSector = jump(Constants.jumpDirection.Right, entity, Right); Right = newSector; x = spriteWidth; }
		if (y < spriteHeight)	{ newSector = jump(Constants.jumpDirection.Top, entity, Top); Top = newSector; y = maxHeight; }
		if (y > maxHeight)		{ newSector = jump(Constants.jumpDirection.Bottom, entity, Bottom); Bottom = newSector; y = spriteHeight; }

		if (newSector != null) { game.setPlayerSector(newSector); entity.setLocation(x, y, z); }
	}

	private Sector jump(Constants.jumpDirection dir, Entity entity, Sector newSector) {
		int x = entity.getX(), y = entity.getY(), z = entity.getZ();

		if (newSector == null) {
			newSector = createSector(dir, 1);
		}

		newSector.putEntity(entity);
		removeList.add(entity);		// Delay remove as list iterator gets upset about its list being modified

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

