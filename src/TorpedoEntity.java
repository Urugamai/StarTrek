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

/**
 * An entity representing a shot fired
 *
 */
public class TorpedoEntity extends Entity {
	private Sector				sector;						/** The sector in which this entity exists */
	private boolean				used;						/** True if this shot has been "used", i.e. its hit something */
	private Entity				Parent;						/** The parent entity for this torpedo - the only entity that cannot be 'hit' by the torpedo */
	private int					Range;

	/**
	 * Create a new shot from the player
	 *
	 * @param sector The sector in which the shot has been created
	 * @param sourceShip The ship that fired the shot
	 * @param sprite The sprite file to be used for this shot
	 */
	public TorpedoEntity(Sector sector, Entity sourceShip, String sprite) {
		super(entityType.TORPEDO, sector.getSprite(sprite), sourceShip.getX(), sourceShip.getY());

		this.sector = sector;
		this.Parent = sourceShip;
		this.Range = 2000; // Pixels
	}

	/**
	 * Request that this shot moved based on time elapsed
	 *
	 * @param delta The time that has elapsed since last move
	 */
	public void move(double delta) {
		// proceed with normal move
		super.move(delta);
		if (this.x < 0 || this.x > Range || this.y < 0 || this.y > Range) { used=true; sector.removeEntity(this); }
	}

	/**
	 * Notification that this shot has collided with another
	 * entity
	 *
	 * @param other The other entity with which we've collided
	 */
	public void collidedWith(Entity other) {
		// prevents double kills, if we've already hit something,
		// don't collide
		if (used) {
			return;
		}

		if (other == Parent) return; // We start the torpedo IN SHIP so this happens initially

		sector.removeEntity(this);	// Torpedo ALWAYS dies on hitting something

		// if we've hit an alien, kill it!
		if (other != null) {
			// remove the affected entities
			if (other instanceof RomulanEntity) sector.removeEntity(other);		// TODO: Replace with a DAMAGE calculation and IF appropriate call removeEntity

			// notify the sector that the alien has been killed
			sector.notifyAlienKilled();
			used = true;
		}
	}
}
