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

import java.util.ArrayList;

/**
 * An entity representing a shot fired
 *
 */
public class PhaserEntity extends ShipEntity {
	private boolean used;
	/**
	 * True if this shot has been "used", i.e. its hit something
	 */
	private Entity Parent;
	/**
	 * The parent entity for this torpedo - the only entity that cannot be 'hit' by the torpedo
	 */
	private double Range;
	private double armedDelay;

	/**
	 * Create a new shot from the player
	 *
	 * @param sourceShip The ship that fired the shot
	 * @param spriteFile The sprite file to be used for this shot
	 */
	public PhaserEntity(Sector thisSector, Entity sourceShip, String spriteFile) {
		super(Transaction.SubType.PHASER, thisSector, spriteFile, sourceShip.getX(), sourceShip.getY());
		mySector = thisSector;

		this.Parent = sourceShip;
		//this.Range = 8.1; // seconds
		this.armedDelay = 0.4;    // enough time to clear ourselves
		energyLevel = 0;
		shieldPercent = 0;
		solidity = 0;
	}

	// Override some parent commands that do NOT apply to torpedo
	public boolean fireTorpedo(double direction) {
		return false;
	}

	public boolean firePhaser(double direction, double power) {
		return false;
	}

	public void move(double delta) {
		// proceed with normal move
		super.move(delta);

		if (armedDelay > 0) {
			armedDelay -= delta;
			return;
		}
	}

	public boolean IDied() {
		if (energyLevel <= 0) return true;

		return false;
	}

	public void doLogic(double delta, ArrayList<Transaction> transactions) {
		super.doLogic(delta, transactions);

		energyTransaction(transactions, Transaction.Action.DEDUCT, delta * 1000);
	}
}
