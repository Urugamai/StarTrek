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
import java.util.ArrayList;

public abstract class Entity {
	// Its all about ME
	protected Sector 		mySector;											/** The sector in which this entity is located */
	protected double 		x, y, z;												/** Where Am I */
	protected float		energyLevel = 0, de = 0;											// How much energy am I carrying (explosive force), what is my rate of growth in energy per second
	protected float		solidity = 0, ds = 0;												// structural strength

	private static TextureLoader		textureLoader;
	protected Sprite	sprite;													/** The sprite (graphics) that represents this entity */
	protected Transaction.SubType eType;

	// What about others?
	private Rectangle	me	= new Rectangle();									/** The rectangle used for this entity during collision detection */
	private Rectangle	him	= new Rectangle();									/** The rectangle used for other entities during collision detection */

	/**
	 * Construct a entity based on a sprite image and a location.
	 *
	 */
	protected Entity(Transaction.SubType eType, String spriteFile) {
		if (textureLoader == null) textureLoader = new TextureLoader();
		this.eType = eType;
		this.sprite = getSprite(spriteFile);
	}

	public Sprite getSprite(String ref) {
		return new Sprite(textureLoader, ref);
	}

	public void setLocation(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
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
	public int getX() {
		return (int)x;
	}

	public int getY() {
		return (int)y;
	}

	public int getZ() {
		return (int)z;
	}

	public void setX(int newValue) { x = newValue; }

	public void setY(int newValue) { y = newValue; }

	public void setZ(int newValue) { z = newValue; }

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

	private Transaction getEmptyTransaction() {
		Transaction trans = new Transaction();
		trans.type = Transaction.Type.ENTITY;
		trans.subType = eType;
		trans.who = this;

		return trans;
	}

	protected void entityTransaction(ArrayList<Transaction> transactions, Transaction.Action action, double amount) {
		Transaction trans = getEmptyTransaction();
		trans.action = action;
		trans.what = Transaction.What.ENTITY;
		trans.howMuch = amount;
		transactions.add(trans);
	}

	protected void structureTransaction(ArrayList<Transaction> transactions, Transaction.Action action, double amount) {
		Transaction trans = getEmptyTransaction();
		trans.action = action;
		trans.what = Transaction.What.STRUCTURE;
		trans.howMuch = amount;
		transactions.add(trans);
	}

	protected void torpedoTransaction(ArrayList<Transaction> transactions, Transaction.Action action, double amount) {
		Transaction trans = getEmptyTransaction();
		trans.action = action;
		trans.what = Transaction.What.TORPEDO;
		trans.howMuch = amount;
		transactions.add(trans);
	}

	protected void energyTransaction(ArrayList<Transaction> transactions, Transaction.Action action, double amount) {
		Transaction trans = getEmptyTransaction();
		trans.action = action;
		trans.what = Transaction.What.ENERGY;
		trans.howMuch = amount;
		transactions.add(trans);
	}

	public void processTransactions(ArrayList<Transaction> transactions) {

		for (Transaction trans : transactions) {
			if (!trans.active) continue;
			if (trans.type == Transaction.Type.ENTITY) {
				if (trans.subType == this.eType) {
					if (trans.who == this) {
						if (trans.action == Transaction.Action.DEDUCT) {
							if (trans.what == Transaction.What.ENERGY) {
								if (trans.howMuch > 0) {
									if (trans.howMuch <= energyLevel) {
										energyLevel -= trans.howMuch;
									} else energyLevel = 0;
								}
								trans.active = false;
								continue;
							}
						}
						if (trans.action == Transaction.Action.ADD) {
							if (trans.what == Transaction.What.ENERGY) {
								if (trans.howMuch > 0) {
										energyLevel += trans.howMuch;
								}
								trans.active = false;
								continue;
							}
						}

						//TODO: implement Entity transactions
//						System.err.println("ENTITY: " + trans.type + ", " + trans.subType + ", " + trans.who + ", " + trans.action + ", " + trans.what + ", " + trans.howMuch);
						trans.active = false;
					}
				}
			}
		}
	}

	public abstract void collidedWith(Entity other, ArrayList<Transaction> transactions);

	public abstract void move(double delta);

	public abstract void doLogic(double delta, ArrayList<Transaction> transactions);

	public abstract boolean doWarpJump();

	public abstract double getWarpSpeed();

	public abstract void warpJumpDone();

	public abstract Galaxy.locationSpec calculateWarpJump(Galaxy.locationSpec loc);
}
