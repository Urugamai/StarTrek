import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private Galaxy galaxy; // For calling backup up the tree

	private int sectorX, sectorY;

	protected static Sector lastNewSector = null;  // There can only be one
	private static int galacticXMin = 0, galacticXMax = 0, galacticYMin = 0, galacticYMax = 0; // , galacticZMin = 0, galacticZMax = 0;

	private int galacticX, galacticY, galacticZ;
//	private int screenWidth = 0, screenHeight = 0;

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

	public Sector(int sectorX, int sectorY) {
		// Counters
		this.sectorX = sectorX;
		this.sectorY = sectorY;

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
//	public int getScreenWidth() { return screenWidth; }
//	public int getScreenHeight() { return screenHeight; }

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
		int xUnits = 0;
		int yUnits = 0;

		Sprite tmpSprite;
		TextureLoader textureLoader = new TextureLoader();		// for loading sprites

		// Whack the star into the middle of the sector
		xUnits = 0;
		yUnits = 0;
		newEntity = new StarEntity(this, Constants.FILE_IMG_STAR, xUnits, yUnits);
		entities.add(newEntity);

		// whack in a starbase if needed
		tmpSprite = new Sprite(textureLoader, Constants.FILE_IMG_STARBASE);
		int spriteXUnits = Constants.Pixels2UnitsX(tmpSprite.getWidth());
		int spriteYUnits = Constants.Pixels2UnitsY(tmpSprite.getHeight());

		for (int i = 0; i < starbaseCount; i++) {
			do {
				newEntity = null;		// dispose of last attempt
				xUnits = (int) (Math.random() * (Constants.sectorSize - 2*spriteXUnits)) - (Constants.sectorCentre - spriteXUnits);
				yUnits = (int) (Math.random() * (Constants.sectorSize - 2*spriteYUnits)) - (Constants.sectorCentre - spriteYUnits);
				newEntity = new StarbaseEntity(tmpSprite, xUnits, yUnits);
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
			tmpSprite = null;	// dispose
		}

		// Whack in the necessary number of enemy units
		tmpSprite = new Sprite(textureLoader, Constants.FILE_IMG_ROMULAN);
		spriteXUnits = (int)Math.ceil(tmpSprite.getWidth() / Constants.sectorXScale);
		spriteYUnits = (int)Math.ceil(tmpSprite.getHeight() / Constants.sectorYScale);

		for (int i = 0; i < enemyCount; i++) {
			do {
				newEntity = null;
				x = (int) (Math.random() * (Constants.sectorSize - 2*spriteXUnits)) - (Constants.sectorCentre - spriteXUnits);
				y = (int) (Math.random() * (Constants.sectorSize - 2*spriteYUnits)) - (Constants.sectorCentre - spriteYUnits);
				newEntity = new RomulanEntity(tmpSprite, x, y);
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}
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

	public boolean tryToFireTorpedo(float direction) {
		if (!ship.fireTorpedo(direction)) return false;	// no torpedoes left

		PhaserEntity shot = new PhaserEntity(this, ship, Constants.FILE_IMG_TORPEDO);
		shot.setImmediateHeading(direction, 0);
		shot.setVelocity(Constants.TORPEDO_SPEED);
		entities.add(shot);
//		game.soundManager.playEffect(game.SOUND_SHOT);
		return true;
	}

	public boolean tryToFirePhaser(float direction, float power) {
		if (! ship.firePhaser(direction, power) ) return false;

		TorpedoEntity shot = new TorpedoEntity(this, ship, Constants.FILE_IMG_PHASER);
		shot.setImmediateHeading(direction, 0);
		shot.energyLevel = power;
		shot.setVelocity(Constants.PHASER_SPEED);
		entities.add(shot);

//		game.soundManager.playEffect(game.SOUND_PHASER_SHOT);
		return true;
	}

	public void draw() {
		// cycle round drawing all the entities we have in the game
		for (Entity entity : entities) {
			entity.draw();
		}
	}

	public void doLogic(double delta, ArrayList<Transaction> transactions) {
		// cycle round every entity doing personal logic and other interactions
		for (Entity entity : entities) {
			entity.doLogic(delta, transactions);
		}
		processCollisions(delta, transactions);
		for (Entity entity : entities) {
			if (entity instanceof ShipEntity)
			if ( ((ShipEntity)entity).IDied() ) {
				entity.entityTransaction(transactions, Transaction.Action.DELETE, 0);
			}
		}
	}

	public void processCollisions(double delta, ArrayList<Transaction> transactions) {	// Non-overlapping scan of all entities against all others
		for (int i = 0; i < entities.size()-1; i++) {
			Entity me = entities.get(i);

			for (int j = i + 1; j < entities.size(); j++) {
				Entity him = entities.get(j);
				if (me.collidesWith(him)){
					me.collidedWith(him, transactions);
					him.collidedWith(me, transactions);
				}
			}
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

	private Transaction getEmptyTransaction() {
		Transaction trans = new Transaction();
		trans.type = Transaction.Type.SECTOR;
		trans.subType = Transaction.SubType.FEDERATIONSECTOR;
		trans.who = this;

		return trans;
	}

	protected void sectorTransaction(ArrayList<Transaction> transactions, Transaction.Action action, Object entity) {
		Transaction trans = getEmptyTransaction();
		trans.action = action;
		trans.what = Transaction.What.ENTITY;
		trans.who = entity;
		transactions.add(trans);
	}

	public void processTransactions(ArrayList<Transaction> transactions) {

		for (Transaction trans : transactions) {
			if (!trans.active) continue;
			if (trans.type == Transaction.Type.SECTOR) {
				//TODO implement Sector transactions
//				if (trans.subType == this.eType) {
				System.err.println("SECTOR: " + trans.type + ", " + trans.subType + ", " + trans.who + ", " + trans.action + ", " + trans.what + ", " + trans.howMuch);
				trans.active = false;
//				}
			}
		}

		for (Entity entity : entities) {
			entity.processTransactions(transactions);
		}
	}
}
