import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	private int enemyCount = 0;
	private int starbaseCount = 0;
	private int planetCount = 0;

	protected ArrayList<Entity> entities = new ArrayList<Entity>();

	public Sector(int sectorX, int sectorY) {
		// Counters
		enemyCount = newEnemyCount();
		starbaseCount = Math.random() < Constants.starbaseProbability ? 1 : 0;
		planetCount = (int) (Math.random() * (Constants.maxPlanets + 1));

		initEntities();
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public int newEnemyCount() {
		int e = enemyCount;
		e += Math.random() < 0.7 ? (int) (Math.random() * (Constants.maxEnemy - e + 1)) : 0;
		return e;
	}

	public void AddEntity(Entity entity) {
		entities.add(entity);
	}

	/**
	 * Initialise the starting state of the entities (ship and aliens). Each
	 * entity will be added to the overall list of entities in the game.
	 *
	 * TODO Move this to Alliance.java as it is a GAME component and needs to be managed from there
	 */
	public void initEntities() {
		Entity newEntity = null;
		int xUnits = 0;
		int yUnits = 0;

		int spriteXUnits, spriteYUnits;

		Sprite tmpSprite;

		newEntity = new Entity(Entity.SubType.STAR, Constants.FILE_IMG_STAR);
		newEntity.sprite.setLocation(0,0,0);	// TODO - Move star to middle of viewport - need viewport dimension!
		AddEntity(newEntity);

		// whack in a starbase if needed
		if (starbaseCount > 0) {
			tmpSprite = new Sprite( Constants.FILE_IMG_STARBASE);
			tmpSprite.setRotationAngle(0.1f,0,0);

			for (int i = 0; i < starbaseCount; i++) {
				newEntity = new Entity(Entity.SubType.STARBASE, tmpSprite);
				AddEntity(newEntity);
			}
			tmpSprite = null;    // dispose
		}

		// Whack in the necessary number of enemy units
		if (enemyCount > 0) {
			tmpSprite = new Sprite(Constants.FILE_IMG_ROMULAN);

			for (int i = 0; i < enemyCount; i++) {
				newEntity = new Entity(Entity.SubType.ENEMYSHIP, tmpSprite);
				AddEntity(newEntity);
			}
			tmpSprite = null;    // dispose
		}
	}

	public void doLogic(double secondsElapsed) {
		// cycle round every entity doing personal logic and other interactions
		for (Entity entity : entities) {
			entity.doLogic(secondsElapsed);
		}
		processCollisions(secondsElapsed);
		for (Entity entity : entities) {

		}
	}

	public void processCollisions(double secondsElapsed) {	// Non-overlapping scan of all entities against all others
		for (int i = 0; i < entities.size()-1; i++) {
			Entity me = entities.get(i);

			for (int j = i + 1; j < entities.size(); j++) {
				Entity him = entities.get(j);
				if (me.collidesWith(him)){
					me.collidedWith(him);
					him.collidedWith(me);
				}
			}
		}
	}
}
