import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

/**
 * Created by Mark on 8/12/2015.
 */
public class Sector {
	public int enemyCount = 0;
	public int starbaseCount = 0;
	public int planetCount = 0;
	public Vector3f galacticLoc = new Vector3f(-1,-1,0);
	public String name;

	protected ArrayList<Entity> entities = new ArrayList<Entity>();
	CollisionList crashes = new CollisionList();

	public static final class CollisionList {
		ArrayList<Collision> collisions = new ArrayList<>();

		public static final class Collision {
			Entity A, B;

			public Collision(Entity ea, Entity eb) {
				A = ea;
				B = eb;
			}
			public boolean compareTypes(Entity.SubType ta, Entity.SubType tb) {
					if ( (A.eType == ta && B.eType == tb)
							|| (A.eType == tb && B.eType == ta)
							) {
						return true;
					}
				return false;
			}
		}

		public void addCollision(Entity me, Entity him) {
			for (Collision c : this.collisions) {
				if ( (c.A == me && c.B == him)
						|| (c.A == him && c.B == me)
						) return;	// Already have it
			}
			collisions.add(new Collision(me, him));
		}

		public void removeCollision(Collision rm) {
			Collision toBeDeleted = null;
			for (Collision c : collisions) {
				if ( (c.A == rm.A && c.B == rm.B)
						|| (c.A == rm.B && c.B == rm.A)
						) {
					toBeDeleted = c;
					break;
				}
			}
			collisions.remove(toBeDeleted);
		}
	}

	public Sector() {
		galacticLoc.x = -1;
		galacticLoc.y = -1;
		galacticLoc.z = 0;
	}

	public Sector(int gx, int gy) {
		galacticLoc.x = gx;
		galacticLoc.y = gy;
		galacticLoc.z = 0;
	}

	public Sector(int gx, int gy, int gz) {
		galacticLoc.x = gx;
		galacticLoc.y = gy;
		galacticLoc.z = gz;
	}

	public void AddEntity(Entity entity) {
		entities.add(entity);
		entity.setLocation(galacticLoc);
	}

	public void removeEntity(Entity entity) {
		entities.remove(entity);
		entity.setLocation(null);
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public void setLRS(Entity me, Entity.LRS lrs) {
		lrs.enemyCount = 0;
		for (Entity entity : entities) {
			if (entity.eType == Entity.SubType.FEDERATIONSHIP && me.eType == Entity.SubType.ENEMYSHIP) lrs.enemyCount++;
			else if (me.eType == Entity.SubType.FEDERATIONSHIP && entity.eType == Entity.SubType.ENEMYSHIP) lrs.enemyCount++;
		}

		lrs.starbaseCount = starbaseCount;
		lrs.planetCount = planetCount;
	}

	public void doLogic(double secondsElapsed) {
		// cycle round every entity doing personal logic and other interactions
		for (Entity entity : entities) {
			entity.doLogic(secondsElapsed);
			findCollisions(entity);		// Add collisions found
		}
		validateCollisions();			// remove any old collisions no longer happening
	}

	public boolean inACollision(Entity me) {
		for (Entity him : entities) {
			if (me == him) continue;
			if (me.collidesWith(him)) {
				return true;
			}
		}
		return false;	// no collisions in progress
	}

	private void findCollisions(Entity me) {
		for (Entity him : entities) {
			if (me == him) continue;
			if (me.collidesWith(him)) {
				crashes.addCollision(me, him);
			}
		}
	}

	private void validateCollisions() {
		ArrayList<CollisionList.Collision> rmCollisions = new ArrayList<>();

		if ( crashes.collisions.isEmpty()) return;

		for (CollisionList.Collision c : crashes.collisions) {

			if ( !entities.contains(c.A) || !entities.contains(c.B) || !c.A.collidesWith(c.B) ) {
				// Undock if this is a docked pair
				if (c.A.docked && c.A.dockedWith == c.B) {
					c.A.docked = false;
					c.A.dockedWith = null;
				}
				if (c.B.docked && c.B.dockedWith == c.A) {
					c.B.docked = false;
					c.B.dockedWith = null;
				}

				// drop the collision
				rmCollisions.add(c);
			}
		}

		if (rmCollisions.isEmpty()) return;

		for (CollisionList.Collision c : rmCollisions) {
			crashes.removeCollision(c);
		}
		rmCollisions.clear();
	}

	public CollisionList getCollisions() { return crashes; }

}
