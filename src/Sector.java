import org.lwjgl.Sys;
import org.omg.CORBA.SystemException;

import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	public int enemyCount = 0;
	public int starbaseCount = 0;
	public int planetCount = 0;

	protected ArrayList<Entity> entities = new ArrayList<Entity>();

	public Sector() {
	}

	public void AddEntity(Entity entity) {
		entities.add(entity);
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
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

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public void setLRS(Entity.LRS lrs) {
		lrs.enemyCount = enemyCount;
		lrs.starbaseCount = starbaseCount;
		lrs.planetCount = planetCount;
	}

	public void doLogic(double secondsElapsed) {
		// cycle round every entity doing personal logic and other interactions
		for (Entity entity : entities) {
			entity.doLogic(secondsElapsed);
			entity.setCollidedWith(findCollision(entity));
		}
	}

	public Entity findCollision(Entity me) {
		for (Entity him : entities) {
			if (me == him) continue;
			if (me.collidesWith(him)) return him;
		}
		return null;
	}
}
