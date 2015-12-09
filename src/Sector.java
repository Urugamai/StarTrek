import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private Game				game;						/** The game in which this entity exists */
	private int enemyCount = 0;
	private int starbaseCount = 0;
	private float starGravity = 0;
	private int alienCount = 0;

	private PlayerShipEntity	ship;
	private TorpedoEntity[]		shots;
	private int					shotIndex;

	private ArrayList<Entity> entities			= new ArrayList<Entity>();
	private ArrayList<Entity>	removeList			= new ArrayList<Entity>();

	public Sector(Game game, int maxEnemy, float maxGravity, float starbaseProbability) {
		this.game = game;

		enemyCount = Math.random() < 0.4 ? (int)(Math.random()*(maxEnemy+1)) : 0;
		starbaseCount = Math.random() < starbaseProbability ? 1 : 0;
		starGravity = (int)(Math.random()*(maxGravity+1));

		// setup n shots
		shots = new TorpedoEntity[15];
		for (int i = 0; i < shots.length; i++) {
			shots[i] = new TorpedoEntity(this, Constants.FILE_IMG_TORPEDO, 0, 0);
		}
	}

	public Sprite getSprite(String ref) { return game.getSprite(ref); }

	public int getEnemyCount() {
		return enemyCount;
	}

	public int getStarbaseCount() {
		return starbaseCount;
	}

	public float getStarGravity() {
		return starGravity;
	}

	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entity will be added to the overall list of entities in the game.
	 */
	public void initEntities(int width, int height) {
		Entity newEntity = null;

		// Whack the star into the middle of the sector
		newEntity = new StarEntity(game, Constants.FILE_IMG_STAR, width/2, height/2);
		entities.add(newEntity);

		// whack it a starbase if needed
		newEntity = null;
		for (int i = 0; i < starbaseCount; i++) {
			do {
				if (newEntity != null) {newEntity = null; } // dispose of dud selection
				newEntity = new FriendlyEntity(game, Constants.FILE_IMG_STARBASE, (int)(Math.random()*(width-50)), (int)(Math.random()*(height - 50)));
			} while ( checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
		}

		// Whack in the player ship
		newEntity = null;
		do {
			if (newEntity != null) {newEntity = null; } // dispose of dud selection
			newEntity = new PlayerShipEntity(game, Constants.FILE_IMG_ENTERPRISE, (int)(Math.random()*(width-50)), (int)(Math.random()*(height - 50)));
		} while ( checkEntityForOverlap(newEntity));
		entities.add(newEntity);
		ship = (PlayerShipEntity)newEntity;

		// Whack in the necessary number of enemy units
		newEntity = null;
		for (int i = 0; i < enemyCount; i++) {
			do {
				if (newEntity != null) {newEntity = null; } // dispose of dud selection
				newEntity = new EnemyShipEntity(game, Constants.FILE_IMG_ROMULAN, (int)(Math.random()*(width-50)), (int)(Math.random()*(height - 50)));
			} while ( checkEntityForOverlap(newEntity));
			entities.add(newEntity); // this ones a keeper
			alienCount++;
		}
	}

	private boolean checkEntityForOverlap(Entity me) {
		for ( Entity entity : entities) {
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

		game.soundManager.playEffect(game.SOUND_HIT);
	}

	/**
	 * Attempt to fire a shot from the player in the direction provided.
	 * Its called "try"
	 * since we must first check that the player can fire at this
	 * point.
	 */
	public void tryToFire(float direction) {

		// TODO check if a torpedo tube has been [re]loaded and is available to shoot

		TorpedoEntity shot = shots[shotIndex++ % shots.length];
		shot.reinitialize(ship.getX(), ship.getY(), direction);
		entities.add(shot);

		game.soundManager.playEffect(game.SOUND_SHOT);
	}

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

	public void move(long msElapsed) {
		// cycle round asking each entity to move itself
		for ( Entity entity : entities ) {
			entity.move(msElapsed);
		}
	}

	public void draw() {
		// cycle round drawing all the entities we have in the game
		for ( Entity entity : entities ) {
			entity.draw();
		}
	}

	public void doLogic() {
		// TODO: This probably needs to change

		// cycle round every entity requesting that
		// their personal logic should be considered.
		for ( Entity entity : entities ) {
			entity.doLogic();
		}
	}
}
