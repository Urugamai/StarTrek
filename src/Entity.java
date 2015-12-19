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
	protected Newtonian.objectData thisObject;

	int x, y, z;

	protected Sprite	sprite;					/** The sprite that represents this entity */

	private Rectangle	me	= new Rectangle();	/** The rectangle used for this entity during collisions  resolution */
	private Rectangle	him	= new Rectangle();	/** The rectangle used for other entities during collision resolution */

	/**
	 * Construct a entity based on a sprite image and a location.
	 *
	 * @param sprite The reference to the image to be displayed for this entity
	 */
	protected Entity(Sprite sprite, int x, int y) {
		this.sprite = sprite;
		this.x = x;
		this.y = y;
	}

	public void addNewtonianObject(Newtonian.objectData theObject) {
		thisObject = theObject;
	}

	public void setHeading(double newDegrees) { thisObject.heading = newDegrees;}

	public void setThrust(double accel, double duration) { thisObject.thrustAcceleration = accel; thisObject.thrustDuration = duration; }

	/**
	 * Request that this entity move itself based on a certain amount
	 * of time passing.
	 *
	 * @param delta The amount of time that has passed in milliseconds
	 */
	public void move(long delta) {
		this.x = (int)(thisObject.x / Constants.PixelSize);
		this.y = (int)(thisObject.y / Constants.PixelSize);
		this.z = (int)(thisObject.z / Constants.PixelSize);
	}

	/**
	 * Draw this entity to the graphics context provided
	 */
	public void draw() {
		sprite.draw(x, y);
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
		return x;
	}

	/**
	 * Get the y location of this entity
	 *
	 * @return The y location of this entity
	 */
	public int getY() {
		return y;
	}

	/**
	 * Check if this entity collised with another.
	 *
	 * @param other The other entity to check collision against
	 * @return True if the entities collide with each other
	 */
	public boolean collidesWith(Entity other) {
		me.setBounds(x, y, sprite.getWidth(), sprite.getHeight());
		him.setBounds( other.x, other.y, other.sprite.getWidth(), other.sprite.getHeight());

		return me.intersects(him);
	}

	/**
	 * Notification that this entity collided with another.
	 *
	 * @param other The entity with which this entity collided.
	 */
	public abstract void collidedWith(Entity other);
}
