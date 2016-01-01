import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private Game game;

	private int sectorWidth, sectorHeight;

	private int enemyCount = 0;
	private int starbaseCount = 0;
	private int planetCount = 0;

	private PlayerShipEntity ship;
	private int shotIndex;

	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private ArrayList<Entity> removeList = new ArrayList<Entity>();

	public Sector(Game game) {
		this.game = game;

		newEnemyCount();

		starbaseCount = Math.random() < Constants.starbaseProbability ? 1 : 0;

		planetCount = (int) (Math.random() * (Constants.maxPlanets + 1));
	}

	public Sprite getSprite(String ref) {
		return game.getSprite(ref);
	}

	public int getSectorWidth() { return sectorWidth; }
	public int getSectorHeight() { return sectorHeight; }

	public int getEnemyCount() {
		return enemyCount;
	}

	public int newEnemyCount() {
		enemyCount += Math.random() < 0.7 ? (int) (Math.random() * (Constants.maxEnemy - enemyCount + 1)) : 0;
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
	public void initEntities(int width, int height) {
		this.sectorWidth = width;
		this.sectorHeight = height;

		Entity newEntity = null;

		// Whack the star into the middle of the sector
		newEntity = new StarEntity(game, Constants.FILE_IMG_STAR, width / 2, height / 2);
		entities.add(newEntity);

		// whack in a starbase if needed
		newEntity = null;
		for (int i = 0; i < starbaseCount; i++) {
			do {
				if (newEntity != null) {
					newEntity = null;		// dispose of last attempt
				}
				newEntity = new StarbaseEntity(game, Constants.FILE_IMG_STARBASE, (int) (Math.random() * (width - 50)), (int) (Math.random() * (height - 50)));
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
				newEntity = new RomulanEntity(game, Constants.FILE_IMG_ROMULAN, (int) (Math.random() * (width - 50)), (int) (Math.random() * (height - 50)));
			} while (checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}
	}

	public void initPlayerShip(int x, int y) {
		Entity newEntity = null;

		// Whack in the player ship - try desired location first
		if (x > 0 && y > 0) {
			newEntity = new PlayerShipEntity(game, Constants.FILE_IMG_ENTERPRISE, x, y);
			if (checkEntityForOverlap(newEntity)) {
				// desired location is occupied
				// TODO: Text message to captain that exit point is occupied, computer override to random location
			} else {
				entities.add(newEntity);
				ship = (PlayerShipEntity) newEntity;

				return;
			}
		}

		do {
			newEntity = new PlayerShipEntity(game, Constants.FILE_IMG_ENTERPRISE, (int) (Math.random() * (sectorWidth - 50)), (int) (Math.random() * (sectorHeight - 50)));
		} while (checkEntityForOverlap(newEntity));
		entities.add(newEntity);
		ship = (PlayerShipEntity) newEntity;
	}

	private boolean checkEntityForOverlap(Entity me) {
		for (Entity entity : entities) {
			if (me == entity) continue;
			if (me.collidesWith(entity)) return true;
		}
		return false;
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

		TorpedoEntity shot = new TorpedoEntity(this, ship, Constants.FILE_IMG_TORPEDO);
		shot.setImmediateHeading(direction, 0);
		shot.setVelocity(Constants.torpedoSpeed);
		entities.add(shot);
		game.soundManager.playEffect(game.SOUND_SHOT);
		return true;
	}

	// TODO processHits needs to be a LOT smarter
	// Identify source and target objects to determine type of hit involved.

	public void processHits() {
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

	public Entity getEntityLeavingSector() {
		for (Entity entity : entities) {
			if (entity.getX() < 0 || entity.getY() < 0 || entity.getX() > sectorWidth || entity.getY() > sectorHeight ) return entity;
		}
		return null;
	}

	public void draw() {
		// cycle round drawing all the entities we have in the game
		for (Entity entity : entities) {
			entity.draw();
		}
	}

	public void doLogic(double delta) {
		// TODO: This probably needs to change

		// cycle round every entity requesting that
		// their personal logic should be considered.
		for (Entity entity : entities) {
			entity.doLogic(delta);
		}
	}
}

