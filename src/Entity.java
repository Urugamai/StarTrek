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

import java.awt.Rectangle;

public abstract class Entity {
	public static enum entityType { STAR, STARBASE, FEDERATIONSHIP, ROMULANSHIP, TORPEDO, BORGSHIP };	// Who Am I

	double x, y, z;												/** Where Am I */
	float currentInclination, currentAngle, velocity = 0.0f;	/** Where Am I Going */

	private float  targetAngle, targetInclination;				/** Where Do I Want To Go */

	float rotationSpeed = 90.0f;						/** Degrees per second */
	float thrustAcceleration = 0, thrustDuration = 0;

	protected Sprite	sprite;					/** The sprite (graphics) that represents this entity */
	protected entityType eType;

	private Rectangle	me	= new Rectangle();	/** The rectangle used for this entity during collisions  resolution */
	private Rectangle	him	= new Rectangle();	/** The rectangle used for other entities during collision resolution */

	/**
	 * Construct a entity based on a sprite image and a location.
	 *
	 * @param sprite The reference to the image to be displayed for this entity
	 */
	protected Entity(entityType eType, Sprite sprite, int x, int y) {
		this.eType = eType;
		this.sprite = sprite;
		this.x = x;
		this.y = y;
		currentAngle = 0;
		currentInclination = 0;
		targetAngle = 0;
		targetInclination = 0;
	}

	public void setHeading(float newDegrees, float newInclination) {
		targetAngle = newDegrees;
		targetInclination = newInclination;
	}

	public void setThrust(float accel, float duration) {
		thrustAcceleration = accel;
		thrustDuration = duration;
	}

	// Should only be used to implement 'all stop' command (velocity = 0)
	public void setVelocity(float newVelocity) {
		velocity = newVelocity;
	}

	private void Rotate(long delta) {
		int f1, f2;

		if (targetAngle >= 0 && currentAngle != targetAngle) {
			if (Math.abs(currentAngle - targetAngle) > 180) f1 = -1; else f1 = 1;
			if (currentAngle < targetAngle) f2 = 1; else f2 = -1;
			currentAngle += rotationSpeed*f1*f2*delta;

			// Standardise on positive angle between 0 and 360 degrees.
			if (currentAngle < 0) currentAngle += 360;
			currentAngle %= 360;
			if ( Math.abs(currentAngle - targetAngle) <= (rotationSpeed)) currentAngle = targetAngle;

		// PERMANENT rotations
		} else if (targetAngle <= -2.0f) {		// Permanent anti-clockwise rotation
			currentAngle -= rotationSpeed*delta;
			if (currentAngle <= 0.0f) currentAngle += 360.0f;
		} else if (targetAngle <= -1.0f) {		// Permanent clockwise rotation
			currentAngle += rotationSpeed*delta;
			currentAngle %= 360.0f;
		}

		// Add code for Z rotation when we implement a 3d screen, use same rotation speed for this
		currentInclination = 0; //= targetInclination;	// just to stop the 'not in use' highlighting ;-)

		sprite.setAngle(currentAngle, currentInclination);
	}

	private void Translate(long delta) {
		double rAngle = Math.toRadians(currentAngle);

		if (thrustDuration > 0) {
			velocity += (thrustAcceleration * delta);
			thrustDuration -= delta;
			if (thrustDuration < delta) { thrustAcceleration = 0; thrustDuration = 0; }
		}

		double vx = velocity*Math.cos(rAngle);
		double vy = -velocity*Math.sin(rAngle);
		double vz = 0; //velocity*Math.sin(currentInclination);

		x = (x + (vx * delta));
		y = (y + (vy * delta));
		z = (z + (vz * delta));
	}

	/**
	 * Request that this entity move itself based on a certain amount
	 * of time passing.
	 *
	 * @param delta The amount of time that has passed in milliseconds
	 */
	public void move(long delta) {
		System.out.println("MOVE: " + delta);
		Rotate(delta/10);

		Translate(delta/10);
	}

	/**
	 * Draw this entity to the graphics context provided
	 */
	public void draw() {
		sprite.draw((int)x, (int)y);
	}

	/**
	 * Do the logic associated with this entity. This method
	 * will be called periodically based on game events
	 */
	public void doLogic() {
	}

	/**
	 * Get the x location of this entity
	 *
	 * @return The x location of this entity
	 */
	public int getX() {
		return (int)x;
	}

	/**
	 * Get the y location of this entity
	 *
	 * @return The y location of this entity
	 */
	public int getY() {
		return (int)y;
	}

	/**
	 * Check if this entity collides with another.
	 * TODO: Probably need collision detection to be smarter
	 * 		- Take into account transparent part of rectangle (no collision)
	 *
	 * @param other The other entity to check collision against
	 * @return True if the entities collide with each other
	 */
	public boolean collidesWith(Entity other) {
		me.setBounds((int)x, (int)y, sprite.getWidth(), sprite.getHeight());
		him.setBounds( (int)other.x, (int)other.y, other.sprite.getWidth(), other.sprite.getHeight());

		return me.intersects(him);
	}

	/**
	 * Notification that this entity collided with another.
	 *
	 * @param other The entity with which this entity collided.
	 */
	public abstract void collidedWith(Entity other);
}
