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

public class PlayerShipEntity extends Entity {
	private static int  RIGHT_BORDER      = 750;	/** Right border at which to disallow further movement */
	private static int  LEFT_BORDER       = 10;	/** Left border at which to disallow further movement */
	private static int  TOP_BORDER;
	private static int  BOTTOM_BORDER;
	private Game game;									/** The game in which the ship exists */

	/**
	 * Create a new entity to represent the players ship
	 *
	 * @param game The game in which the ship is being created
	 * @param ref The reference to the sprite to show for the ship
	 * @param x The initial x location of the player's ship
	 * @param y The initial y location of the player's ship
	 */
	public PlayerShipEntity(Game game,String ref,int x,int y) {
		super(entityType.FEDERATIONSHIP, game.getSprite(ref), x, y);

		this.game = game;

		RIGHT_BORDER = game.getWidth() - 30;
		LEFT_BORDER = 30;
		TOP_BORDER = 30;
		BOTTOM_BORDER = game.getHeight() - 30;
	}

	/**
	 * Request that the ship move itself based on an elapsed amount of
	 * time
	 *
	 * @param delta The time that has elapsed since last move (ms)
	 */
	public void move(long delta) {
		super.move(delta);
	}

	/**
	 * Notification that the player's ship has collided with something
	 *
	 * @param other The entity with which the ship has collided
	 */
	public void collidedWith(Entity other) {

		if (other instanceof RomulanEntity) {
			//game.notifyDeath();
		}
	}
}
