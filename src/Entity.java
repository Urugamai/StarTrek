/*
 * Copyright (c) 2002-2010 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

	protected Sprite	sprite;						/** My sprite (graphics, movement, location, etc) */

	public LRS[][] longRangeScan = null;
	private LRS emptyLRS = new LRS();

	public Entity collidedWith = null;
	public boolean docked = false;

	// Its all about ME
	protected double	radius2 = 0;
	protected float		energyLevel = 0;			// What do we have now
	protected float 	energyGrowth = 0;			// How much energy can I MAKE per second
	protected float		maxEnergy = 0;				// How big are my battery banks (damage reduces this)

	protected float		solidity = 0;				// How solid is this entity
	protected float		solidityGrowth = 0;			// How much solidity can I INCREASE per second
	protected float		maxSolidity = 0;			// How solid can I get (damage reduces this)

	protected float		shieldEnergy = 0;			// What is my shield energy level
	protected float		shieldGrowth = 0;			// what is my shield rate of repair per second
	protected float		maxShield = 0;				// How much shield protection can we deliver (damage reduces this)

	protected float		phaserEnergyBank = 0;		// Current contents of phaser energy banks
	protected float		phaserGrowth = 0;			// Rate of Recharge of phaser banks per second
	protected float		maxPhaserEnergy = 0;		// Maximum size of phaser energy bank (damage reduces this)

	protected int		torpedoCount = 0;				// number of torpedoes we are carrying (restock at starbase)
	protected int		maxTorpedo;					// Maximum number of torpedoes we can carry (damage reduces this)

	protected float		damageLevel = 1;			// How undamaged am I (fraction of above GROWTHS that can apply)

	protected SubType eType;			// What am I

	// Hit Box aids
	private Rectangle	me	= new Rectangle();									/** The rectangle used for this entity during collision detection */
	private Rectangle	him	= new Rectangle();									/** The rectangle used for other entities during collision detection */

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
		radius2 = Math.pow(Math.max(sprite.getHeight(), sprite.getWidth()),2);
	}

	public Sprite getSprite(String ref) {
		return new Sprite(ref);
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
	 * Check if this entity collides with another.
	 *
	 * @param other The other entity to check collision against
	 * @return True if the entities collide with each other
	 */
	public boolean collidesWith(Entity other) {
		Vector3f meLocation = this.sprite.getLocation();
		Vector3f himLocation = other.sprite.getLocation();

		double separation2 = Math.pow(meLocation.x-himLocation.x,2)+Math.pow(meLocation.y-himLocation.y,2)+0; // Math.pow(meLocation.z-himLocation.z,2))

		// Circle collision detection - radius is max of height or width of image, Z-coord ignored.
		return ( (this.radius2	+ other.radius2) > (separation2) );
	}

	public void setCollidedWith(Entity other) { collidedWith = other; }

	public void doLogic(double secondsElapsed) {
		sprite.doLogic(secondsElapsed);
		energyLevel -= sprite.energyConsumption; sprite.energyConsumption = 0;
	}
}
