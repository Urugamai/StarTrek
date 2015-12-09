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

public class EnemyShipEntity extends Entity {

	private static int  RIGHT_BORDER      = 750;	/** Right border at which to disallow further movement */
	private static int  LEFT_BORDER       = 10;		/** Left border at which to disallow further movement */
	private static int  TOP_BORDER;
	private static int  BOTTOM_BORDER;

	private long		accumulatedTime		= 0;
	private static long	courseDuration = 10000;

	private Game		game;						/** The game in which the entity exists */

	private float heading = 0.0f;
	private float speed = 0.0f;

	/**
	 * Create a new alien entity
	 *
	 * @param game The game in which this entity is being created
	 * @param x The intial x location of this alien
	 * @param y The intial y location of this alien
	 */
	public EnemyShipEntity(Game game, String shipFile, int x, int y) {
		super(game.getSprite(shipFile), x, y);

		this.game = game;

		RIGHT_BORDER = game.getWidth() - 30;
		LEFT_BORDER = 30;
		TOP_BORDER = 30;
		BOTTOM_BORDER = game.getHeight() - 30;
	}

	public void newHeading() {

		heading = ((float)Math.random()*360.0f + 180 ) % 360;

		sprite.setAngle(heading);
		sprite.setRotationSpeed(0.2f);
	}

	public void newSpeed() {
		speed = (float)Math.random()*100.0f;
	}

	/**
	 * Request that this alien move
	 *
	 * @param delta The time that has elapsed since last move
	 */
	public void move(long delta) {
		accumulatedTime += delta;
		if (accumulatedTime > courseDuration) {
			newHeading();
			newSpeed();
			accumulatedTime -= courseDuration;
		}

		if ((dx < 0) && (x < LEFT_BORDER)) { speed = 0; }
		if ((dx > 0) && (x > RIGHT_BORDER)) { speed = 0; }
		if ((dy < 0) && (y < TOP_BORDER)) { speed = 0; }
		if ((dy > 0) && (y > BOTTOM_BORDER)) { speed = 0; }

		float rads = (float)Math.toRadians(sprite.getCurrentAngle());
		dx = (float)Math.cos(rads)*speed;
		dy = (float)Math.sin(rads)*speed;

		// proceed with normal move
		super.move(delta);
	}

	/**
	 * Update the game logic related to aliens
	 */
	public void doLogic() {
	}

	/**
	 * Notification that this alien has collided with another entity
	 *
	 * @param other The other entity
	 */
	public void collidedWith(Entity other) {
		// collisions with aliens are handled elsewhere
	}
}
