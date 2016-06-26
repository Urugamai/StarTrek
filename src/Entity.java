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
import java.util.ArrayList;

public class Entity {
	public static enum SubType { STAR, STARBASE, FEDERATIONSHIP, ENEMYSHIP, TORPEDO
	};	// What Am I

	protected Sprite	sprite;						/** My sprite (graphics, movement, location, etc) */

	// Its all about ME
	protected float		energyLevel = 0;			// What do we have now
	protected float 	energyGrowth = 0;			// How much energy can I MAKE per second
	protected float		maxEnergy = 0;				// How big are my battery banks

	protected float		solidity = 0;				// How solid is this entity
	protected float		solidityGrowth = 0;			// How much solidity can I INCREASE per second
	protected float		maxSolidity = 0;			// How solid can I get

	protected float		shieldEnergy = 0;			// What is my shield energy level
	protected float		shieldGrowth = 0;			// what is my shield rate of repair per second
	protected float		maxShield = 0;				// How much shield protection can we deliver

	protected float		phaserEnergyBank = 0;		// Current contents of phaser energy banks
	protected float		phaserGrowth = 0;			// Rate of Recharge of phaser banks per second
	protected float		maxPhaserEnergy = 0;		// Maximum size of phaser energy bank

	protected int		torpedoCount;				// number of torpedoes we are carrying (restock at starbase)
	protected int		maxTorpedo;					// Maximum number of torpedoes we can carry

	protected float		damageLevel = 1;			// How undamaged am I (fraction of above GROWTHS that can apply)

	protected SubType eType;			// What am I

	// Hit Box aids
	private Rectangle	me	= new Rectangle();									/** The rectangle used for this entity during collision detection */
	private Rectangle	him	= new Rectangle();									/** The rectangle used for other entities during collision detection */

	protected Entity(SubType eType, String spriteFile) {
		this.eType = eType;
		this.sprite = getSprite(spriteFile);
	}

	protected Entity(SubType eType, Sprite sprite) {
		this.eType = eType;
		this.sprite = sprite;
	}

	public Sprite getSprite(String ref) {
		return new Sprite(ref);
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

		// TODO Fix collision detection to take rotation into account
		// TODO Fix collision detection to take transparency into account
		me.setBounds((int)Math.floor(meLocation.x), (int)Math.floor(meLocation.y), sprite.getWidth(), sprite.getHeight());
		him.setBounds( (int)Math.floor(himLocation.x), (int)Math.floor(himLocation.y), other.sprite.getWidth(), other.sprite.getHeight());

		return me.intersects(him);
	}

	public void collidedWith(Entity other) {}

	public void doLogic(double secondsElapsed) {

	}
}
