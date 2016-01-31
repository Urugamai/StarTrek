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

public class RomulanEntity extends ShipEntity {
	private double accumulatedTime = 0;
	private static double courseDuration = 10000;

	private float heading = 0.0f;
	private float speed = 0.0f;

	/**
	 * Create a new alien entity
	 *
	 * @param x The intial x location of this alien
	 * @param y The intial y location of this alien
	 */
	public RomulanEntity(Sector thisSector, String shipFile, int x, int y) {
		super(entityType.ROMULANSHIP, thisSector, shipFile, x, y);
		currentSector = thisSector;
		energyLevel = 900;
	}

	public void newHeading() {

		heading = ((float) Math.random() * 360.0f + 180) % 360;

		sprite.setAngle(heading, 0);
	}

	public void newSpeed() {
		speed = (float) Math.random() * (Constants.c * Constants.IMPULSE_MAX);
	}

	/**
	 * Request that this alien move
	 *
	 * @param delta The time that has elapsed since last move
	 */
	public void move(double delta) {
		accumulatedTime += delta;
		if (accumulatedTime > courseDuration) {
			newHeading();
			newSpeed();
			accumulatedTime -= courseDuration;
		}

		// proceed with normal move
		super.move(delta);
	}

	/**
	 * Update the game logic related to aliens
	 */
	public void doLogic(double delta) {
		move(delta);
	}

}
