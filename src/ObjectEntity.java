import java.util.ArrayList;

/**
 * Created by Mark on 23/01/2016.
 */
public abstract class ObjectEntity extends Entity {
	private float 		rotationSpeed = 30.0f;									/** Degrees per second */
	private boolean		rotateClockwise = true;
	private float		currentAngle = 0;

	public ObjectEntity(Transaction.SubType objectType, Sector thisSector, String ref, int x, int y) {
		super(objectType, ref, x, y);
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

	public void doLogic(double delta, ArrayList<Transaction> transactions) {
		move(delta);
	}

	public boolean doWarpJump(){return false;}

	public double getWarpSpeed(){ return 0;}

	public void warpJumpDone(){}

	public Galaxy.locationSpec calculateWarpJump(Galaxy.locationSpec loc) { return null; }

}
