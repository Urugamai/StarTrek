/**
 * Created by Mark on 23/01/2016.
 */
public abstract class ObjectEntity extends Entity {
	private float 		rotationSpeed = 30.0f;									/** Degrees per second */
	private boolean		rotateClockwise = true;
	private float		currentAngle = 0;

	public ObjectEntity(Entity.entityType objectType, Sector thisSector, String ref, int x, int y) {
		super(objectType, ref, x, y);
		//this.setHeading(-1.0f, 0);		// set this sprite to constantly rotate clockwise

		currentSector = thisSector;
	}

	private void Rotate(double delta) {

			// PERMANENT rotations
		if (! rotateClockwise) {		// Permanent anti-clockwise rotation
			currentAngle -= rotationSpeed*delta;
			if (currentAngle <= 0.0f) currentAngle += 360.0f;
		} else {
			currentAngle += rotationSpeed*delta;
			currentAngle %= 360.0f;
		}

		sprite.setAngle(currentAngle, 0);
	}
	public void move(double delta) {
		Rotate(delta);
	}

	public void doLogic(double delta) {
		move(delta);
	}

	public boolean doWarpJump(){return false;}

	public double getWarpSpeed(){ return 0;}

	public void warpJumpDone(){}

	public Galaxy.locationSpec calculateWarpJump(Galaxy.locationSpec loc) { return null; }

}
