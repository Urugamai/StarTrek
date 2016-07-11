/*
 *
 */

import org.lwjgl.util.vector.Vector3f;

import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Entity {
	public static enum SubType { STAR, PLANET, STARBASE, FEDERATIONSHIP, ENEMYSHIP, TORPEDO };	// What Am I

	public class LRS {
		int enemyCount = -1;
		int starbaseCount = -1;
		int planetCount = -1;
	}
	/* Routine to build the two dimensional array that contains the LRS results of the galaxy
	 * Since these can change while the ship is not within scanning range, we keep the scanned results here
	 */
	public LRS[][] allocate(Class<LRS> c,int row,int column){
		LRS[][] matrix = (LRS[][]) Array.newInstance(c, column, row);
		for (int x = 0; x < column; x++) {
			//matrix[x] = (LRS[]) Array.newInstance(c,column);
			for(int y = 0; y < row; y++) {
				matrix[x][y] = new LRS();		// Allocates and initialises a Sector
			}
		}
		return matrix;
	}

	protected SubType eType;						// What am I
	protected Sprite	sprite;						/** My sprite (graphics, movement, location, etc) */

	protected double AInextThoughtTime = 0;

	public LRS[][] longRangeScan = null;
	private LRS emptyLRS = new LRS();

	public Vector3f galacticLoc;
	public Entity dockedWith = null;
	public boolean docked = false;
	public double dockedTimer = 0;

	// Its all about ME
	protected double	radius2 = 0;
	protected double	energyLevel = 0;			// What do we have now
	protected double 	energyGrowth = 0;			// How much energy can I MAKE per second
	protected double	maxEnergy = 0;				// How big are my battery banks (damage reduces this)

	public boolean 		shieldsUp = true;
	protected double	shieldEnergy = 0;			// What is my shield energy level
	protected double	maxShield = 0;				// How much shield protection can we deliver (damage reduces this)

	protected int		torpedoCount = 0;				// number of torpedoes we are carrying (restock at starbase)
	protected int		maxTorpedo;					// Maximum number of torpedoes we can carry (damage reduces this)

	protected float		damageLevel = 1;			// How undamaged am I (fraction of above GROWTHS that can apply)

	protected Entity(SubType eType, String spriteFile) {
		newEntity(eType, getSprite(spriteFile), 0);
	}

	protected Entity(SubType eType, String spriteFile, int gs) {
		newEntity(eType, getSprite(spriteFile), gs);
	}

	protected Entity(SubType eType, Sprite sprite) {
		newEntity(eType, sprite, 0);
	}

	protected Entity(SubType eType, Sprite sprite, int gs) {
		newEntity(eType, sprite, gs);
	}

	protected void newEntity(SubType eType, Sprite sprite, int gs) {
		this.eType = eType;
		this.sprite = sprite;
		if (gs > 0) longRangeScan = allocate(LRS.class, gs, gs);
		radius2 = Math.pow(Math.max(sprite.getHeight(), sprite.getWidth())/2,2);
	}

	public Sprite getSprite(String ref) {
		return new Sprite(ref);
	}

	public void setLocation(int gx, int gy, int gz) {
		Vector3f gl = new Vector3f(gx, gy, gz);
		setLocation(gl);
	}

	public void setLocation(Vector3f gl) {
		galacticLoc = gl;
	}

	public double addEnergy(double deltaE) {
		double initialEnergy = energyLevel;
		energyLevel += deltaE;
		if (energyLevel > maxEnergy) {
			energyLevel = maxEnergy;
		}
		if (energyLevel < 0) {
			energyLevel = 0;
		}

		return energyLevel - initialEnergy;
	}

	public double addShieldEnergy(double deltaE) {
		double initialEnergy = shieldEnergy;
		shieldEnergy += deltaE;
		if (shieldEnergy > maxShield) {
			shieldEnergy = maxShield;
		}
		if (shieldEnergy < 0) {
			shieldEnergy = 0;
		}

		return shieldEnergy - initialEnergy;
	}

	public LRS getLRS(int gx, int gy) {
		if (longRangeScan == null) return emptyLRS;
		return longRangeScan[gx][gy];
	}

	public void setLRS(int gx, int gy, LRS lrs) {
		longRangeScan[gx][gy].enemyCount = lrs.enemyCount;
		longRangeScan[gx][gy].starbaseCount = lrs.starbaseCount;
		longRangeScan[gx][gy].planetCount = lrs.planetCount;
	}

	/**
	 * Check if this entity collides with another - use radius assuming shields, etc will hit outside the actual shape
	 *
	 * @param other The other entity to check collision against
	 * @return True if the entities collide with each other
	 */
	public boolean collidesWith(Entity other) {
		Vector3f meLocation = this.sprite.getLocation();
		Vector3f himLocation = other.sprite.getLocation();

		double separation2 = Math.pow(meLocation.x-himLocation.x,2)+Math.pow(meLocation.y-himLocation.y,2)+0; // Math.pow(meLocation.z-himLocation.z,2))

		// Circle collision detection - radius is half of the max of height or width of image, Z-coord ignored (for now?)
		return ( (this.radius2	+ other.radius2) > (separation2) );
	}

	//public void setCollidedWith(Entity other) { collidedWith = other; }

	public void doLogic(double secondsElapsed) {
		sprite.doLogic(secondsElapsed);
		if (shieldEnergy <= 0) shieldsUp = false;
		if (shieldEnergy > 0) shieldsUp = true;
	}
}
