import java.util.ArrayList;

/**
 * Created by Mark on 23/01/2016.
 */
public abstract class ShipEntity extends Entity {
	protected int 		torpedoCount = Constants.maxTorpedoes;
	protected float		shieldPercent = 100;											// shield strength
	protected boolean	shieldsUp = true;

	protected StarbaseEntity dockedTo = null;
	private double		damageTimer = 0;

	private float  		targetAngle, targetInclination;							/** Where Do I Want To Go */
	private float 		currentInclination, currentAngle;

	private float 		rotationSpeed = 30.0f;									/** Degrees per second */

	private float 		thrustAcceleration = 0, thrustDuration = 0;
	protected float		velocity = 0.0f;

	private double		warpSpeed = 0;
	private float		warpDuration = 0;
	private double		warpDelay = 0;
	private boolean		warpJump = false;

	private double		gdx = 0, gdy = 0, gdz = 0;		// Galactic Coordinate spring-loading warp galactic sector delta

	public ShipEntity(Transaction.SubType shipType, String ref, int sectorX, int sectorY) {
		super(shipType, ref, sectorX, sectorY);
		currentAngle = 0;
		currentInclination = 0;
		targetAngle = 0;
		targetInclination = 0;
		energyLevel = 1000;
		de = 1;
		solidity = 100;
		ds = 1;
	}

	public ShipEntity(Transaction.SubType shipType, Sprite ref, int sectorX, int sectorY) {
		super(shipType, ref, sectorX, sectorY);
		currentAngle = 0;
		currentInclination = 0;
		targetAngle = 0;
		targetInclination = 0;
		energyLevel = 1000;
		de = 1;
		solidity = 100;
		ds = 1;
	}

	// Add OR Subtract from the ships energy reserves
	protected double addEnergy(double increment) {
		double newEnergyLevel = energyLevel + increment;
		if (newEnergyLevel > Constants.maxEnergy) newEnergyLevel = Constants.maxEnergy;
		if (newEnergyLevel < 0) newEnergyLevel = 0;

		increment = newEnergyLevel - energyLevel;
		energyLevel = (float)newEnergyLevel;

		return increment;
	}

	protected void addSolidity(double increment) {
		solidity += increment;
		if (solidity > 100) solidity = 100;
		if (solidity < 0) solidity = 0;
	}

	public boolean isDocked() { return (dockedTo != null); }

	public void setHeading(float newDegrees, float newInclination) {
		targetAngle = newDegrees;
		targetInclination = newInclination;
	}

	public float getHeading() {
		return currentAngle;
	}

	public void setImmediateHeading(float newDegrees, float newInclination) {
		setHeading(newDegrees, newInclination);
		currentAngle = newDegrees;
		currentInclination = newInclination;

	}

	public float getThrust() { return thrustAcceleration; }

	public void setThrust(float accel, float duration) {
		thrustAcceleration = accel;
		thrustDuration = duration;
	}

	public float getVelocity() {
		return velocity;
	}

	// Should only be used to implement 'all stop' command (velocity = 0)
	public void setVelocity(float newVelocity) {
		velocity = newVelocity;
	}

	public boolean doWarpJump() {
		return warpJump;
	}

	public void setWarp(float warpSpeed, float duration) {
		if (warpSpeed < 1 || duration < 1) return;

		this.warpSpeed = warpSpeed;
		warpDuration = duration;
		warpDelay = 6;    // Maximum time to rotate to correct heading
		warpJump = false;
	}

	public void warpMove(double delta) {
		if (warpSpeed <= 0) return;

		double energy = Math.pow(warpSpeed, 2)*10*delta;
		addEnergy(energy);

		warpDelay -= delta;
		if (warpDelay < 0) {
			warpDelay = 1;
			warpJump = true;	// we jump once per second, warpSpeed distance
		}
	}

	public void warpJumpDone() {
		warpJump = false;
		if (warpDuration <= 0) {	// jump complete
			warpDuration = 0;
			warpSpeed = 0;
			gdx = 0; gdy = 0; gdz = 0;
		}
	}

	public double getWarpSpeed() {
		return warpSpeed;
	}

	public Galaxy.locationSpec calculateWarpJump(Galaxy.locationSpec loc) {

		double rAngle = Math.toRadians(currentAngle);

		// TODO: Make the delta accumulative until it exceeds 1, then subtract int(d.) from d. and continue
		gdx += warpSpeed * 1 * Math.cos(rAngle);
		gdy += -warpSpeed * 1 * Math.sin(rAngle);
		gdz += 0; //warpSpeed * 1 * Math.sin(currentInclination);

		if ((int)gdx != 0 || (int)gdy != 0 || (int)gdz != 0 ) {
			loc.addGx((int)gdx);
			loc.addGy((int)gdy);
			loc.addGz((int)gdz);
			warpDuration -= 1;  // only decrement duration once we jump somewhere else
		}

		gdx -= (int)gdx;
		gdy -= (int)gdy;
		gdz -= (int)gdz;

		return loc;
	}
	public boolean fireTorpedo(double direction) {
		// direction irrelevant at this level

		if (torpedoCount > 0) { torpedoCount--; return true; }
		return false;
	}

	public boolean firePhaser(double direction, double power) {
		// direction irrelevant at this level
		if (power > energyLevel) return false;

		energyLevel -= power;

		return true;
	}

	private void Rotate(double delta) {
		int f1, f2;

		if (targetAngle >= 0 && currentAngle != targetAngle) {
			if (Math.abs(currentAngle - targetAngle) > 180) f1 = -1; else f1 = 1;
			if (currentAngle < targetAngle) f2 = 1; else f2 = -1;
			currentAngle += rotationSpeed*f1*f2*delta;

			// Standardise on positive angle between 0 and 360 degrees.
			if (currentAngle < 0) currentAngle += 360;
			currentAngle %= 360;
			if ( Math.abs(currentAngle - targetAngle) <= (rotationSpeed*delta)) currentAngle = targetAngle;

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

	private void Translate(double delta) {
		double rAngle = Math.toRadians(currentAngle);

		if (thrustDuration > 0) {
			double energyUsed = Math.pow(thrustAcceleration/10, 2) * delta;
			addEnergy(-energyUsed);

			velocity += (thrustAcceleration * delta);
			if (velocity < 0) { velocity = 0; thrustDuration = 0; thrustAcceleration = 0; return; }

			thrustDuration -= delta;
			if (thrustDuration < delta) { thrustAcceleration = 0; thrustDuration = 0; }
		}

		double vx = velocity*Math.cos(rAngle);
		double vy = -velocity*Math.sin(rAngle);
		double vz = 0; //velocity*Math.sin(currentInclination);

//		x = (x + (vx * delta));
//		y = (y + (vy * delta));
//		z = (z + (vz * delta));
	}

	public void move(double delta) {
		Rotate(delta);
		Translate(delta);
		warpMove(delta);
	}

	public void doLogic(double delta, ArrayList<Transaction> transactions) {
		energyTransaction(transactions, Transaction.Action.ADD, de*delta);

		if (shieldsUp) {
			energyTransaction(transactions, Transaction.Action.DEDUCT, shieldPercent/100*2*delta);
		}

		energyTransaction(transactions, Transaction.Action.DEDUCT, thrustAcceleration*delta);

		move(delta);

		if (dockedTo != null) {
			if (! collidesWith(dockedTo)) dockedTo = null;	// we have left the base
			else {
				if (velocity <= 2.0) {
					// Docked bonus
					if (dockedTo.energyLevel < dockedTo.de * delta) {
						dockedTo.energyTransaction(transactions, Transaction.Action.DEDUCT, dockedTo.energyLevel);
						energyTransaction(transactions, Transaction.Action.ADD, dockedTo.energyLevel);
					} else {
						dockedTo.energyTransaction(transactions, Transaction.Action.DEDUCT, dockedTo.de * delta);
						energyTransaction(transactions, Transaction.Action.ADD, dockedTo.de * delta);
					}

					if (dockedTo.torpedoCount > 1 && torpedoCount < Constants.maxTorpedoes) {
						torpedoTransaction(transactions, Transaction.Action.ADD, 1);
						dockedTo.torpedoTransaction(transactions, Transaction.Action.DEDUCT, 1);
					}

					if (solidity < 100 && dockedTo.solidity > 0) {
						structureTransaction(transactions, Transaction.Action.ADD, dockedTo.solidity/100*delta);
						dockedTo.energyTransaction(transactions, Transaction.Action.DEDUCT, 10*delta);	// 10 energy per 1 solidity
					}
				} else dockedTo = null;	// Too fast, no longer docked but are still COLLIDING!
			}
		}
	}

	private double processShieldHit(double energy) {
		if (! shieldsUp) return energy;

		double blocked = shieldPercent*10+1;
		double leakage = energy <= blocked ? (1 - shieldPercent/101)*energy : (energy - blocked);
		double powerConsumption = energy <= blocked ? energy/10 : blocked/10;
		addEnergy(-powerConsumption);
		shieldPercent = energy <= blocked ? (float)(1 - energy/blocked)*shieldPercent : 0;

		return leakage;
	}

	private double processStructuralHit(double energy, double physical) {
		double strength = solidity*20+1;
		double leakage = energy <= strength ? (1 - solidity/100)*energy : energy - strength;
		double newSolidity = energy <= strength ? (float)(1-energy/strength)*solidity : 0;
		addSolidity(newSolidity - solidity);

		return leakage;
	}

	public void processHit(double energy, double solidity, ArrayList<Transaction> transactions) {
		if (energy <= 0 && solidity <= 0) return;


		energy = processShieldHit(energy);
		energy = processStructuralHit(energy, solidity);
		addEnergy(-energy);

		if (solidity < 1) entityTransaction( transactions, Transaction.Action.DELETE, 0); // super.mySector.queueEntity(Constants.listType.REMOVE, this); // TODO: Handle Player dying

		System.out.println(this.eType + ": Energy: " + energyLevel + " Shields: " + shieldPercent + "% Structural Integrity: " + solidity + "%");
	}

	private void damageCalc(Entity other, ArrayList<Transaction> transactions) {
		if (other.eType == Transaction.SubType.BORGSHIP || other.eType == Transaction.SubType.FEDERATIONSHIP || other.eType == Transaction.SubType.ROMULANSHIP || other.eType == Transaction.SubType.STARBASE)
			((ShipEntity) other).processHit(this.energyLevel, this.solidity, transactions);
	}

	public void collidedWith(Entity other, ArrayList<Transaction> transactions) {
		System.err.println(this.eType + " collided with " + other.eType);

		if (other.eType == Transaction.SubType.STARBASE) {
			if (velocity <= 2.0) {
				dockedTo = (StarbaseEntity) other;
				return;
			}
		}

		damageCalc(other, transactions);
	}

	public boolean IDied() {
		if (solidity <= 0) return true;

		return false;
	}
}
